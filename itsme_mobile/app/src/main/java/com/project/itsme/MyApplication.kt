package com.project.itsme

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
//import com.project.kakao_login.BuildConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Other initialization code

        // Kakao SDK initialization
        KakaoSdk.init(this, "b01c383e18f12480c03dbd097c75dbfb")
    }
}
