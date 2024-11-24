package com.project.kakao_login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.style.TextDirection.Companion.Content
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link


class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var viewModel: KakaoAuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = KakaoAuthViewModel(application)
        setContentView(R.layout.activity_settings)
        findViewById<TextView>(R.id.shareKakaoText).setOnClickListener {
            shareToKakao()
        }

        val userNameView = findViewById<TextView>(R.id.profileEditText)
        viewModel.getUserName()?.let{ username ->
            userNameView.text = username
        }?: run{
            userNameView.text = "사용자 이름"
        }

        // 뒤로가기 버튼 이벤트 처리
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // RequestActivity로 이동
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
            finish() // 현재 Activity 종료
        }

        val profileEditText = findViewById<TextInputEditText>(R.id.profileEditText)
        val changeProfileText = findViewById<TextView>(R.id.changeProfileText)

        changeProfileText.setOnClickListener {
            val newProfileName = profileEditText.text.toString()
            if (newProfileName.isNotEmpty()) {
                Toast.makeText(this, "프로필 이름이 '$newProfileName'(으)로 변경되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "프로필 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }


        // 물음표 아이콘 이벤트 처리
        val questionIcon: ImageView = findViewById(R.id.questionIcon)
        questionIcon.setOnClickListener {
            // InfoActivity로 이동
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭 리스너
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            viewModel.kakaoLogout()
        }

        // 회원 탈퇴 버튼 클릭 리스너
        findViewById<Button>(R.id.deleteAccountButton).setOnClickListener {
            showUnlinkConfirmationDialog()
        }
    }
    private fun showUnlinkConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말로 탈퇴하시겠습니까?\n탈퇴 시 모든 데이터가 삭제됩니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                viewModel.kakaoUnlink()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun shareToKakao() {
        // 피드 템플릿 생성
        val feedTemplate = FeedTemplate(
            content = Content(
                title = "Itsme!",
                description = "당신의 말투로 스마트워치 답장을 자동으로 변환해보세요!",
                imageUrl = "https://example.com/your_app_image.jpg",
                link = Link(
                    webUrl = "https://play.google.com/store/apps/details?id=your.package.name",
                    mobileWebUrl = "https://play.google.com/store/apps/details?id=your.package.name"
                )
            ),
            // 버튼은 일단 제외하고 기본 템플릿만 사용
        )

        // 카카오톡 설치 여부 확인
        if (ShareClient.instance.isKakaoTalkSharingAvailable(this)) {
            // 카카오톡으로 공유
            ShareClient.instance.shareDefault(this, feedTemplate) { result, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "공유 실패: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (result != null) {
                    Toast.makeText(
                        this,
                        "공유 성공",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(result.intent)
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유 사용
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(feedTemplate)

            // 커스텀 탭으로 웹 브라우저 실행
            try {
                KakaoCustomTabsClient.openWithDefault(this, sharerUrl)
            } catch (e: Exception) {
                // 커스텀 탭 실패 시 기본 브라우저로 실행
                try {
                    KakaoCustomTabsClient.open(this, sharerUrl)
                } catch (e: Exception) {
                    Toast.makeText(this, "카카오톡 공유하기가 가능한 환경이 아닙니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
