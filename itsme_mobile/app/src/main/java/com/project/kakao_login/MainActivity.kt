package com.project.kakao_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.project.kakao_login.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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