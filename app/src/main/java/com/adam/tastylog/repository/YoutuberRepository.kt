package com.adam.tastylog.repository

import com.adam.tastylog.model.YoutuberModel
import com.adam.tastylog.useCase.GetYoutuberListUseCase

class YoutuberRepository(private val getYoutuberListUseCase: GetYoutuberListUseCase) {

    suspend fun getYoutuberList(): List<YoutuberModel> {
        return getYoutuberListUseCase.execute()
    }
}