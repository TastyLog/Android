package com.adam.tastylog

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.adam.tastylog.utils.RetrofitBuilder
import com.adam.tastylog.utils.UpdateSearchTermWorker
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import java.util.concurrent.TimeUnit

class MYApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        MYApplication.applicationContext = applicationContext

        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        // Kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_share_app_key))

        // 주기적으로 실시간 검색어 데이터를 갱신하기 위한 WorkManager 작업 예약
        val workRequest = PeriodicWorkRequestBuilder<UpdateSearchTermWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()) // 네트워크 연결이 필요
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "updateSearchTerms",
            ExistingPeriodicWorkPolicy.KEEP, // 이미 예약된 작업이 있다면 유지
            workRequest
        )
    }

    companion object {
        lateinit var applicationContext: Context
    }

}