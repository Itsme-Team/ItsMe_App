package com.project.itsme

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val pageDescription: TextView = findViewById(R.id.pageDescription)
        val pageImageView: ImageView = findViewById(R.id.pageImageView)
        val speechText: TextView = findViewById(R.id.speechText) // 말풍선 텍스트 추가

        viewPager.adapter = ViewPagerAdapter(this)

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // 각 탭 크기와 간격 조정
        tabLayout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            for (i in 0 until tabLayout.tabCount) {
                val tab = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i) as ViewGroup
                val layoutParams = tab.layoutParams as ViewGroup.MarginLayoutParams

                layoutParams.width = 8.dpToPx() // 각 점의 너비
                layoutParams.height = 8.dpToPx() // 각 점의 높이
                layoutParams.setMargins(16.dpToPx(), 0, 16.dpToPx(), 0) // 점 간격
                tab.layoutParams = layoutParams
                tab.requestLayout()
            }
        }

        // 이미지 리소스 배열
        val images = listOf(
            R.drawable.info_image1, // 첫 번째 페이지 이미지
            R.drawable.info_image2, // 두 번째 페이지 이미지
            R.drawable.info_image3  // 세 번째 페이지 이미지
        )

        // 말풍선 텍스트 배열
        val speechBubbles = listOf(
            "기본 답변 리스트에 접속하면,\n변환 가능한 답변이 모두 보이고,\n버튼을 클릭해 그대로 가져갈 수 있어요!",
            "화면 하단의 채팅방 버튼을 누르면\nAI가 변환한 답변이 포함된\n채팅방을 확인할 수 있어요!",
            "홈 화면의 채팅방에 들어가면,\n지금까지의 전송된 답변 목록과\n수정할 수 있는 기능이 있답니다!"
        )

        // 페이지 변경에 따라 설명 및 이미지, 말풍선 업데이트
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageDescription.text = when (position) {
                    0 -> "어떤 답변들을 내 말투로 변환 해주는거지?"
                    1 -> "AI 답변을 활용해 대화한 흐름이 궁금한데..."
                    2 -> "내 말투랑 변환된 답장 말투가 \n다른 것 같아서 답변을 수정하고 싶어요."
                    else -> "기본 설명입니다."
                }
                // 크기, 글씨체, Bold 처리
                when (position) {
                    0 -> {
                        pageDescription.textSize = 18f // 크기 조정 (SP)
                        pageDescription.setTypeface(null, android.graphics.Typeface.BOLD) // Bold 처리
                        pageDescription.setTextColor(getColor(R.color.black)) // 텍스트 색상
                    }
                    1 -> {
                        pageDescription.textSize = 18f // 크기 조정 (SP)
                        pageDescription.setTypeface(null, android.graphics.Typeface.BOLD) // Bold 처리
//                        pageDescription.setTypeface(null, android.graphics.Typeface.ITALIC) // 이탤릭체
                        pageDescription.setTextColor(getColor(R.color.black)) // 텍스트 색상
                    }
                    2 -> {
                        pageDescription.textSize = 18f // 크기 조정 (SP)
                        pageDescription.setTypeface(null, android.graphics.Typeface.BOLD) // Bold 처리
//                        pageDescription.setTypeface(null, android.graphics.Typeface.NORMAL) // 일반 글씨체
                        pageDescription.setTextColor(getColor(R.color.black)) // 텍스트 색상
                    }
                    else -> {
                        pageDescription.textSize = 16f
                        pageDescription.setTypeface(null, android.graphics.Typeface.NORMAL)
                        pageDescription.setTextColor(getColor(R.color.black))
                    }
                }

                // 이미지 업데이트
                pageImageView.setImageResource(images[position])

                // 말풍선 텍스트 업데이트
                speechText.text = speechBubbles[position]
            }
        })
    }

    // dp를 픽셀로 변환하는 함수
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
}
