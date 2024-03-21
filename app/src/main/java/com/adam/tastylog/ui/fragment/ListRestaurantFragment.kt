package com.adam.tastylog.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R
import com.adam.tastylog.ui.activity.SearchRestaurantActivity
import com.adam.tastylog.databinding.FragmentListRestaurantBinding
import com.adam.tastylog.model.RestaurantModel
import com.adam.tastylog.model.YoutuberModel
import com.adam.tastylog.repository.RestaurantRepository
import com.adam.tastylog.repository.YoutuberRepository
import com.adam.tastylog.ui.adapter.ListRestaurantAdapter
import com.adam.tastylog.ui.adapter.ListYoutuberChipAdapter
import com.adam.tastylog.useCase.GetRestaurantListUseCase
import com.adam.tastylog.useCase.GetYoutuberListUseCase
import com.adam.tastylog.utils.RetrofitBuilder
import com.adam.tastylog.viewModel.RestaurantViewModel
import com.adam.tastylog.viewModel.RestaurantViewModelFactory
import com.adam.tastylog.viewModel.YoutuberViewModel
import com.adam.tastylog.viewModel.YoutuberViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ListRestaurantFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentListRestaurantBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentSortOption: String = "거리순" // 기본값으로 "거리순" 설정
    private lateinit var shimmerLayout: ShimmerFrameLayout
//    private var selectedYoutubers = mutableSetOf<String>()
//    private var youtuberNameToIdMap = mapOf<String, Int>()

    private val restaurantViewModel: RestaurantViewModel by activityViewModels {
        RestaurantViewModelFactory(RetrofitBuilder.restaurantService.let { restaurantService ->
            RestaurantRepository(GetRestaurantListUseCase(restaurantService))
        })
    }

//    private lateinit var youtuberViewModel: YoutuberViewModel

    private lateinit var listRestaurantAdapter: ListRestaurantAdapter

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentSearchQuery: String? = null


    companion object {
        const val SEARCH_REQUEST_CODE = 100 // 예시 요청 코드
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModel 초기화
//        val restaurantService = RetrofitBuilder.restaurantService
//        val getYoutuberListUseCase = GetYoutuberListUseCase(restaurantService)
//        val youtuberRepository = YoutuberRepository(getYoutuberListUseCase)
//        youtuberViewModel = ViewModelProvider(this,YoutuberViewModelFactory(youtuberRepository)).get(YoutuberViewModel::class.java)
//        youtuberViewModel.getYoutubers()


        // Adapter 인스턴스 초기화
        listRestaurantAdapter = ListRestaurantAdapter(listOf())

        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentListRestaurantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        getCurrentLocationAndFetchSortedByDistanceRestaurants()
        updateCurrentLocation() // 현재 위치 정보 업데이트
        setupEditTextSearchListener()

    }
    override fun onStart() {
        super.onStart()
        val bottomSheetDialog = dialog as BottomSheetDialog?
        val bottomSheet = bottomSheetDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val layoutParams = bottomSheet?.layoutParams
        layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheet?.layoutParams = layoutParams

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isDraggable = false
    }


    private fun setupUI() {
        val backButton = binding.frameLayoutBackButtonFrame
        val autoCompleteTextView = binding.autoComplete
        val items = listOf("거리순", "평점순", "리뷰순")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_fillter, items)
        autoCompleteTextView.setAdapter(adapter)

        // 드릅다운 너비 설정
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        autoCompleteTextView.dropDownWidth = width

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val itemSelected = adapter.getItem(position)
            if (currentSortOption != itemSelected) {
                if (itemSelected != null) {
                    currentSortOption = itemSelected
//                    listYoutuberAdapter.notifyDataSetChanged()
                    showTravelData()

                    when (itemSelected) {
                        "거리순" -> getCurrentLocationAndFetchSortedByDistanceRestaurants()
                        "평점순" -> getCurrentLocationAndFetchSortedByRatingRestaurants()
                        "리뷰순" -> getCurrentLocationAndFetchSortedByReviewsRestaurants()
                    }

                    // 필터링 변경 시, ViewModel의 applyNewFilter 함수 호출
                    applyNewFilter()
                }
            }
        }


        val restaurantRecyclerView = binding.recyclerviewListRestaurant
        restaurantRecyclerView.adapter = listRestaurantAdapter
        restaurantRecyclerView.layoutManager = LinearLayoutManager(context)

        restaurantRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // 전체 아이템의 75% 지점에 도달했는지 확인
                val loadMoreThreshold = (totalItemCount * 0.75).toInt()
                if ((visibleItemCount + firstVisibleItemPosition) >= loadMoreThreshold && firstVisibleItemPosition >= 0) {
                    Log.d("ListRestaurantFragment", "Load more items threshold reached")
                    loadMoreItems()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 스크롤이 멈추면 BottomSheet 닫힘 허용
                    dialog?.setCancelable(true)
                }
            }
        })

        restaurantViewModel.restaurants.observe(viewLifecycleOwner, Observer { restaurants ->
            // 데이터 업데이트
            listRestaurantAdapter.updateRestaurantData(restaurants)
            hideTravelData()
        })


        // ViewModel의 showShimmer 상태를 관찰
        restaurantViewModel.showShimmer.observe(viewLifecycleOwner) { show ->
            if (show) {
                showTravelData() // Shimmer 시작
            } else {
                hideTravelData() // Shimmer 중단
            }
        }

        backButton.setOnClickListener { dismiss() }
        shimmerLayout = binding.shimmerLayout


        // TextInputLayout 클릭 리스너 설정
        binding.textviewYoutuberFilter.setOnClickListener {
            // YoutuberSelectionFragment 표시 로직
            val fragment = YoutuberSelectionFragment() // YoutuberSelectionFragment 인스턴스 생성
            fragment.show(parentFragmentManager, fragment.tag) // Fragment 표시
        }
    }


    private fun loadMoreItems() {
        if (!restaurantViewModel.isLastPage && !restaurantViewModel.isLoading) {
            // 현재 검색어가 있다면 검색 모드로 다음 페이지 데이터 요청
            if (!currentSearchQuery.isNullOrEmpty()) {
                searchMoreData()
            } else {
                // 검색어가 없다면 일반 데이터 로드 모드로 다음 페이지 데이터 요청

                getCurrentLocationAndLoadMore()
            }
        }
    }
    private fun searchMoreData() {
        currentLatitude?.let { lat ->
            currentLongitude?.let { lng ->
                val nextPage = restaurantViewModel.currentPage
                val sortOption = getApiSortOption(currentSortOption)
                val youtuberIds = restaurantViewModel.currentYoutuberIds

                restaurantViewModel.searchRestaurants(lat, lng, currentSearchQuery, sortOption, youtuberIds, nextPage)
            }
        }
    }
    private fun getCurrentLocationAndLoadMore() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val sortOption = getApiSortOption(currentSortOption)
                    val youtuberIds = restaurantViewModel.currentYoutuberIds


                    if (youtuberIds.isNotEmpty()) {
                        restaurantViewModel.getRestaurantsSorted(latitude, longitude, sortOption, youtuberIds, restaurantViewModel.currentPage)
                    } else {
                        restaurantViewModel.getRestaurantsSorted(latitude, longitude, sortOption, emptyList(), restaurantViewModel.currentPage)
                    }
                } ?: run {
                    Log.e("ListRestaurantFragment", "Location information is not available.")
                }
            }
        } else {
            Log.e("ListRestaurantFragment", "Location permission not granted")
        }
    }


    private fun getCurrentLocationAndFetchSortedByDistanceRestaurants() {
        getCurrentLocationAndFetchSortedRestaurants("")
    }
    private fun getCurrentLocationAndFetchSortedByReviewsRestaurants() {
        getCurrentLocationAndFetchSortedRestaurants("totalReview,desc")
    }
    private fun getCurrentLocationAndFetchSortedByRatingRestaurants() {
        getCurrentLocationAndFetchSortedRestaurants("rating,desc")
    }



    private fun getCurrentLocationAndFetchSortedRestaurants(sortOption: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val youtuberIds = restaurantViewModel.currentYoutuberIds

                    // 유튜버 필터링이 있는 경우에도 sortOption 유지
                    val finalSortOption = if (youtuberIds.isNotEmpty()) sortOption else sortOption
                    restaurantViewModel.currentPage = 0
                    restaurantViewModel.getRestaurantsSorted(latitude, longitude, finalSortOption, youtuberIds, restaurantViewModel.currentPage)
                } ?: run {
                    Log.e("ListRestaurantFragment", "Location information is not available.")
                }
            }
        }
    }

    private fun getApiSortOption(userFriendlySortOption: String): String {
        return when (userFriendlySortOption) {
            "거리순" -> ""
            "평점순" -> "rating,desc"
            "리뷰순" -> "totalReview,desc"
            else -> userFriendlySortOption
        }
    }

    // Shimmer를 표시하는 함수
    private fun showTravelData() {
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        binding.recyclerviewListRestaurant.visibility = View.GONE
    }
    // Shimmer를 숨기는 함수
    private fun hideTravelData() {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        binding.recyclerviewListRestaurant.visibility = View.VISIBLE
    }

//    private fun applyNewFilter() {
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//                location?.let {
//                    val latitude = it.latitude
//                    val longitude = it.longitude
//                    val sortOption = getApiSortOption(currentSortOption)
//                    val youtuberIds = restaurantViewModel.currentYoutuberIds
//                    val searchQuery = binding.editTextSearch.text.toString() // EditText에서 검색어를 가져옴
//
//                    // 현재 검색어도 포함하여 ViewModel의 applyNewFilter 함수 호출
//                    restaurantViewModel.applyNewFilter(latitude, longitude, sortOption, youtuberIds, searchQuery)
//
//                }
//            }
//        }
//    }
        private fun applyNewFilter() {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        val sortOption = getApiSortOption(currentSortOption)
                        val youtuberIds = restaurantViewModel.currentYoutuberIds
//                        val searchQuery = binding.editTextSearchFragment.text.toString() // EditText에서 검색어를 가져옴

                        // Apply new filter with the current search word, sorting option, and youtuber IDs
                        restaurantViewModel.applyNewFilter(latitude, longitude, sortOption, youtuberIds, "")
                    }
                }
            }
        }
    private fun setupEditTextSearchListener() {
        val editTextSearch = view?.findViewById<EditText>(R.id.editTextSearch)
        // EditText 클릭 시 포커스를 받지 않도록 설정
        editTextSearch?.isFocusableInTouchMode = false
        editTextSearch?.isFocusable = false
        editTextSearch?.setOnClickListener {
            // SearchActivity를 시작하는 인텐트 생성
            val intent = Intent(context, SearchRestaurantActivity::class.java)
            startActivityForResult(intent, SEARCH_REQUEST_CODE)
        }
    }



    // 검색 결과를 받기 위한 onActivityResult 메소드 오버라이드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val recyclerViewScroll = view?.findViewById<RecyclerView>(R.id.recyclerview_list_restaurant)
        if (requestCode == SEARCH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra("search_query")?.let { searchQuery ->
                if (searchQuery.isNotEmpty()) {
                    view?.findViewById<EditText>(R.id.editTextSearch)?.setText(searchQuery) // 추가
                    currentSearchQuery = searchQuery
                    // 새 검색을 시작하기 전에 기존 검색 결과를 초기화
                    restaurantViewModel.resetSearchResults()
                    recyclerViewScroll?.scrollToPosition(0) // 스크롤 좌표 초기화
                    currentLatitude?.let { lat ->
                        currentLongitude?.let { lng ->
                            // 새 검색어로 검색 실행
                            val sortOption = getApiSortOption(currentSortOption)
                            val youtuberIds = restaurantViewModel.currentYoutuberIds
                            restaurantViewModel.searchRestaurants(lat, lng, currentSearchQuery, sortOption, youtuberIds)
                        }
                    }
                }
            }
        }
    }

//    // 검색 결과를 받기 위한 onActivityResult 메소드 오버라이드
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == SEARCH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            data?.getStringExtra("search_query")?.let { searchQuery ->
//                if (searchQuery.isNotEmpty()) {
//                    // Update the search word in ViewModel
//                    restaurantViewModel.updateSearchWord(searchQuery)
//                    applyNewFilter()
//                }
//            }
//        }
//    }

    private fun updateCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                }
            }
        }
    }
}