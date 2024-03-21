package com.adam.tastylog.useCase

import com.adam.tastylog.model.RealTimeSearchTermModel
import com.adam.tastylog.response.RestaurantResponse
import com.adam.tastylog.service.RestaurantService

class GetRealTimeSearchTermsUseCase(private val restaurantService: RestaurantService) {
    suspend fun execute(): List<RealTimeSearchTermModel> {
        val response = restaurantService.getRealTimeSearchTerms()
        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("API call failed with error: ${response.errorBody()?.string()}")
        }
    }
}
