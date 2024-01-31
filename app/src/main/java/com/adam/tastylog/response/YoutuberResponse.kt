package com.adam.tastylog.response

import com.adam.tastylog.model.YoutuberModel


data class YoutuberResponse(
    val code: Int,
    val message: String,
    val data: List<YoutuberModel>
)