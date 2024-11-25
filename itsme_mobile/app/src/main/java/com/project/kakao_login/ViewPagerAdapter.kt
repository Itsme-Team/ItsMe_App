package com.project.kakao_login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val imageSources = listOf(
        R.drawable.info_page1, // 첫 번째 페이지 이미지
        R.drawable.info_page2, // 두 번째 페이지 이미지
        R.drawable.info_page3  // 세 번째 페이지 이미지
    )

    override fun getItemCount(): Int = imageSources.size

    override fun createFragment(position: Int): Fragment {
        // ExampleFragment 이미지 리소스 전달
        return ExampleFragment.newInstance(imageSources[position])
    }
}
