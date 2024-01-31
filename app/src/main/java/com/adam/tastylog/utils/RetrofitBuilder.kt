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

//@SuppressLint("StaticFieldLeak")
//object RetrofitBuilder {
//
//    private lateinit var context: Context
//    private lateinit var myCache: Cache
//
//    fun initialize(context: Context) {
//        this.context = context
//        this.myCache = Cache(context.cacheDir, (50 * 1024 * 1024).toLong()) // 10 MB 캐시 크기
//    }
//
//    private val cacheInterceptor = Interceptor { chain ->
//        var request = chain.request()
//        request = if (isNetworkAvailable(context)) {
//            Log.d("RetrofitBuilder", "Network available: using network")
//            request.newBuilder().header("Cache-Control", "public, max-age=" + 5).build()
//        } else {
//            Log.d("RetrofitBuilder", "Network unavailable: using cache")
//            request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
//        }
//        chain.proceed(request)
//    }
//
//
//    private fun isNetworkAvailable(context: Context): Boolean {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork
//        val capabilities = connectivityManager.getNetworkCapabilities(network)
//        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
//    }
//
//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val okHttpClient by lazy {
//        OkHttpClient.Builder()
//            .cache(myCache)
//            .addInterceptor(loggingInterceptor)
//            .addNetworkInterceptor(cacheInterceptor)
//            .build()
//    }
//
//    private val retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl("http://54.196.227.9:8080/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
//            .build()
//    }
//
//    val restaurantService: RestaurantService by lazy {
//        retrofit.create(RestaurantService::class.java)
//    }
//}