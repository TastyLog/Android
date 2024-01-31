package com.adam.tastylog.data

import com.adam.tastylog.model.RestaurantModel

data class RestaurantData(
    val content: List<RestaurantModel>,
    val pageNumber: Int,  // 현재 페이지 번호
    val pageSize: Int,    // 페이지 크기
    val totalElements: Long,  // 전체 요소 수
    val totalPages: Int,  // 전체 페이지 수
    val last: Boolean    // 마지막 페이지 여부
)
