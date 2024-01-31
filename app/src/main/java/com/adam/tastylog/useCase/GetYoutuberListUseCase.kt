package com.adam.tastylog.useCase

import com.adam.tastylog.model.YoutuberModel
import com.adam.tastylog.service.RestaurantService

class GetYoutuberListUseCase(private val restaurantService: RestaurantService) {
    suspend fun execute(): List<YoutuberModel> {
        val response = restaurantService.getYoutuberList()
        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("No data available")
        } else {
            throw Exception("API call failed with error: ${response.errorBody()}")
        }
    }
}