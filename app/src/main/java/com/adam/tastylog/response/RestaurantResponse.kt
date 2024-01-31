package com.adam.tastylog.response

import com.adam.tastylog.data.RestaurantData

data class RestaurantResponse(
    val code: Int,
    val message: String,
    val data: RestaurantData
)