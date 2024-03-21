package com.adam.tastylog.service

import com.adam.tastylog.response.RealTimeSearchTermResponse
import com.adam.tastylog.response.RestaurantResponse
import com.adam.tastylog.response.YoutuberResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantService {
    @GET("/api/v1/food/{latitude}/{longitude}")
    suspend fun getRestaurantList(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("page") page: Int = 0, // 기본값을 0으로 설정
        @Query("size") size: Int = 1000 // 기본값을 1000으로 설정
    ): Response<RestaurantResponse>


    @GET("/api/v1/food/{latitude}/{longitude}")
    suspend fun getSortedFilteredRestaurantList(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("sort") sort: String,
        @Query("youtuberId") youtuberIds: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 40
    ): Response<RestaurantResponse>



    @GET("/api/v1/youtuber/all-list")
    suspend fun getYoutuberList(): Response<YoutuberResponse>

    // 검색 기능을 정렬 옵션 및 유튜버 ID와 함께 처리하는 API
    @GET("/api/v1/food/{latitude}/{longitude}")
    suspend fun getSearchRestaurantListWithFilters(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("searchWord") searchWord: String,
        @Query("sort") sort: String,
        @Query("youtuberId") youtuberId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 40
    ): Response<RestaurantResponse>

    @GET("/api/v1/food/search/rank")
    suspend fun getRealTimeSearchTerms(): Response<RealTimeSearchTermResponse>

}
