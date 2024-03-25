package com.adam.tastylog.ui.fragment

//import android.Manifest
//import android.Manifest.*
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import androidx.appcompat.widget.AppCompatButton
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.activityViewModels
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.adam.tastylog.R
//import com.adam.tastylog.databinding.FragmentYoutuberFilterBinding
//import com.adam.tastylog.model.YoutuberModel
//import com.adam.tastylog.repository.RestaurantRepository
//import com.adam.tastylog.repository.YoutuberRepository
//import com.adam.tastylog.ui.adapter.FilterYoutuberAdapter
//import com.adam.tastylog.useCase.GetRestaurantListUseCase
//import com.adam.tastylog.useCase.GetYoutuberListUseCase
//import com.adam.tastylog.utils.RetrofitBuilder
//import com.adam.tastylog.viewModel.RestaurantViewModel
//import com.adam.tastylog.viewModel.RestaurantViewModelFactory
//import com.adam.tastylog.viewModel.YoutuberViewModel
//import com.adam.tastylog.viewModel.YoutuberViewModelFactory
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment

//class YoutuberSelectionFragment : BottomSheetDialogFragment() {
//    private lateinit var binding: FragmentYoutuberFilterBinding
//    private var selectedYoutubers = mutableSetOf<String>()
//    private var youtuberNameToIdMap = mapOf<String, Int>()
//
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    private lateinit var filterYoutuberAdapter: FilterYoutuberAdapter
//    private lateinit var youtuberRecyclerView: RecyclerView
//    private lateinit var youtuberViewModel: YoutuberViewModel
//
//    // 공유 ViewModel 초기화
//    private val restaurantViewModel: RestaurantViewModel by activityViewModels {
//        // Factory 생성
//        RestaurantViewModelFactory(RetrofitBuilder.restaurantService.let { restaurantService ->
//            RestaurantRepository(GetRestaurantListUseCase(restaurantService))
//        })
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // ViewModel 초기화
//        val restaurantService = RetrofitBuilder.restaurantService
//
//        val getYoutuberListUseCase = GetYoutuberListUseCase(restaurantService)
//        val youtuberRepository = YoutuberRepository(getYoutuberListUseCase)
//        youtuberViewModel = ViewModelProvider(this,YoutuberViewModelFactory(youtuberRepository)).get(YoutuberViewModel::class.java)
//        youtuberViewModel.getYoutubers()
//
//
//        // FusedLocationProviderClient 초기화
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//
//
//        // Adapter 인스턴스 초기화
//
//
//    }
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentYoutuberFilterBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupYoutuberFilteringUI()
//
//
//        // 선택 완료 버튼 클릭 리스너
//        view.findViewById<AppCompatButton>(R.id.button_select_youtuber).setOnClickListener {
//            // 필터링 로직 실행
//            applyNewFilter()
//            // 프래그먼트 닫기
//            dismiss()
//        }
//
//
//
//    }
//
//    override fun onStart() {
//        super.onStart()
//        val bottomSheetDialog = dialog as BottomSheetDialog?
//        val bottomSheet = bottomSheetDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//        val layoutParams = bottomSheet?.layoutParams
//        layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
//        bottomSheet?.layoutParams = layoutParams
//
//        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet as View)
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//        bottomSheetBehavior.isDraggable = false
//    }
//
//
//
//
//    private fun setupYoutuberFilteringUI() {
//        youtuberRecyclerView = binding.recyclerviewSelectYoutuber
//        filterYoutuberAdapter = FilterYoutuberAdapter(listOf(), selectedYoutubers) { youtuber ->
//            handleYoutuberSelection(youtuber)
//        }
//        youtuberRecyclerView.adapter = filterYoutuberAdapter
//        youtuberRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
////        val layoutManager = GridLayoutManager(context, 3)
////        youtuberRecyclerView.layoutManager = layoutManager
//
//        // 유튜버 이름과 ID를 매핑
//        youtuberViewModel.youtubers.observe(viewLifecycleOwner, Observer { youtubers ->
//            val allYoutubers = listOf(
//                YoutuberModel("전체", "", 0, "")
//            ) + youtubers
//
//            youtuberNameToIdMap = youtubers.associate { it.youtuberName to it.youtuberId }
//            filterYoutuberAdapter.updateYoutuberData(allYoutubers)
//        })
//    }
//
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
//        filterYoutuberAdapter.notifyDataSetChanged()
//    }
//
//    private fun applyNewFilter() {
//        if (ContextCompat.checkSelfPermission(requireContext(), permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//                location?.let {
//                    val latitude = it.latitude
//                    val longitude = it.longitude
////                    val currentSearchWord = restaurantViewModel.getCurrentSearchWord() // 현재 검색어 가져오기
//                    val youtuberIds = selectedYoutubers.mapNotNull { youtuberNameToIdMap[it] }.toList()
//
//
//                    val sortOption = "" // 또는 현재 선택된 정렬 옵션을 사용
//
//                    // 현재 검색어를 포함하여 ViewModel의 applyNewFilter 함수 호출
//                    restaurantViewModel.applyNewFilter(latitude, longitude, sortOption, youtuberIds, "")
//                }
//            }
//        }
//    }
//
//
//}