package com.project.itsme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.project.itsme.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.provider.Settings

class MainActivity : AppCompatActivity() {
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()
    private val TAG = "MainActivity"
    private val NOTIFICATION_PERMISSION_CODE = 100

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()

        // 알림 권한 확인 및 요청
        if (!isNotificationAccessEnabled()) {
            requestNotificationAccess()
        } else {
            proceedWithLogin()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "FCM 등록 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM 등록 토큰 가져오기 성공: $token")
            sendRegistrationToServer(token)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    proceedWithLogin()
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE
                    )
                }
            }
        } else {
            proceedWithLogin()
        }
    }

    private fun isNotificationAccessEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        val packageName = packageName
        return !enabledListeners.isNullOrEmpty() && enabledListeners.contains(packageName)
    }

    private fun requestNotificationAccess() {
        Toast.makeText(this, "알림 접근 권한을 활성화해주세요.", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    private fun proceedWithLogin() {
        initUI()

        lifecycleScope.launch {
            kakaoAuthViewModel.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    showLoadingScreen()
                    startRequestActivity()
                } else {
                    hideLoading()
                }
            }
        }
    }

    private fun initUI() {
        binding.btnLogin.setOnClickListener {
            showLoadingScreen()
            kakaoAuthViewModel.kakaoLogin()
        }

        if (kakaoAuthViewModel.isLoggedIn.value) {
            showLoadingScreen()
        } else {
            hideLoading()
        }
    }

    private fun showLoadingScreen() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.btnLogin.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loadingProgressBar.visibility = View.GONE
        binding.btnLogin.visibility = View.VISIBLE
    }

    private fun startRequestActivity() {
        val intent = Intent(this@MainActivity, RequestActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // onResume에서는 별도로 권한 확인 로직을 실행하지 않음
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    proceedWithLogin()
                } else {
                    Toast.makeText(this, "알림 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = getUserId()

        if (userId.isNotEmpty()) {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://itsmeweb.site")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            val tokenRequest = TokenRequest(user_id = userId, token = token)
            Log.d(TAG, "Sending FCM token request: $tokenRequest")

            val call = service.registerFcmToken(tokenRequest)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "FCM 토큰이 서버에 성공적으로 등록되었습니다.")
                    } else {
                        Log.e(TAG, "FCM 토큰 등록 실패: ${response.code()}")
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
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

    private fun getUserId(): String {
        val sharedPreferences = getSharedPreferences(KakaoAuthViewModel.PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KakaoAuthViewModel.USER_ID, "") ?: ""
    }
}
