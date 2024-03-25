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
    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentSortOption: String = "거리순" // 기본값으로 "거리순" 설정
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private var selectedYoutubers = mutableSetOf<String>()
    private var youtuberNameToIdMap = mapOf<String, Int>()

    private lateinit var listYoutuberAdapter: ListYoutuberChipAdapter
    private lateinit var youtuberRecyclerView: RecyclerView
    private lateinit var youtuberViewModel: YoutuberViewModel

    private lateinit var listRestaurantAdapter: ListRestaurantAdapter

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentSearchQuery: String? = null


    private var lastClickTime = 0L // 이전에 클릭한 시간을 저장하는 변수
    private val clickDelay = 500L // 클릭 딜레이 (밀리초)

    companion object {
        const val SEARCH_REQUEST_CODE = 100 // 예시 요청 코드
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModel 초기화
        val restaurantService = RetrofitBuilder.restaurantService
        val getRestaurantListUseCase = GetRestaurantListUseCase(restaurantService)
        val restaurantRepository = RestaurantRepository(getRestaurantListUseCase)
        restaurantViewModel = ViewModelProvider(this, RestaurantViewModelFactory(restaurantRepository)).get(RestaurantViewModel::class.java)


        val getYoutuberListUseCase = GetYoutuberListUseCase(restaurantService)
        val youtuberRepository = YoutuberRepository(getYoutuberListUseCase)
        youtuberViewModel = ViewModelProvider(this,YoutuberViewModelFactory(youtuberRepository)).get(YoutuberViewModel::class.java)
        youtuberViewModel.getYoutubers()


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
        setupYoutuberFilteringUI()
        getCurrentLocationAndFetchSortedByDistanceRestaurants()
        updateCurrentLocation() // 현재 위치 정보 업데이트
//        setupEditTextSearch()
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
                    listYoutuberAdapter.notifyDataSetChanged()
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

                // 스크롤 중 BottomSheet 닫힘 방지
//                dialog?.setCancelable(false)

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
            listRestaurantAdapter.updateRestaurantData(filterRestaurantsByYoutuber(restaurants))
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
                val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }.toList()

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
                    val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }


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
                    val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }

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


    private fun handleYoutuberSelectionWithDelay(youtuber: YoutuberModel) {
        val currentTime = System.currentTimeMillis() // 현재 시간을 가져오기.
        if (currentTime - lastClickTime >= clickDelay) { // 현재 시간과 마지막 클릭 시간의 차이가 클릭 딜레이보다 크거나 같은 경우에만 클릭 이벤트를 처리
            handleYoutuberSelection(youtuber)
            lastClickTime = currentTime // 마지막 클릭 시간을 업데이트.
        }
    }


    private fun filterRestaurantsByYoutuber(restaurants: List<RestaurantModel>): List<RestaurantModel> {
        return if (selectedYoutubers.isEmpty()) {
            restaurants
        } else {
            restaurants.filter { it.youtuberName in selectedYoutubers }
        }
    }

    // 추가적으로 유튜버 필터링 UI 구성 및 리스너 설정
    private fun setupYoutuberFilteringUI() {
        youtuberRecyclerView = binding.recyclerviewYoutuberChip
//        listYoutuberAdapter = ListYoutuberChipAdapter(listOf(), selectedYoutubers) { youtuber ->
//            handleYoutuberSelection(youtuber)
//        }
        listYoutuberAdapter = ListYoutuberChipAdapter(listOf(), selectedYoutubers) { youtuber ->
            handleYoutuberSelectionWithDelay(youtuber)
        }
        youtuberRecyclerView.adapter = listYoutuberAdapter
        youtuberRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // 유튜버 이름과 ID를 매핑
        youtuberViewModel.youtubers.observe(viewLifecycleOwner, Observer { youtubers ->
            val allYoutubers = listOf(
                YoutuberModel("전체", "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/allImage.png", 0, "")
            ) + youtubers

            youtuberNameToIdMap = youtubers.associate { it.youtuberName to it.youtuberId }
            listYoutuberAdapter.updateYoutuberData(allYoutubers)
        })
    }
    private fun handleYoutuberSelection(youtuber: YoutuberModel) {
        if (youtuber.youtuberName == "전체") {
            selectedYoutubers.clear() // 유튜버 선택 초기화
        } else {
            if (selectedYoutubers.contains(youtuber.youtuberName)) {
                selectedYoutubers.remove(youtuber.youtuberName)
            } else {
                selectedYoutubers.add(youtuber.youtuberName)
            }
        }
        listYoutuberAdapter.notifyDataSetChanged()
        applyNewFilter() // 필터링 적용
    }
    private fun applyNewFilter() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val sortOption = getApiSortOption(currentSortOption)
                    val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }.toList()
                    val searchQuery = binding.editTextSearch.text.toString() // EditText에서 검색어를 가져옴

                    // 현재 검색어도 포함하여 ViewModel의 applyNewFilter 함수 호출
                    restaurantViewModel.applyNewFilter(latitude, longitude, sortOption, youtuberIds, searchQuery)

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
                            val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }
                            restaurantViewModel.searchRestaurants(lat, lng, currentSearchQuery, sortOption, youtuberIds)
                        }
                    }
                }
            }
        }
    }


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