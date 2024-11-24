package com.project.kakao_login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = listOf(
        ExampleFragment.newInstance(
            "어떤 답변들을 내 말투로 변환해주느냐?",
            "기본 답변 리스트에 접속하면, 변환 가능한 답변이 모두 보이고, 버튼을 클릭해 그대로 가져갈 수 있어요!"
        ),
        ExampleFragment.newInstance(
            "AI 답변을 활용해 대화 흐름이 궁금한데...",
            "화면 하단의 재생 버튼을 누르면 AI가 생성한 답변이 포함된 재생방을 확인할 수 있어요!"
        ),
        ExampleFragment.newInstance(
            "내 말투로 변환된 답변 말투가 다를 것 같아서 답변을 수정하고 싶어요.",
            "홈 화면의 채팅창에 들어가면, 지금까지 전송한 답변 문구와 수정할 수 있는 기능이 있답니다!"
        )
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
