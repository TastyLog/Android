package com.adam.tastylog.useCase

import com.adam.tastylog.response.RestaurantResponse
import com.adam.tastylog.service.RestaurantService

class GetRestaurantListUseCase(private val restaurantService: RestaurantService) {

    suspend fun executeAll(latitude: Double, longitude: Double): RestaurantResponse {
        // API 호출 및 결과 처리
        val response = restaurantService.getRestaurantList(latitude, longitude)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("No data available")
        } else {
            throw Exception("API call failed with error: ${response.errorBody()}")
        }
    }

    // 유튜버 ID 기반 필터링된 데이터 요청을 처리하는 메소드
    suspend fun executeFilteredByYoutuber(
        latitude: Double,
        longitude: Double,
        youtuberIds: String,
        page: Int,
        size: Int,
        sorting: String
    ): RestaurantResponse {
        val response = restaurantService.getRestaurantListFilteredByYoutuber(
            latitude,
            longitude,
            youtuberIds,
            page,
            size,
            sorting
        )
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("No data available")
        } else {
            throw Exception("API call failed with error: ${response.errorBody()}")
        }
    }

    // 정렬 및 유튜버 ID 기반 필터링된 데이터 요청을 처리하는 메소드
    suspend fun executeSortedFiltered(
        latitude: Double,
        longitude: Double,
        sorting: String,
        youtuberIds: String,
        page: Int,
        size: Int
    ): RestaurantResponse {
        val response = restaurantService.getSortedFilteredRestaurantList(
            latitude,
            longitude,
            sorting,
            youtuberIds,
            page,
            size
        )
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("No data available")
        } else {
            throw Exception("API call failed with error: ${response.errorBody()}")
        }
    }
}