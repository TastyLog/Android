package com.adam.tastylog.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.adam.tastylog.model.RealTimeSearchTermModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


//object SharedPreferencesManager {
//    private const val PREFS_NAME = "SearchTermsPreferences"
//    private const val LAST_UPDATE_TIME_KEY = "LastUpdateTime"
//
//    // SharedPreferences 인스턴스를 가져오는 함수
//    private fun getSharedPreferences(context: Context): SharedPreferences {
//        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//    }
//
//    // 마지막 API 호출 시간을 저장
//    fun updateLastApiCallTime(context: Context) {
//        val nextHourTime = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
//        val zoneId = ZoneId.systemDefault()
//        val nextHourMillis = nextHourTime.atZone(zoneId).toInstant().toEpochMilli()
//        getSharedPreferences(context).edit().putLong(LAST_UPDATE_TIME_KEY, nextHourMillis).apply()
//    }
//
//    // 마지막 API 호출 시간을 로드
//    fun getLastApiCallTime(context: Context): Long {
//        return getSharedPreferences(context).getLong(LAST_UPDATE_TIME_KEY, 0)
//    }
//
//}