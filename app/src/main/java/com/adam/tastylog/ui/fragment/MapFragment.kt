package com.adam.tastylog.ui.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.adam.tastylog.databinding.FragmentMapBinding
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R
import com.adam.tastylog.model.RestaurantModel
import com.adam.tastylog.model.YoutuberModel
import com.adam.tastylog.repository.RestaurantRepository
import com.adam.tastylog.repository.YoutuberRepository
import com.adam.tastylog.ui.adapter.ListYoutuberChipAdapter
import com.adam.tastylog.useCase.GetRestaurantListUseCase
import com.adam.tastylog.useCase.GetYoutuberListUseCase
import com.adam.tastylog.utils.RetrofitBuilder
import com.adam.tastylog.viewModel.RestaurantViewModel
import com.adam.tastylog.viewModel.RestaurantViewModelFactory
import com.adam.tastylog.viewModel.YoutuberViewModel
import com.adam.tastylog.viewModel.YoutuberViewModelFactory
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale


/** 맵 프래그 먼트*/
class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var binding: FragmentMapBinding
    private lateinit var naverMap: NaverMap
    private var isUserInteracting = false
    private lateinit var lottieAnimationView: LottieAnimationView //로딩 애니메이션
    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var youtuberViewModel: YoutuberViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var youtubePlayer: YouTubePlayer? = null
    private var isInitialYoutuberClick = true // 유튜버 칩 최초 클릭 판별 boolean

    // 유튜버 RecyclerView와 어댑터를 위한 변수 선언
    private lateinit var youtuberRecyclerView: RecyclerView
    private lateinit var listYoutuberAdapter: ListYoutuberChipAdapter

    private val markers = mutableListOf<Marker>()
    private val selectedYoutubers = mutableSetOf<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("MapFragment", "onCreateView called")
        (activity as AppCompatActivity).supportActionBar?.hide()
        // 기존 바인딩 설정
        binding = FragmentMapBinding.inflate(inflater, container, false)

        val restaurantService = RetrofitBuilder.restaurantService
        val getRestaurantListUseCase = GetRestaurantListUseCase(restaurantService)
        val restaurantRepository = RestaurantRepository(getRestaurantListUseCase)
        val restaurantViewModelFactory = RestaurantViewModelFactory(restaurantRepository)
        restaurantViewModel = ViewModelProvider(this, restaurantViewModelFactory).get(RestaurantViewModel::class.java)

        // YoutuberViewModel 초기화
        val getYoutuberListUseCase = GetYoutuberListUseCase(restaurantService)
        val youtuberRepository = YoutuberRepository(getYoutuberListUseCase)
        youtuberViewModel = ViewModelProvider(this, YoutuberViewModelFactory(youtuberRepository)).get(YoutuberViewModel::class.java)
        youtuberViewModel.getYoutubers()


        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        lottieAnimationView = binding.root.findViewById(R.id.lottieAnimationView)


        setupBottomSheetBehavior()
        setupButtonListRestaurantDialog()
        // 유튜버 칩 리사이클러뷰 설정
        setupYoutuberChipRecyclerView()


        return binding.root
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bottomSheetBehavior = BottomSheetBehavior.from(binding.root.findViewById<View>(R.id.bottomSheetLayout))
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    // BottomSheet가 열려있으면 닫기
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    // 여기에서 스크롤 위치 초기화
                    val scrollView = binding.root.findViewById<NestedScrollView>(R.id.nested_scrollView_bottom_sheet)
                    scrollView?.scrollTo(0,0)
                } else {
                    // 그렇지 않으면 기본 동작 수행
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    private fun setupButtonListRestaurantDialog() {
        binding.buttonListRestaurantDialog.setOnClickListener {
            val bottomSheetFragment = ListRestaurantFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun setupBottomSheetBehavior() {
        val bottomSheetLayout = binding.root.findViewById<View>(R.id.bottomSheetLayout)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        val displayMetrics = DisplayMetrics()
        (activity as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        bottomSheetBehavior.maxHeight = (screenHeight * 0.8).toInt()
        // 초기에 BottomSheet를 숨김 상태로 설정
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        val scrollView = binding.root.findViewById<NestedScrollView>(R.id.nested_scrollView_bottom_sheet)

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("BottomSheet", "State changed to: $newState")
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> binding.buttonListRestaurantDialog.visibility = View.GONE
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        youtubePlayer?.pause()
                        binding.buttonListRestaurantDialog.visibility = View.VISIBLE
                        scrollView?.scrollTo(0, 0)
                        Log.d("BottomSheet", "ScrollView reset")
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }


    override fun onMapReady(mapObject: NaverMap) {
        Log.d("MapFragment", "onMapReady called")
        naverMap = mapObject

        // 사용자 위치 관련 기존 설정
        val uiSettings = naverMap.uiSettings
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isCompassEnabled = false


        if (isLocationPermissionGranted() && isLocationServiceEnabled()) {
            getCurrentLocationAndFetchRestaurants()
            restaurantViewModel.restaurants.observe(viewLifecycleOwner, Observer { restaurants ->
                createMarkers(restaurants)
            })
        }
        if (isLocationPermissionGranted() && isLocationServiceEnabled()) {
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
            moveCameraToCurrentLocation()
        } else {
            Log.d("MapFragment", "Location permission not granted or location service disabled")
        }
        naverMap.addOnCameraChangeListener { reason, _ ->
            naverMap.addOnCameraChangeListener { reason, _ ->
                isUserInteracting = reason == CameraUpdate.REASON_GESTURE
            }
        }
        // 위치 정보 로드에 실패하거나 지연될 경우를 대비해 최대 로딩 시간 설정
        val loadingTimeoutMillis = 6000L // 6초
        Handler(Looper.getMainLooper()).postDelayed({
            if (lottieAnimationView.visibility == View.VISIBLE) {
                lottieAnimationView.visibility = View.GONE // 애니메이션 종료
                Log.d("MapFragment", "Location loading animation stopped due to timeout")
            }
        }, loadingTimeoutMillis)

    }

    private fun createMarkers(restaurants: List<RestaurantModel>) {
        clearMarkers()
        restaurants.forEach { restaurant ->
            createAndAddMarker(restaurant)
        }
    }

    private fun createAndAddMarker(restaurant: RestaurantModel) {
        val profileImageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/${restaurant.youtuberProfile}"
        Glide.with(this)
            .asBitmap()
            .load(profileImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val marker = Marker()
                    marker.position = LatLng(restaurant.latitude, restaurant.longitude)
                    marker.width = Marker.SIZE_AUTO
                    marker.height = Marker.SIZE_AUTO

                    val baseIcon = createBaseMarkerIcon()
                    val combinedIcon = overlayProfileImageOnMarker(baseIcon, resource)
                    marker.icon = OverlayImage.fromBitmap(combinedIcon)
                    marker.map = naverMap
                    marker.tag = restaurant

                    marker.setOnClickListener {
                        val selectedRestaurant = it.tag as RestaurantModel
                        updateBottomSheetUI(selectedRestaurant)
                        toggleBottomSheet()
                        true
                    }
                    markers.add(marker)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    // 필요한 경우 처리하기
                }
            })
    }

    //프로필 이미지 원형 처리 함수
    private fun createRoundedProfileImage(profileImage: Bitmap): Bitmap {
        val size = Math.min(profileImage.width, profileImage.height)
        val roundedImage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(roundedImage)
        val path = Path()
        // 원형 클리핑 경로 설정
        path.addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CCW)
        canvas.clipPath(path)
        // 프로필 이미지를 원형으로 그리기
        canvas.drawBitmap(profileImage, (size - profileImage.width) / 2f, (size - profileImage.height) / 2f, null)

        return roundedImage
    }

    // 기본 마커 아이콘을 Bitmap으로 생성하는 함수
    private fun createBaseMarkerIcon(): Bitmap {
        val size = 100 // 마커 아이콘의 크기
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // 마커 아이콘의 배경을 그리기 위한 Paint 설정
        val paint = Paint().apply {
            color = Color.WHITE // 배경색
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // 원형 모양의 마커 아이콘 배경 그리기
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        return bitmap
    }

    // 기본 마커 아이콘 위에 유튜버 프로필 이미지를 오버레이하는 함수
    private fun overlayProfileImageOnMarker(baseIcon: Bitmap, profileImage: Bitmap): Bitmap {
        val canvas = Canvas(baseIcon)
        // 프로필 이미지를 원형으로 크롭
        val roundedProfileImage = createRoundedProfileImage(profileImage)
        // 프로필 이미지의 크기와 위치 조정 (여기서는 중앙에 배치)
        val imageSize = baseIcon.width / 1 // 프로필 이미지 크기는 마커 아이콘의 절반으로 설정
        val imageX = (baseIcon.width - imageSize) / 1
        val imageY = (baseIcon.height - imageSize) / 1
        // 원형 프로필 이미지를 마커 아이콘 위에 오버레이
        canvas.drawBitmap(roundedProfileImage, null, Rect(imageX, imageY, imageX + imageSize, imageY + imageSize), null)

        return baseIcon
    }

    //마커 Clear 시키는 함수
    private fun clearMarkers() {
        markers.forEach { it.map = null }
        markers.clear()
    }


    /** 바텀 시트 UI 업데이트 */
    private fun updateBottomSheetUI(restaurant: RestaurantModel) {
        val bottomSheetLayout = binding.root.findViewById<View>(R.id.bottomSheetLayout)
        val textviewRestaurantName = bottomSheetLayout.findViewById<TextView>(R.id.textview_restaurant_name)
        val textviewRestaurantCategory = bottomSheetLayout.findViewById<TextView>(R.id.textview_restaurant_category)
        val textviewRestaurantCallNumber = bottomSheetLayout.findViewById<TextView>(R.id.textview_restaurant_call_number)
        val textviewRestaurantAddress = bottomSheetLayout.findViewById<TextView>(R.id.textview_restaurant_address)
        val textviewReviews = bottomSheetLayout.findViewById<TextView>(R.id.textview_bottomSheet_restaurant_reviews) // 리뷰 수
        val ratingBarRating = bottomSheetLayout.findViewById<SimpleRatingBar>(R.id.ratingbar_bottomSheet_restaurant_rating) // 별점 레이팅
        val textviewRating = bottomSheetLayout.findViewById<TextView>(R.id.textview_bottomSheet_restaurant_rate) // 별점 텍스트
        val textviewRestaurantDistance = bottomSheetLayout.findViewById<TextView>(R.id.textview_restaurant_distance)
        val youtubePlayerView = bottomSheetLayout.findViewById<YouTubePlayerView>(R.id.youtube_player_view) // YouTubePlayerView 찾기
        val imageViewYoutubeIcon = bottomSheetLayout.findViewById<ImageView>(R.id.imageview_youtube_icon)
        val imageViewYoutubeThumbnail = bottomSheetLayout.findViewById<ImageView>(R.id.image_view_youtube_thumnail)
        val videoId = extractYoutubeVideoId(restaurant.youtuberLink) // 유튜브 링크에서 동영상 ID 추출
        val imageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/" + restaurant.representativeImage
        val cardViewKakaoMapIntent = bottomSheetLayout.findViewById<MaterialCardView>(R.id.card_view_intent_kakao_map)//카카오 링크로 이동하는 CardView 찾기
        val cardViewNaverMapIntent = bottomSheetLayout.findViewById<MaterialCardView>(R.id.card_view_intent_naver_map) // 네이버 링크로 이동하는 CardView 찾기
        val cardViewKakaoShareIntent = bottomSheetLayout.findViewById<MaterialCardView>(R.id.card_view_intent_kakao_share)
        val imageViewRestaurantPicture = bottomSheetLayout.findViewById<ImageView>(R.id.imageview_restaurant_picture)

        imageViewYoutubeThumbnail.visibility = View.VISIBLE // 새로운 데이터 항목에 대해 썸네일 초기화
        imageViewYoutubeIcon.visibility = View.VISIBLE

        // 음식점 대표이미지 로드
        Glide.with(this)
            .load(imageUrl)
            .into(imageViewRestaurantPicture)
        // 썸네일 이미지 로드
        Glide.with(this)
            .load("https://img.youtube.com/vi/$videoId/0.jpg")
            .into(imageViewYoutubeThumbnail)
        // 유튜브 플레이어 준비
        youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youtubePlayer: YouTubePlayer) {
                this@MapFragment.youtubePlayer = youtubePlayer
                youtubePlayer.addListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // 재생 준비가 되면 썸네일 숨기기
                        imageViewYoutubeThumbnail.visibility = View.GONE
                        imageViewYoutubeIcon.visibility = View.GONE
                    }
                    override fun onStateChange(
                        youTubePlayer: YouTubePlayer,
                        state: PlayerConstants.PlayerState
                    ) {
                        if (state == PlayerConstants.PlayerState.ENDED) {
                            // 재생이 종료되면 썸네일을 보여줌.
                            imageViewYoutubeThumbnail.visibility = View.VISIBLE
                            imageViewYoutubeIcon.visibility = View.VISIBLE
                        }
                    }
                })
            }
        })

        // 썸네일 클릭 이벤트 설정
        imageViewYoutubeThumbnail.setOnClickListener {
            // 특정 유튜버 이름인 경우, 웹 브라우저를 통해 유튜브 링크 열기
            if (restaurant.youtuberName == "tzuyang쯔양" || restaurant.youtuberName == "장사의 신") {
                openWebPage("https://www.youtube.com/watch?v=$videoId")
            } else {
                // YouTubePlayerView를 통해 비디오 재생 시작
                youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youtubePlayer: YouTubePlayer) {
                        youtubePlayer.loadVideo(videoId, 0f)
                        imageViewYoutubeThumbnail.visibility = View.GONE
                        imageViewYoutubeIcon.visibility = View.GONE
                    }
                })
            }
        }
        imageViewYoutubeIcon.setOnClickListener {
            if (restaurant.youtuberName == "tzuyang쯔양" || restaurant.youtuberName == "장사의 신") {
                openWebPage("https://www.youtube.com/watch?v=$videoId")
            } else {
                // YouTubePlayerView를 통해 비디오 재생 시작
                youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youtubePlayer: YouTubePlayer) {
                        youtubePlayer.loadVideo(videoId, 0f)
                        imageViewYoutubeThumbnail.visibility = View.GONE
                        imageViewYoutubeIcon.visibility = View.GONE
                    }
                })
            }
        }

        cardViewNaverMapIntent.setOnClickListener {
            openWebPage(restaurant.naverLink) // 네이버 링크 열기
        }
        cardViewKakaoMapIntent.setOnClickListener {
            openKakaoMap(restaurant) // 카카오맵 링크 열기
        }
        cardViewKakaoShareIntent.setOnClickListener {
            sendKakaoLink(restaurant)
        }

        textviewRestaurantName.text = restaurant.restaurantName
        textviewRestaurantAddress.text = restaurant.address
        textviewRestaurantCategory.text = restaurant.category
        textviewRestaurantCallNumber.text = restaurant.phoneNumber
        textviewRestaurantDistance.text = restaurant.distance
        textviewRating.text = restaurant.rating.toString()
        ratingBarRating.rating = restaurant.rating.toFloat()

        textviewRestaurantAddress.setOnClickListener{
            copyTextToClipboard(restaurant.address)
        }
        // 전화번호 클릭 시 전화 걸기 인텐트 실행
        textviewRestaurantCallNumber.apply {
            text = restaurant.phoneNumber
            setOnClickListener {
                val phone = restaurant.phoneNumber.trim() // 공백 제거
                Log.d("PhoneCallIntent", "Dialing number: $phone")
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            }
        }
        val rateReviewsText = "리뷰 ${
            NumberFormat.getNumberInstance(
                Locale.getDefault()).format(restaurant.totalReviews)}개"
        textviewReviews.text = rateReviewsText

        //추가
    }


    /** 카카오 맵 인텐트*/
    private fun openKakaoMap(restaurant: RestaurantModel) {
        try {
            // 카카오맵 앱 URL 스키마
            val intentUri = Uri.parse("kakaomap://look?p=${restaurant.latitude},${restaurant.longitude}")
            val intent = Intent(Intent.ACTION_VIEW, intentUri)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 카카오맵 앱이 설치되어 있지 않은 경우, 웹사이트를 통해 열기
            val kakaoMapUrl = "https://map.kakao.com/?urlX=${restaurant.longitude}&urlY=${restaurant.latitude}&name=${restaurant.restaurantName}"
            openWebPage(kakaoMapUrl)
        }
    }

    /**Default 웹사이트 인텐트*/
    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        startActivity(intent)
    }

    // 유튜브 동영상 ID 추출 함수
    private fun extractYoutubeVideoId(youtubeUrl: String): String {
        val uri = Uri.parse(youtubeUrl)
        return uri.getQueryParameter("v") ?: ""
    }

    // BottomSheet 상태 토글 함수
    private fun toggleBottomSheet() {
        val bottomSheetLayout = binding.root.findViewById<View>(R.id.bottomSheetLayout)
        bottomSheetLayout.visibility = View.VISIBLE // BottomSheet를 보이게 설정
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.buttonListRestaurantDialog.visibility = View.GONE
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationServiceEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**현재 위치로 카메라 이동*/
    private fun moveCameraToCurrentLocation() {
        Log.d("MapFragment", "Moving camera to current location")
        if (isLocationPermissionGranted() && isLocationServiceEnabled()) {
            val locationSource = FusedLocationSource(requireActivity(), LOCATION_PERMISSION_REQUEST_CODE)
            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.Follow

            locationSource.activate { location ->
                location?.let {
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude))
                    naverMap.moveCamera(cameraUpdate)
                    lottieAnimationView.visibility = View.GONE // 위치 로드 후 애니메이션 숨기기
                }
            }
        } else {
            Log.e("MapFragment", "Location permission not granted or Location Services not enabled")
        }
    }

    /**맛집 정보 Load*/
    private fun getCurrentLocationAndFetchRestaurants() {
        Log.d("MapFragment", "Getting current location")
        if (isLocationPermissionGranted() && isLocationServiceEnabled()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    val latitude: Double
                    val longitude: Double

                    if (location != null) {
                        Log.d("MapFragment", "Location received: Lat=${location.latitude}, Lon=${location.longitude}")
                        latitude = location.latitude
                        longitude = location.longitude
                    } else {
                        Log.e("MapFragment", "Location information is not available. Defaulting to Gangnam Station")

                        // 서울 강남역의 위도와 경도
                        latitude = 37.498095
                        longitude = 127.027610
                    }

                    restaurantViewModel.getAllRestaurants(latitude, longitude)
                }
            } else {
                Log.e("MapFragment", "Location permission not granted")
            }
        } else {
            Log.e("MapFragment", "Location permission not granted or Location Services not enabled")
        }
    }



    // 주소를 클립보드에 복사하는 함수
    private fun copyTextToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("restaurant_address", text)
        clipboard.setPrimaryClip(clip)

        // Android 12L(API 수준 32) 이하에서만 토스트 메시지를 표시
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(requireContext(), "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    /** -------youtuber------- 시작 */
    // 유튜버 칩 리사이클러뷰 설정
    private fun setupYoutuberChipRecyclerView() {
        youtuberRecyclerView = binding.root.findViewById(R.id.recyclerview_youtuber_chip)
        listYoutuberAdapter = ListYoutuberChipAdapter(listOf(), selectedYoutubers) { youtuber ->
            handleYoutuberSelection(youtuber)
        }
        youtuberRecyclerView.adapter = listYoutuberAdapter
        youtuberRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        youtuberViewModel.youtubers.observe(viewLifecycleOwner, Observer { youtubers ->
            // "전체" 옵션 추가
            val allYoutubers = listOf(
                YoutuberModel("전체", "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/allImage.png", 0, "")
            ) + youtubers
            listYoutuberAdapter.updateYoutuberData(allYoutubers)
        })

    }

    /**유튜버 선택 필터링*/
    private fun handleYoutuberSelection(youtuber: YoutuberModel) {
        if (youtuber.youtuberName == "전체") {
            selectedYoutubers.clear()
        } else {
            if (youtuber.youtuberName in selectedYoutubers) {
                selectedYoutubers.remove(youtuber.youtuberName)
            } else {
                selectedYoutubers.add(youtuber.youtuberName)
            }
        }
        filterMarkersBySelectedYoutubers()
        listYoutuberAdapter.notifyDataSetChanged() // 어댑터에 변경 사항 알림

        //유튜버 칩 최초 선택시
        if (isInitialYoutuberClick) {
            if (::naverMap.isInitialized) {
                smoothZoomOutToKorea()
                isInitialYoutuberClick = false
            } else {
                Log.e("MapFragment", "naverMap is not initialized yet.")
            }
        }
    }

    // 선택된 유튜버에 따라 음식점 데이터 필터링 및 마커 업데이트
    private fun filterMarkersBySelectedYoutubers() {
        markers.forEach { marker ->
            val restaurant = marker.tag as RestaurantModel
            marker.map = if (restaurant.youtuberName in selectedYoutubers || selectedYoutubers.isEmpty()) naverMap else null
        }
    }


    // 우리나라 전체를 보여주는 부드러운 줌아웃
    private fun smoothZoomOutToKorea() {
        val koreaLatLngBounds = LatLngBounds(
            LatLng(33.0, 124.0), // 대한민국 남서쪽 좌표
            LatLng(43.0, 132.0)  // 대한민국 북동쪽 좌표
        )
        val cameraUpdate = CameraUpdate.fitBounds(koreaLatLngBounds).animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }


    /**카카오톡 공유하기 */
    private fun sendKakaoLink(restaurant: RestaurantModel) {
        val defaultFeed = FeedTemplate(
            content = Content(
                title = restaurant.restaurantName,
                description = restaurant.address,
                imageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/${(restaurant.representativeImage)}",
                link = Link(
                    mobileWebUrl = restaurant.naverLink,
                    webUrl = restaurant.naverLink // 웹 브라우저용 링크
                )
            ),
            buttons = listOf(
                Button(
                    "자세히 보기",
                    Link(
                        mobileWebUrl = restaurant.naverLink,
                        webUrl = restaurant.naverLink
                    )
                )
            )
        )

        if (ShareClient.instance.isKakaoTalkSharingAvailable(requireContext())) {
            // 카카오톡으로 카카오톡 공유 가능
            ShareClient.instance.shareDefault(requireContext(), defaultFeed) { sharingResult, error ->
                if (error != null) {
                }
                else if (sharingResult != null) {
                    Timber.d("카카오톡 공유 성공 ${sharingResult.intent}")
                    startActivity(sharingResult.intent)

                    Timber.w("Warning Msg: ${sharingResult.warningMsg}")
                    Timber.w("Argument Msg: ${sharingResult.argumentMsg}")
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유 사용 권장
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(defaultFeed)

            try {
                KakaoCustomTabsClient.openWithDefault(requireContext(), sharerUrl)
            } catch(e: UnsupportedOperationException) {
                // CustomTabsServiceConnection 지원 브라우저가 없을 때 처리
            }

            try {
                KakaoCustomTabsClient.open(requireContext(), sharerUrl)
            } catch (e: ActivityNotFoundException) {
                // 디바이스에 설치된 인터넷 브라우저가 없을 때 처리
            }
        }
    }


    /**----------------------생명주기 메서드----------------------*/
    /**----------------------생명주기 메서드----------------------*/
    override fun onStart() {
        super.onStart()
        Log.d("MapFragment", "onStart called")
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MapFragment", "onResume called")
        setupBackPressHandler()

        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MapFragment", "onPause called")
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        Log.d("MapFragment", "onStop called")
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MapFragment", "onDestroy called")
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("MapFragment", "onSaveInstanceState called")
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("MapFragment", "onLowMemory called")
        binding.mapView.onLowMemory()
    }
}