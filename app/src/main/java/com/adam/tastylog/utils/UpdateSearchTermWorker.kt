package com.adam.tastylog

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.adam.tastylog.repository.SearchTermRepository
import com.adam.tastylog.useCase.GetRealTimeSearchTermsUseCase
import com.adam.tastylog.utils.RetrofitBuilder
import com.adam.tastylog.utils.SharedPreferencesManager

class UpdateSearchTermWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = SearchTermRepository(GetRealTimeSearchTermsUseCase(RetrofitBuilder.restaurantService))
        try {
            val terms = repository.getRealTimeSearchTerms()
            SharedPreferencesManager.saveSearchTerms(applicationContext, terms)
            SharedPreferencesManager.updateLastApiCallTime(applicationContext)
            // 여기에 더 많은 로직을 추가할 수 있습니다. 예를 들어, 로컬 데이터베이스 업데이트
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
