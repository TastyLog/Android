package com.adam.tastylog.model

import java.io.Serializable

data class RestaurantModel(
    val uniqueKey: String,
    val restaurantName: String,
    val category: String,
    val phoneNumber: String,
    val address: String,
    val youtuberProfile: String,
    val youtuberName: String,
    val youtuberLink: String,
    val latitude: Double,
    val longitude: Double,
    val naverLink: String,
    val totalReviews: Int,
    val rating: Double,
    val representativeImage: String,
    val youtuberId: Int,
    val channelId: String,
    val distance: String // 거리 정보
) : Serializable