package com.project.itsme

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KakaoAuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "KakaoAuthViewModel"
        const val PREF_NAME = "login_pref"
        const val IS_LOGGED_IN = "is_logged_in"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
    }

    private val context = application.applicationContext
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        _isLoggedIn.value = sharedPreferences.getBoolean(IS_LOGGED_IN, false)
        Log.d(TAG, "Initial login status: ${_isLoggedIn.value}")
    }

    fun kakaoLogin() {
        viewModelScope.launch {
            val loginSuccess = handleKakaoLogin()
            if (loginSuccess) {
                fetchUserInfo { success ->
                    if (success) {
                        _isLoggedIn.value = true
                        sharedPreferences.edit().putBoolean(IS_LOGGED_IN, true).apply()
                        Log.d(TAG, "Login successful, saved login status to SharedPreferences")
                        Toast.makeText(context, "로그인이 확인되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        _isLoggedIn.value = false
                        Log.e(TAG, "Login failed to fetch user info.")
                    }
                }
            } else {
                _isLoggedIn.value = false
                Log.e(TAG, "Login failed.")
            }
        }
    }
    fun kakaoUnlink() {
        viewModelScope.launch {
            val isUnlinked = suspendCoroutine<Boolean> { continuation ->
                UserApiClient.instance.unlink { error ->
                    if (error != null) {
                        Log.e(TAG, "Failed to unlink from Kakao. Error: $error")
                        continuation.resume(false)
                    } else {
                        Log.i(TAG, "Successfully unlinked from Kakao")
                        continuation.resume(true)
                    }
                }
            }

            if (isUnlinked) {
                clearLocalUserData()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "회원 탈퇴에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun kakaoLogout() {
        viewModelScope.launch {
            val isLoggedOut = UserApiClient.instance.run {
                if (isKakaoTalkLoginAvailable(context)) {
                    handleKakaoLogout()
                } else {
                    // 토큰이 없는 경우, 이미 로그아웃된 상태로 간주
                    true
                }
            }

            if (isLoggedOut) {
                clearLocalUserData()
                navigateToMainActivity()
            } else {
                Log.e(TAG, "Logout failed")
                // 로그아웃 실패 처리 (예: 사용자에게 알림)
                Toast.makeText(context, "로그아웃에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun handleKakaoLogout(): Boolean =
        suspendCoroutine { continuation ->
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e(TAG, "Logout failed. Error: $error")
                    when (error) {
                        is ClientError -> {
                            if (error.reason == ClientErrorCause.TokenNotFound) {
                                // 토큰이 없는 경우, 이미 로그아웃된 상태로 간주
                                continuation.resume(true)
                            } else {
                                continuation.resume(false)
                            }
                        }
                        else -> continuation.resume(false)
                    }
                } else {
                    Log.i(TAG, "Logout successful")
                    continuation.resume(true)
                }
            }
        }

    private fun clearLocalUserData() {
        _isLoggedIn.value = false
        sharedPreferences.edit().apply {
            clear()
            remove(IS_LOGGED_IN)
            remove(USER_ID)
            remove(USER_NAME)
            apply()
        }
        Log.d(TAG, "Logout successful, cleared SharedPreferences")
    }

    private suspend fun navigateToMainActivity() {
        withContext(Dispatchers.Main) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            _logoutEvent.emit(Unit)
        }
    }

    private suspend fun handleKakaoLogin(): Boolean =
        suspendCoroutine { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "Login failed. Error: $error")
                    continuation.resume(false)
                } else if (token != null) {
                    Log.i(TAG, "Login successful with token: ${token.accessToken}")
                    continuation.resume(true)
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "KakaoTalk login failed. Error: $error")
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            continuation.resume(false)
                            return@loginWithKakaoTalk
                        }
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                    } else if (token != null) {
                        Log.i(TAG, "KakaoTalk login successful with token: ${token.accessToken}")
                        continuation.resume(true)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            }
        }

    private fun fetchUserInfo(onComplete: (Boolean) -> Unit) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "Failed to fetch user info. Error: $error")
                onComplete(false)
            } else if (user != null) {
                val userId = user.id.toString()
                val userName = user.kakaoAccount?.profile?.nickname ?: "Unknown"

                Log.i(TAG, "Fetched user ID: $userId")
                Log.i(TAG, "Fetched user name: $userName")

                sharedPreferences.edit().apply {
                    putString(USER_ID, userId)
                    putString(USER_NAME, userName)
                    apply()
                }
                Log.d(TAG, "User info saved to SharedPreferences")
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun getUserId(): String? {
        val userId = sharedPreferences.getString(USER_ID, null)
        Log.d(TAG, "Retrieved user ID from SharedPreferences: $userId")
        return userId
    }

    fun getUserName(): String? {
        val userName = sharedPreferences.getString(USER_NAME, null)
        Log.d(TAG, "Retrieved user name from SharedPreferences: $userName")
        return userName
    }
}