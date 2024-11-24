package com.project.kakao_login

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationUtils {
    fun setupBottomNavigation(activity: Activity, bottomNavigationView: BottomNavigationView) {

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (activity !is RequestActivity) { // 현재 화면이 RequestActivity가 아니면 이동
                        val intent = Intent(activity, RequestActivity::class.java)
                        activity.startActivity(intent)
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // 애니메이션 추가
                        activity.finish() // 현재 화면 종료
                    }
                    true
                }
                R.id.nav_replies -> {
                    if (activity !is DefaultRepliesActivity) { // 현재 화면이 DefaultRepliesActivity가 아니면 이동
                        val intent = Intent(activity, DefaultRepliesActivity::class.java)
                        activity.startActivity(intent)
                        //activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // 애니메이션 추가
                        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        activity.finish() // 현재 화면 종료
                    }
                    true
                }
                R.id.nav_settings -> {
                    if (activity !is SettingsActivity) { // 현재 화면이 DefaultRepliesActivity가 아니면 이동
                        val intent = Intent(activity, SettingsActivity::class.java)
                        activity.startActivity(intent)
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // 애니메이션 추가
//                        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        activity.finish() // 현재 화면 종료
                    }
                    true
                }

                else -> false
            }
        }
    }
}
