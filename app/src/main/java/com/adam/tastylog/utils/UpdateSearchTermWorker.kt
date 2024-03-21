package com.adam.tastylog.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.adam.tastylog.model.RealTimeSearchTermModel
import com.adam.tastylog.repository.SearchTermRepository
import com.adam.tastylog.useCase.GetRealTimeSearchTermsUseCase
import com.google.gson.Gson

//class UpdateSearchTermWorker(appContext: Context, workerParams: WorkerParameters):
//    CoroutineWorker(appContext, workerParams) {
//
//    override suspend fun doWork(): Result {
//        return try {
//            SharedPreferencesManager.updateLastApiCallTime(applicationContext)
//
//            Result.success()
//        } catch (e: Exception) {
//            Result.retry()
//        }
//    }
//}

class UpdateSearchTermWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val searchTermRepository = SearchTermRepository(GetRealTimeSearchTermsUseCase(RetrofitBuilder.restaurantService))

    override suspend fun doWork(): Result {
        Log.d("UpdateSearchTermWorker", "Work started")
        return try {
            Log.d("UpdateSearchTermWorker", "Fetching real time search terms")
            val realTimeSearchTerms = searchTermRepository.getRealTimeSearchTerms()
            Log.d("UpdateSearchTermWorker", "Fetched ${realTimeSearchTerms.size} terms")

            saveRealTimeSearchTerms(realTimeSearchTerms)
            Log.d("UpdateSearchTermWorker", "Real time search terms saved")

            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateSearchTermWorker", "Error during work execution", e)
            Result.retry()
        }
    }



    private fun saveRealTimeSearchTerms(realTimeSearchTerms: List<RealTimeSearchTermModel>) {
        // SharedPreferences에 JSON 형식으로 저장하기 위해 Gson 라이브러리를 사용
        val gson = Gson()
        val realTimeSearchTermsJson = gson.toJson(realTimeSearchTerms)

        val sharedPreferences = applicationContext.getSharedPreferences("AppData", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("realTimeSearchTerms", realTimeSearchTermsJson)
            putLong("lastUpdateTime", System.currentTimeMillis())
            apply()
        }
    }
}
