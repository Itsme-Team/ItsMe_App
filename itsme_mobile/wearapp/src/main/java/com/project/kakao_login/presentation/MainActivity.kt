package com.project.kakao_login.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.project.kakao_login.databinding.ActivityMainBinding

import com.google.android.gms.wearable.*
import android.util.Log
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener

import com.project.kakao_login.R
import com.project.kakao_login.databinding.ActivityButtonBinding
import org.json.JSONArray


import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {
    private lateinit var binding: ActivityMainBinding

    companion object {
        var replyList: List<List<String>> = listOf()
        var senderList = listOf<String>()
        var MessageSender : String? = null
        var MessageSenderGroup : String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val messageList = intent.getStringArrayListExtra("messageList") ?: ArrayList()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Wearable.getDataClient(this).addListener(this)

        createNotificationChannel()
        binding.buttonSendNotification.setOnClickListener{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED){
                // 권한이 허용된 경우 알림을 보냅니다.
                postNotification("Test Title", "Test Text")
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

    //메세지로 온 답장 리스트를 파싱해서 저장
    private fun parseReplyList(message: String){
        try {
            val regex = "\\[(.*?)\\]".toRegex()
            val matches = regex.findAll(message)

            replyList = matches.map { matchResult ->
                matchResult.groupValues[1]
                    .split("', '")
                    .map { it.trim('\'') }
            }.toList()


            // 결과 출력 (디버깅용)
            println("Wear 디버깅 | replyList : "+replyList)

        } catch (e: Exception) {
            Log.e("MainActivity", "Error parsing message: ${e.message}")
        }
    }

    //안드로이드에서 온 대화상대방리스트를 파싱해서 senderList에 저장
    private fun parseSender(sender_list: String){
        try {
            senderList = sender_list.split("<sep>").map { it.trim() }.filter { it.isNotEmpty() }

            // 결과 출력 (디버깅용)
            println("Wear 디버깅 | senderList : "+ senderList)
        }
        catch (e: Exception) {
            Log.e("MainActivity", "Error parsing message: ${e.message}")
        }
    }

    // 안드로이드에서 온 메세지를 노티로 띄워줌
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                if(event.dataItem.uri.path == "/notification_info"){
                    val text = dataMapItem.dataMap.getString("text") // 메세지
                    val title = dataMapItem.dataMap.getString("title") // 보낸사람
                    val groupName = dataMapItem.dataMap.getString("groupName") // 단톡방이름 없으면 null
                    val packageName = dataMapItem.dataMap.getString("packageName") // 패키지이름
                    // < ---------디버깅용-------->
                    println(groupName == "null")
                    println(title)
                    println("MessageSender : $MessageSender")
                    // <------------------------->
                    if (groupName == "null"){
                        MessageSender = title // 개인톡방이면 messageSender에 보낸사람이름 저장
                        MessageSenderGroup = "null" // 단톡방이 아니면 sendergroup에 "null" 저장
                    }
                    else{
                        MessageSender = groupName // 단톡방이면 messageSender에 단톡방이름 저장
                        MessageSenderGroup = title // 단톡방이면 sendergroup에 보낸사람이름 저장
                    }
                    println("MessageSender : $MessageSender")
                    Log.d("WearMainActivity", "Received notification - Title: $title, Text: $text")
                    postNotification(title.toString(), text.toString())
                }
                else if(event.dataItem.uri.path == "/reply_list") {
                    val room = dataMapItem.dataMap.getString("room")?: "empty room " // 메세지
                    val reply = dataMapItem.dataMap.getString("reply_list")?: "empty list" // 보낸사람
                    postNotification("상대맞춤답장", "반영완료")
                    Log.d("WearMainActivity", "Received reply list - Room: $room, Reply List: $reply")

                    parseSender(room.toString())
                    parseReplyList(reply.toString())
                    println("GET의 경우 {$replyList}")
                }
            }
        }
        dataEvents.release()
    }

    private val channelId = "wear_os_channel_id"
    private val channelName = "Wear OS Channel"
    private val channelDescription = "Channel for Wear OS notifications"
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            postNotification("Hello Wear OS", "This is a test notification")
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    //알림 채널 생성
    private fun createNotificationChannel() {
        // 알림 채널은 Android 8.0 (API 26) 이상에서만 필요합니다.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }
        // 알림 채널을 시스템에 등록합니다.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    //알림 발송
    private fun postNotification(title: String, text: String) {
        val intent = Intent(this, ButtonActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val channelId = "wear_os_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val notificationManager = NotificationManagerCompat.from(this)
        //권한을 확인하고 알림을 보냄
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManager.notify(System.currentTimeMillis().toInt()/*1*/, notificationBuilder.build()) //고유 ID 사용으로 알림 스택
            println("성공")
        }
        else{
            println("실패")
        }
    }

    //버튼화면으로 전환
    private fun navigateToButtonActivity() {
        val intent = Intent(this, ButtonActivity::class.java)
        startActivity(intent)
    }
}
