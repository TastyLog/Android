package com.adam.tastylog.model

data class KeyWordRankModel(
    val rank: Int,
    val keyword: String,
    val change: String // "+1", "-1", "NEW" 등의 변동값을 문자열로 저장
)
