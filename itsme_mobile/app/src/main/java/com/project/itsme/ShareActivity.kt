package com.project.itsme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.nio.charset.StandardCharsets

class ShareActivity : AppCompatActivity() {
    private val TAG = "ShareActivity"
    private lateinit var mMyAPI: RequestAPI
    private var userId: String? = null
    private var userName: String? = null
    private var fileUri: Uri? = null
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()

    private val sharedPreferences: SharedPreferences
        get() = getSharedPreferences(KakaoAuthViewModel.PREF_NAME, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        Log.d(TAG, "onCreate: ShareActivity started")

        initRequestAPI("https://itsmeweb.site")

        if (intent?.action == Intent.ACTION_SEND) {
            fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
            Log.d(TAG, "Received FILE_URI: $fileUri")

            checkLoginAndProceed()
        } else {
            Log.e(TAG, "Invalid intent action: ${intent?.action}")
            returnToKakaoTalk()
        }
    }

    private fun checkLoginAndProceed() {
        userId = sharedPreferences.getString(KakaoAuthViewModel.USER_ID, null)
        userName = sharedPreferences.getString(KakaoAuthViewModel.USER_NAME, null)

        Log.d(TAG, "Retrieved userId: $userId")
        Log.d(TAG, "Retrieved userName: $userName")

        if (userId == null || userName == null) {
            Log.d(TAG, "User not logged in, starting login process")
            startLoginProcess()
        } else {
            Log.d(TAG, "User already logged in, proceeding with file upload")
            fileUri?.let { postFile(it) }
        }
    }

    private fun startLoginProcess() {
        lifecycleScope.launch {
            kakaoAuthViewModel.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    Log.d(TAG, "Login successful, proceeding with file upload")
                    userId = sharedPreferences.getString(KakaoAuthViewModel.USER_ID, null)
                    userName = sharedPreferences.getString(KakaoAuthViewModel.USER_NAME, null)
                    fileUri?.let { postFile(it) }
                }
            }
        }

        Log.d(TAG, "Initiating Kakao login")
        kakaoAuthViewModel.kakaoLogin()
    }

    private fun initRequestAPI(baseUrl: String) {
        Log.d(TAG, "Initializing RequestAPI with baseUrl: $baseUrl")
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mMyAPI = retrofit.create(RequestAPI::class.java)
    }

    private fun postFile(uri: Uri) {
        Log.d(TAG, "postFile started with URI: $uri")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val file = File(cacheDir, "shared_file.txt")
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val requestFile = RequestBody.create("text/plain".toMediaTypeOrNull(), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val userNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userName?.toByteArray(StandardCharsets.UTF_8)?.let { String(it) } ?: "")
                val userIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userId?.toByteArray(StandardCharsets.UTF_8)?.let { String(it) } ?: "")

                Log.d(TAG, "Sending file upload request")
                val response = withContext(Dispatchers.IO) {
                    mMyAPI.postFile(body, userNameBody, userIdBody).execute()
                }

                if (response.isSuccessful) {
                    Log.d(TAG, "File upload successful: ${response.body()}")
                    Toast.makeText(this@ShareActivity, "파일 업로드 성공!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "File upload failed. Error body: $errorBody")
                    try {
                        val jsonObject = JSONObject(errorBody)
                        val message = jsonObject.optString("message", "알 수 없는 오류")
                        Toast.makeText(this@ShareActivity, message, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing error body", e)
                        Toast.makeText(this@ShareActivity, "파일 업로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "File upload failed with exception", e)
                Toast.makeText(this@ShareActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                Log.d(TAG, "File upload process completed. Returning to KakaoTalk in 2 seconds.")
                Handler(Looper.getMainLooper()).postDelayed({
                    returnToKakaoTalk()
                }, 10)
            }
        }
    }

    private fun returnToKakaoTalk() {
        Log.d(TAG, "Attempting to return to KakaoTalk")
        val kakaoTalkPackageName = "com.kakao.talk"
        val launchIntent = packageManager.getLaunchIntentForPackage(kakaoTalkPackageName)
        if (launchIntent != null) {
            Log.d(TAG, "Launching KakaoTalk")
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        } else {
            Log.d(TAG, "KakaoTalk is not installed")
            Toast.makeText(this, "카카오톡이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
        finish()
        Log.d(TAG, "ShareActivity finished")
    }
}