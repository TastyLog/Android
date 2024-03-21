package com.adam.tastylog.model

data class RealTimeSearchTermModel(
    val keyword: String,
    var rank: Int,
    var state: String,
)

