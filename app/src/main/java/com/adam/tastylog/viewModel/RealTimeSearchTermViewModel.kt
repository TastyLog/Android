package com.adam.tastylog.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.adam.tastylog.model.RealTimeSearchTermModel
import com.adam.tastylog.repository.SearchTermRepository
import com.adam.tastylog.utils.UpdateSearchTermWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class RealTimeSearchTermViewModel(application: Application) : AndroidViewModel(application) {

    private val _realTimeSearchTerms = MutableLiveData<List<RealTimeSearchTermModel>>()
    val realTimeSearchTerms: LiveData<List<RealTimeSearchTermModel>> = _realTimeSearchTerms

    private val _lastUpdateTime = MutableLiveData<String>()
    val lastUpdateTime: LiveData<String> = _lastUpdateTime

    init {
        loadRealTimeSearchTermsFromPreferences()
    }

    fun loadRealTimeSearchTermsFromPreferences() {
        Log.d("RealTimeSearchTermVM", "Loading real time search terms from preferences")

        val sharedPreferences = getApplication<Application>().getSharedPreferences("AppData", Context.MODE_PRIVATE)
        val realTimeSearchTermsJson = sharedPreferences.getString("realTimeSearchTerms", null)
        if (realTimeSearchTermsJson != null) {
            val gson = Gson()
            val type = object : TypeToken<List<RealTimeSearchTermModel>>() {}.type
            val realTimeSearchTerms: List<RealTimeSearchTermModel> = gson.fromJson(realTimeSearchTermsJson, type)
            _realTimeSearchTerms.value = realTimeSearchTerms

            val lastUpdateTimeMillis = sharedPreferences.getLong("lastUpdateTime", 0L)
            if (lastUpdateTimeMillis > 0) {
                val lastUpdateTime = Instant.ofEpochMilli(lastUpdateTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("HH:00 기준 업데이트")
                val formattedTime = lastUpdateTime.format(formatter)
                _lastUpdateTime.value = formattedTime
            }
        }
    }


    // 사용자가 수동으로 데이터를 새로고침하고 싶을 때
//    fun refreshRealTimeSearchTerms() {
//        // WorkManager를 사용하여 데이터 갱신 작업을 예약
//        //백그라운드에서 API를 호출하고 결과를 SharedPreferences에 저장
//        val workRequest = OneTimeWorkRequestBuilder<UpdateSearchTermWorker>().build()
//        WorkManager.getInstance(getApplication()).enqueue(workRequest)
//    }
}


