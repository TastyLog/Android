package com.adam.tastylog.repository

import com.adam.tastylog.model.RealTimeSearchTermModel
import com.adam.tastylog.model.RestaurantModel
import com.adam.tastylog.response.RestaurantResponse
import com.adam.tastylog.useCase.GetRealTimeSearchTermsUseCase
import com.adam.tastylog.useCase.GetRestaurantListUseCase

class SearchTermRepository(private val getRealTimeSearchTermsUseCase: GetRealTimeSearchTermsUseCase) {
    suspend fun getRealTimeSearchTerms(): List<RealTimeSearchTermModel> {
        return getRealTimeSearchTermsUseCase.execute()
    }
}