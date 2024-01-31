package com.adam.tastylog

import android.app.Application
import android.content.Context
import com.adam.tastylog.utils.RetrofitBuilder
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk

class MYApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        MYApplication.applicationContext = applicationContext

        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        //RetrofitBuilder 초기화
//        RetrofitBuilder.initialize(this)


        // Kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_share_app_key))
    }

    companion object {
        lateinit var applicationContext: Context
    }

}