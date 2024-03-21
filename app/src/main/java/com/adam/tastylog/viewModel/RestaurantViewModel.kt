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

    // 현재 검색 쿼리, 정렬 옵션, 선택된 유튜버 ID 저장
    private var currentSearchWord: String? = null
    private var currentSorting: String = ""
    var currentYoutuberIds: List<Int> = emptyList()

    var currentPage = 0
    private val pageSize = 40 // 페이지당 아이템 수
    var isLastPage = false
    var isLoading = false

    fun resetPaging() {
        currentPage = 0
        isLastPage = false
    }

    fun applyNewFilter(latitude: Double, longitude: Double, sorting: String, youtuberIds: List<Int>, searchWord: String? = currentSearchWord) {
        currentSorting = sorting
        currentYoutuberIds = youtuberIds
        if (searchWord != null) {
            currentSearchWord = searchWord
        }
        resetPaging()
        searchRestaurants(latitude, longitude, currentSearchWord, sorting, youtuberIds)
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
        page: Int = currentPage
    ) {
//        currentPage = 0

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

                isLastPage = newRestaurants.size < pageSize
                currentPage++
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

    fun searchRestaurants(
        latitude: Double,
        longitude: Double,
        searchWord: String? = currentSearchWord, // 기본값으로 현재 검색어 사용
        sorting: String = currentSorting, // 기본값으로 현재 정렬 옵션 사용
        youtuberIds: List<Int>,
        page: Int = currentPage // 기본값으로 현재 페이지 사용
    ) {
        // 로딩 중이거나 마지막 페이지에 도달했으면 더 이상 로드하지 않음
        if (isLoading || isLastPage) return
        isLoading = true

        viewModelScope.launch {
            try {
                // 검색어, 정렬 옵션, 유튜버 ID를 사용하여 필터링된 검색 결과를 요청
                val response = restaurantRepository.getSearchRestaurantListWithFilters(
                    latitude, longitude, searchWord.orEmpty(), sorting, youtuberIds.joinToString(","), page, pageSize
                )

                // 검색 결과 처리
                val newRestaurants = response.content
                val currentRestaurants = if (page == 0) emptyList() else _restaurants.value ?: emptyList()
                _restaurants.postValue(currentRestaurants + newRestaurants)
                isLastPage = newRestaurants.size < pageSize
                currentPage++

                if (page == 0 && newRestaurants.size <= 4) {
                    _showShimmer.postValue(false)
                }
            } catch (e: Exception) {
                _showShimmer.postValue(false)
            } finally {
                isLoading = false
            }
        }
    }
    // ViewModel 내부에 검색 결과를 초기화하는 함수 추가
    fun resetSearchResults() {
        currentPage = 0
        isLastPage = false
        _restaurants.postValue(emptyList()) // LiveData를 비워 초기 상태로 리셋
    }

//
//    //선택된 유튜버 ID 목록을 업데이트
//    fun updateSelectedYoutuberIds(youtuberIds: List<Int>) {
//        currentYoutuberIds = youtuberIds
//        // 필요한 경우 추가적인 로직을 여기에 구현
//    }
//

    fun getCurrentSearchWord(): String? {
        return currentSearchWord
    }


}