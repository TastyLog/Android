package com.adam.tastylog.repository

import com.adam.tastylog.data.RestaurantData
import com.adam.tastylog.model.RestaurantModel
import com.adam.tastylog.useCase.GetRestaurantListUseCase

class RestaurantRepository(private val getRestaurantListUseCase: GetRestaurantListUseCase) {

    suspend fun getAllRestaurantList(latitude: Double, longitude: Double): List<RestaurantModel> {
        // UseCase를 사용하여 데이터 가져오기
        val restaurantResponse = getRestaurantListUseCase.executeAll(latitude, longitude)
        // 데이터 변환 및 반환
        return restaurantResponse.data.content.map { mapToRestaurantModel(it) }
    }

    // 유튜버 ID 기반 필터링된 식당 목록 가져오는 함수
    suspend fun getRestaurantListFilteredByYoutuber(latitude: Double, longitude: Double, youtuberIds: String, page: Int, size: Int, sorting: String): RestaurantData {
        val restaurantResponse = getRestaurantListUseCase.executeFilteredByYoutuber(latitude, longitude, youtuberIds, page, size, sorting)
        return restaurantResponse.data // 여기서 RestaurantData 객체 반환
    }

    // 정렬 및 유튜버 ID 기반 필터링된 식당 목록을 가져오는 함수
    suspend fun getSortedFilteredRestaurantList(latitude: Double, longitude: Double, sorting: String, youtuberIds: String, page: Int, size: Int): RestaurantData {
        val restaurantResponse = getRestaurantListUseCase.executeSortedFiltered(latitude, longitude, sorting, youtuberIds, page, size)
        return restaurantResponse.data // RestaurantData 객체 반환
    }

    private fun mapToRestaurantModel(restaurantData: RestaurantModel): RestaurantModel {
        return RestaurantModel(
            uniqueKey = restaurantData.uniqueKey,
            restaurantName = restaurantData.restaurantName,
            category = restaurantData.category,
            phoneNumber = restaurantData.phoneNumber,
            address = restaurantData.address,
            youtuberProfile = restaurantData.youtuberProfile,
            youtuberName = restaurantData.youtuberName,
            youtuberLink = restaurantData.youtuberLink,
            latitude = restaurantData.latitude,
            longitude = restaurantData.longitude,
            naverLink = restaurantData.naverLink,
            totalReviews = restaurantData.totalReviews,
            rating = restaurantData.rating,
            representativeImage = restaurantData.representativeImage,
            youtuberId = restaurantData.youtuberId,
            channelId = restaurantData.channelId,
            distance = restaurantData.distance
        )
    }
}
