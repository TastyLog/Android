package com.adam.tastylog.response

import com.adam.tastylog.data.RestaurantData
import com.adam.tastylog.model.RealTimeSearchTermModel

data class RealTimeSearchTermResponse(
    val code: Int,
    val message: String,
    val data: List<RealTimeSearchTermModel>
)