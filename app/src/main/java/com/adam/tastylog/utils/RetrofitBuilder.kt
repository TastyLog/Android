package com.adam.tastylog.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.adam.tastylog.service.RestaurantService
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    // HTTP 요청/응답을 로깅하기 위한 인터셉터 생성
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 요청과 응답의 본문을 포함한 모든 로그 기록
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://54.196.227.9:8080/") // API 기본 URL
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(loggingInterceptor).build())
        .build()

    val restaurantService: RestaurantService = retrofit.create(RestaurantService::class.java)
}
