package com.project.kakao_login

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.kakao_login.KakaoAuthViewModel.Companion.PREF_NAME
import com.project.kakao_login.KakaoAuthViewModel.Companion.USER_ID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "default_channel"
        private const val NOTIFICATION_ID = 1
    }

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token: $token")
        sendRegistrationToServer(token)
    }

    fun sendRegistrationToServer(token: String) {
        val userId = getUserId()
        if (userId.isNotEmpty()) {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://itsmeweb.site")
//                .baseUrl("http://10.0.2.2:8000")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            val call = service.registerFcmToken(TokenRequest(userId, token))
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "FCM 토큰이 서버에 성공적으로 등록되었습니다.")
                    } else {
                        Log.e(TAG, "FCM 토큰 등록 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(TAG, "FCM 토큰 등록 실패", t)
                }
            })
        } else {
            Log.e(TAG, "사용자 ID가 없습니다. FCM 토큰을 등록할 수 없습니다.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleNow(remoteMessage.data)
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body)
        }
    }

    private fun handleNow(data: Map<String, String>) {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun showNotification(title: String?, messageBody: String?) {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            Log.d(TAG, "Notification shown successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    private fun getUserId(): String {
        return sharedPreferences.getString(USER_ID, "") ?: ""
    }
}

interface ApiService {
    @POST("api/fcm_token/")
    fun registerFcmToken(@Body tokenRequest: TokenRequest): Call<Void>
}

data class TokenRequest(val user_id: String, val token: String)