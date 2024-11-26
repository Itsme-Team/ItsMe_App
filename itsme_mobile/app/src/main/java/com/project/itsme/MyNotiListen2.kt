package com.project.itsme

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import com.faendir.rhino_android.RhinoAndroidHelper
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import org.mozilla.javascript.Context
import com.google.android.gms.wearable.Wearable


class MyNotiListen2 : NotificationListenerService(), DataClient.OnDataChangedListener {

    companion object {
        var execContext: android.content.Context? = null
    }
    override fun onCreate() {
        super.onCreate()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    private fun sendNotiInfo(title: String, text: String, packageName: String, groupName: String) {
        val putDataMapRequest = PutDataMapRequest.create("/notification_info").apply {
            dataMap.putString("title", title)
            dataMap.putString("text", text)
            dataMap.putString("packageName", packageName)
            dataMap.putString("groupName", groupName) // 단톡방 이름 or null
            dataMap.putLong("time", System.currentTimeMillis()) // 중복메세지를 피하기 위해서 타임스탬프 추가

        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
        Log.d("AndroidSendMessage", "WEAROS로 메세지 전송완료")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val packageName: String = sbn?.packageName ?: "Null"
        val extras = sbn?.notification?.extras
        val extraTitle: String = extras?.getString(Notification.EXTRA_TITLE).toString() //알림 제목
        val extraText: String = extras?.get(Notification.EXTRA_TEXT).toString() //본문 텍스트
        val GroupName : String = extras?.getString(Notification.EXTRA_SUB_TEXT).toString() //보조텍스트 -> 단톡방일 시 톡방 이름

        // <-- null 내용은 보내지 않기위한 디버깅 ---->
        //println(extraText)
        //println(GroupName.contains("개의 안 읽은 메시지"))
        //println(extraTitle != null)
        //println(extraTitle.isNotEmpty())
        // <------------------------------------->

        //jp.naver.line.android
        if (packageName == "com.kakao.talk" && GroupName.contains("개의 안 읽은 메시지") == false) {
            sendNotiInfo(extraTitle, extraText, packageName, GroupName )
            // <-- 보낸 내용 디버깅 -->
            Log.d(
                "TAG", "onNotificationPosted:\n"
                        + "PackageName: $packageName\n"
                        + "Title: $extraTitle\n"
                        + "Text: $extraText\n"
                        + "GroupName : $GroupName\n"
            )
            // <------------------->
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/button_text") {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val buttonText = dataMapItem.dataMap.getString("button_text")
                val senderGroup = dataMapItem.dataMap.getString("senderGroup")
                val sender = dataMapItem.dataMap.getString("sender")
//                개인톡방
//                sender : 보낸사람이름
//                senderGroup : "null"
//
//                단체톡방
//                sender : 단톡방이름
//                senderGroup : 보낸사람이름

                Log.d("button received", "button $buttonText received")
                if (sender != null && senderGroup != null){
                    logActiveNotifications(buttonText.toString(), sender, senderGroup)
                }
                else{
                  //흠,, 이럴일이 없기를 바랍시다
                }
            }
        }
    }

    fun logActiveNotifications(myreply: String, sender : String, senderGroup : String) {
        Log.d("CallReplyAction", "logActiveNotification 함수 호출")
        val activeNotifications = getActiveNotifications()
        for (notification in activeNotifications) {
//                개인톡방
//                sender : 보낸사람이름
//                senderGroup : "null"
//
//                단체톡방
//                sender : 단톡방이름
//                senderGroup : 보낸사람이름
            val extras = notification.notification.extras
            val title = extras?.getString(Notification.EXTRA_TITLE)
            val text = extras?.getString(Notification.EXTRA_TEXT)
            val groupName = extras?.getString(Notification.EXTRA_SUB_TEXT)

            if (notification.packageName == "com.kakao.talk") {
                println("<-------------들어옴------------->")
                val actions = notification.notification.actions
                if (actions != null) {
                    for ((index, action) in actions.withIndex()) {
                        println("Action $index:")
                        println("  Title: ${action.title}")
                        println("  Intent: ${action.actionIntent}")
                        println("  RemoteInputs: ${action.remoteInputs?.joinToString { it.resultKey }}")
                        if (action.title.toString().contains("답장", true) || action.title.toString().contains("reply", true)) {
                            // Found the reply action, send the reply
                            execContext = applicationContext
                            if(senderGroup == "null"){
                                if(title == sender){
                                    callResponder(action, myreply)
                                }
                            }
                            else{
                                if(groupName == sender){
                                    callResponder(action, myreply)
                                }
                            }
                        }
                    }
                } else {
                    println("No actions found in the notification.")
                }
            }
            //jp.naver.line.android
//            if (notification.packageName == "com.kakao.talk") {
//                val wExt = Notification.WearableExtender(notification?.notification)
//                val action = wExt.actions.firstOrNull(){act ->
//                    // <-- reply를 찾았는지 찍어봄(디버깅용) -->
//                    val title = act.title.toString()
//                    println("Action Title: $title")
//                    // <----------------------------------->
//                    act.remoteInputs != null && act.remoteInputs.isNotEmpty() &&
//                            (act.title.toString().contains("reply", true) || act.title.toString().contains("답장", true))
//                }
//                if (action != null){
//                    execContext = applicationContext
//                    callResponder(action, myreply)
//                }
//            }
        }
    }

    fun callResponder(session: Notification.Action?, myreply: String){
        println("CallResponder 호출")
        val parseContext = RhinoAndroidHelper.prepareContext()
        val replier = MyNotiListen2.SessionCacheReplier(session)
        parseContext.optimizationLevel = -1
        replier.reply(myreply)
        Context.exit()
    }

    class SessionCacheReplier (private val session : Notification.Action?){
        fun reply(value: String){
            println("CallResponder 호출")
            if (session == null){ return }

            val sendIntent = Intent()
            val msg = Bundle()

            session.remoteInputs?.forEach { inputable -> msg.putCharSequence(inputable.resultKey, value)}

            RemoteInput.addResultsToIntent(session.remoteInputs, sendIntent, msg)

            try {
                session.actionIntent.send(MyNotiListen2.execContext, 0, sendIntent)
            }catch (e:PendingIntent.CanceledException){
                // 예외 처리
            }
        }
    }


}