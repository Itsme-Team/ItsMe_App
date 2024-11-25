package com.project.kakao_login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.content.Context
import android.provider.Settings
import android.text.TextUtils


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500) // 1초 지연

        if (!isNotificationAccessEnabled(this)) {
            requestNotificationAccess(this)
        } else {
            Toast.makeText(this, "알림 접근 권한이 이미 활성화되어 있습니다.", Toast.LENGTH_SHORT).show()
            // 권한이 이미 활성화된 경우 다음 화면으로 이동
            startMainActivity()
        }
    }
    override fun onResume() {
        super.onResume()

        // 설정 화면에서 돌아올 경우 다시 권한 확인
        if (isNotificationAccessEnabled(this)) {
            Toast.makeText(this, "알림 접근 권한이 활성화되었습니다.", Toast.LENGTH_SHORT).show()
            startMainActivity()
        } else {
            Toast.makeText(this, "알림 접근 권한을 활성화해주세요.", Toast.LENGTH_LONG).show()
        }
    }
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun requestNotificationAccess(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
        Toast.makeText(context, "알림 접근 권한을 활성화해주세요.", Toast.LENGTH_LONG).show()
    }

    fun isNotificationAccessEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val packageName = context.packageName
        return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(packageName)
    }

}