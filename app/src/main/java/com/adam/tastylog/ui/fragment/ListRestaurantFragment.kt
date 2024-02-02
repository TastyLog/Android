package com.adam.tastylog.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
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


        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentListRestaurantBinding.inflate(inflater, container, false)
//        setupBackButtonHandler() // 뒤로 가기 버튼 핸들러 설정
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupYoutuberFilteringUI()
//        getCurrentLocationAndFetchRestaurants()
        getCurrentLocationAndFetchSortedByDistanceRestaurants()
        setBannerAds()


//        // BottomSheetDialogFragment의 Behavior 설정
//        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet as View)
//        bottomSheetBehavior.isDraggable = false // 드래그로 닫히는 것을 방지
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

//    private fun setupBackButtonHandler() {
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            Log.d("ListRestaurantFragment", "Back button pressed")
//            if (this@ListRestaurantFragment.isVisible) {
//                Log.d("ListRestaurantFragment", "Fragment is visible. Dismissing...")
//                this@ListRestaurantFragment.dismiss()
//            } else {
//                Log.d("ListRestaurantFragment", "Fragment is not visible. Default back action.")
//                isEnabled = false
//                requireActivity().onBackPressed()
//            }
//        }
//    }

    private fun setupUI() {
        val backButton = binding.frameLayoutBackButtonFrame
        val autoCompleteTextView = binding.autoComplete
        val items = listOf("거리순", "평점순", "리뷰순")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_fillter, items)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val itemSelected = adapter.getItem(position)
            if (currentSortOption != itemSelected) {
                if (itemSelected != null) {
                    currentSortOption = itemSelected
//                    selectedYoutubers.clear() // 유튜버 선택 초기화
                    listYoutuberAdapter.notifyDataSetChanged()
//                    showTravelData(true)
                    showTravelData()

                    when (itemSelected) {
//                        "거리순" -> getCurrentLocationAndFetchRestaurants()
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
        val listRestaurantAdapter = ListRestaurantAdapter(listOf())
        restaurantRecyclerView.adapter = listRestaurantAdapter
        restaurantRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

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
        // 스크롤이 끝에 도달했을 때만 호출하도록 수정
        if (!restaurantViewModel.isLastPage && !restaurantViewModel.isLoading) {
            getCurrentLocationAndLoadMore()
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


    private fun getCurrentLocationAndLoadMore() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val sortOption = getApiSortOption(currentSortOption)
                    val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }

//                    restaurantViewModel.currentPage++ // 페이지 번호 증가

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


    private fun getCurrentLocationAndFetchRestaurants() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation
            locationResult.addOnSuccessListener { location ->
                location?.let {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    restaurantViewModel.getAllRestaurants(latitude, longitude) // 거리순은 getAllRestaurants 호출
                } ?: run {
                    Log.e("ListRestaurantFragment", "Location information is not available.")
                }
            }
        } else {
            Log.e("ListRestaurantFragment", "Location permission not granted")
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
        listYoutuberAdapter = ListYoutuberChipAdapter(listOf(), selectedYoutubers) { youtuber ->
            handleYoutuberSelection(youtuber)
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

//    private fun handleYoutuberSelection(youtuber: YoutuberModel) {
//        if (youtuber.youtuberName == "전체") {
//            selectedYoutubers.clear() // 유튜버 선택 초기화
//        } else {
//            if (selectedYoutubers.contains(youtuber.youtuberName)) {
//                selectedYoutubers.remove(youtuber.youtuberName)
//            } else {
//                selectedYoutubers.add(youtuber.youtuberName)
//            }
//        }
//        listYoutuberAdapter.notifyDataSetChanged()
//        restaurantViewModel.currentPage = 0 // 페이지 번호 초기화
//        filterRestaurantsBySelectedYoutubers()
//    }
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

//    private fun filterRestaurantsBySelectedYoutubers() {
//        val selectedYoutuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }
//        val sortOption = getApiSortOption(currentSortOption) // 현재 정렬 옵션 가져오기
//
//        if (selectedYoutuberIds.isNotEmpty()) {
//            getCurrentLocationAndFetchFilteredRestaurants(selectedYoutuberIds)
//        } else {
//            // 유튜버 필터가 없을 때, 정렬 옵션에 따라 처리
//            getCurrentLocationAndFetchSortedRestaurants(sortOption)
//        }
//    }
        private fun applyNewFilter() {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        val sortOption = getApiSortOption(currentSortOption)
                        val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }

                        // ViewModel의 applyNewFilter 함수 호출
                        restaurantViewModel.applyNewFilter(latitude, longitude, sortOption, youtuberIds)
                    }
                }
            }
        }

    private fun getCurrentLocationAndFetchFilteredRestaurants(youtuberIds: List<Int>) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val sortOption = getApiSortOption(currentSortOption) // 현재 정렬 옵션 가져오기

                    // 정렬 옵션을 추가하여 메서드 호출
                    restaurantViewModel.getRestaurantsFilteredByYoutuber(latitude, longitude, youtuberIds, restaurantViewModel.currentPage, sortOption)

                } ?: run {
                    Log.e("ListRestaurantFragment", "Location information is not available.")
                }
            }
        } else {
            Log.e("ListRestaurantFragment", "Location permission not granted")
        }
    }


    private fun setBannerAds() {
        MobileAds.initialize(requireContext())          // 1) 광고 SDK 초기화
        val adRequest = AdRequest.Builder().build()   // 2)
        binding.adView.loadAd(adRequest)            // 3)

        // 4) 애드뷰 리스너 추가
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("ads log", "배너 광고가 로드되었습니다.") // 로그 출력
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("ads log", "배너 광고가 로드 실패했습니다. ${adError.responseInfo}")
            }

            override fun onAdOpened() {
                Log.d("ads log", "배너 광고를 열었습니다.")
                // 전면 에 광고가 오버레이 되었을 때
            }

            override fun onAdClicked() {
                Log.d("ads log", "배너 광고를 클랙했습니다.")
            }

            override fun onAdClosed() {
                Log.d("ads log", "배너 광고를 닫았습니다.")
            }
        }
    }
}