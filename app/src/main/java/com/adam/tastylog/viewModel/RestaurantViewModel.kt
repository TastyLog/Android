package com.adam.tastylog.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.tastylog.model.RestaurantModel
import com.adam.tastylog.repository.RestaurantRepository
import kotlinx.coroutines.launch

class RestaurantViewModel(private val restaurantRepository: RestaurantRepository) : ViewModel() {
    private val _restaurants = MutableLiveData<List<RestaurantModel>>()
    private val _showShimmer = MutableLiveData<Boolean>()

    val showShimmer: LiveData<Boolean> = _showShimmer
    val restaurants: LiveData<List<RestaurantModel>> = _restaurants

    var currentPage = 0
    private val pageSize = 40 // 페이지당 아이템 수
    var isLastPage = false
    var isLoading = false

    fun resetPaging() {
        currentPage = 0
        isLastPage = false
    }

    fun applyNewFilter(
        latitude: Double,
        longitude: Double,
        sorting: String,
        youtuberIds: List<Int>
    ) {
        resetPaging()
        getRestaurantsSorted(latitude, longitude, sorting, youtuberIds, currentPage)
    }

    fun getAllRestaurants(latitude: Double, longitude: Double) {
        if (isLoading) return
        isLoading = true
        currentPage = 0
        viewModelScope.launch {
            try {
                val restaurantList = restaurantRepository.getAllRestaurantList(latitude, longitude)
                _restaurants.postValue(restaurantList)
                isLastPage = restaurantList.size < pageSize
                isLoading = false
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error fetching restaurants: ${e.message}")
                isLoading = false
            }
        }
    }

    fun getRestaurantsSorted(
        latitude: Double,
        longitude: Double,
        sorting: String,
        youtuberIds: List<Int>,
        page: Int = 0
    ) {
        if (isLastPage || isLoading) return
        isLoading = true
        val youtuberIdString = youtuberIds.joinToString(",")

        if (page == 0) {
            _restaurants.postValue(emptyList())
            _showShimmer.postValue(true) // 페이지 0에서 로딩 시작 시 shimmer 표시
        }

        viewModelScope.launch {
            try {
                val sortedResponse = restaurantRepository.getSortedFilteredRestaurantList(
                    latitude,
                    longitude,
                    sorting,
                    youtuberIdString,
                    page,
                    pageSize
                )
                val newRestaurants = sortedResponse.content
                val currentRestaurants = _restaurants.value ?: emptyList()
                _restaurants.postValue(currentRestaurants + newRestaurants)

                isLastPage = page >= sortedResponse.totalPages
                currentPage = page + 1

                // 첫 페이지이고 로드된 데이터가 4개 이하일 경우 shimmer 숨기기
                if (page == 0 && newRestaurants.size <= 4) {
                    _showShimmer.postValue(false)
                }

                isLoading = false
            } catch (e: Exception) {
                Log.e(
                    "RestaurantViewModel",
                    "Error fetching sorted and filtered restaurants: ${e.message}"
                )
                _showShimmer.postValue(false) // 오류 발생 시 shimmer 숨기기
                isLoading = false
            }
        }
    }

    fun getRestaurantsFilteredByYoutuber(
        latitude: Double,
        longitude: Double,
        youtuberIds: List<Int>,
        page: Int = 0,
        sorting: String
    ) {

        if (isLastPage or isLoading) return
        currentPage = 0 // 페이지 번호 초기화

        isLoading = true
        val youtuberIdString = youtuberIds.joinToString(",")

        viewModelScope.launch {
            try {
                val response = restaurantRepository.getRestaurantListFilteredByYoutuber(
                    latitude,
                    longitude,
                    youtuberIdString,
                    page,
                    pageSize,
                    sorting
                )
                val newRestaurants = response.content
                val currentRestaurants = _restaurants.value ?: emptyList()
                _restaurants.postValue(currentRestaurants + newRestaurants)

                isLastPage = page >= response.totalPages
                currentPage = page + 1
                isLoading = false
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error fetching filtered restaurants: ${e.message}")
                isLoading = false
            }
        }
    }
}
