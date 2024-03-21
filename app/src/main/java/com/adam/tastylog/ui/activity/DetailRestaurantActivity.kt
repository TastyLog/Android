package com.adam.tastylog.ui.activity

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.adam.tastylog.R
import com.adam.tastylog.databinding.ActivityDetailRestaurantBinding
import com.adam.tastylog.model.RestaurantModel
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import timber.log.Timber
import java.net.URLEncoder

class DetailRestaurantActivity : AppCompatActivity() {
    // View Binding 클래스 선언
    private lateinit var binding: ActivityDetailRestaurantBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityDetailRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setBannerAds()

        // 인텐트에서 RestaurantModel 객체 받기
        val restaurantData = intent.getSerializableExtra("restaurantData") as? RestaurantModel
        restaurantData?.let {
            // 받은 데이터를 사용하여 UI 업데이트
            updateUI(it)
            loadNaverMapPreview(it) // 네이버 맵 프리뷰 로드
        }

        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout_appBar)
        val backButtonFrame = findViewById<FrameLayout>(R.id.toolbar_backButton_Frame)
        val shareButtonFrame = findViewById<FrameLayout>(R.id.toolbar_shareButton_Frame) // 공유 버튼 참조
        val backButton = findViewById<ImageView>(R.id.toolbar_backButton) // 뒤로 가기 버튼 참조
        val shareButton = findViewById<ImageView>(R.id.toolbar_shareButton) // 공유 버튼 참조

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                // 축소됨 (Collapsed)
                backButton.setImageResource(R.drawable.icon_back) // 검은색 아이콘
                shareButton.setImageResource(R.drawable.icon_share_black) // 검은색 공유 아이콘
            } else {
                // 확장됨 (Expanded)
                backButton.setImageResource(R.drawable.icon_back_white) // 하얀색 아이콘
                shareButton.setImageResource(R.drawable.icon_share_white) // 하얀색 공유 아이콘
            }
        })

        backButtonFrame.setOnClickListener {
            finish() // 뒤로 가기 이벤트
        }
        shareButtonFrame.setOnClickListener {
            // 공유 로직 구현
            restaurantData?.let { sendKakaoLink(it) }
        }
    }

    private fun updateUI(restaurant: RestaurantModel) {
        val youtubePlayerView = findViewById<YouTubePlayerView>(R.id.youtube_player_view_detail_activity) // YouTubePlayerView 찾기
        val imageViewYoutubeThumbnail = findViewById<ImageView>(R.id.image_view_youtube_thumnail_detail_activity)
        val imageViewYoutubeIcon = findViewById<ImageView>(R.id.imageview_youtube_icon_detail_activity)
        val buttonCopyAddress = findViewById<AppCompatButton>(R.id.button_copy_address)
        val buttonIntentMap = findViewById<AppCompatButton>(R.id.button_intent_map)
        val videoId = extractYoutubeVideoId(restaurant.youtuberLink) // 유튜브 링크에서 동영상 ID 추출
        val imageViewRestaurantPicture = findViewById<ImageView>(R.id.imageview_restaurant_picture_detail_activity)
        val cardViewKakaoMapIntent = findViewById<MaterialCardView>(R.id.card_view_intent_kakao_map_detail_activity)//카카오 링크로 이동하는 CardView 찾기
        val cardViewNaverMapIntent = findViewById<MaterialCardView>(R.id.card_view_intent_naver_map_detail_activity) // 네이버 링크로 이동하는 CardView 찾기
        val imageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/" + restaurant.representativeImage


        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val fcmToken = sharedPreferences.getString("fcmToken", "Token not available")


        //UI 요소에 레스토랑 정보 설정
        binding.textviewRestaurantYoutubers.text = restaurant.youtuberName
        binding.textviewRestaurantName.text = restaurant.restaurantName
        binding.textviewRestaurantRating.text = "${restaurant.rating}"
        binding.ratingbarRestaurantRating.rating = restaurant.rating.toFloat()
        binding.textviewRestaurantReviews.text = String.format("네이버 리뷰 %d개", restaurant.totalReviews)
        binding.textviewRestaurantCategory.text = "${restaurant.category}"
        binding.textviewRestaurantCallNumber.text = "${restaurant.phoneNumber}"
        val addressText = "${restaurant.address}\n(현재 내 위치로부터 ${restaurant.distance})"

        binding.textviewRestaurantAddress.text = addressText
        // 음식점 대표이미지 로드
        Glide.with(this)
            .load(imageUrl)
            .into(imageViewRestaurantPicture)

        // 썸네일 이미지 로드
        Glide.with(this)
            .load("https://img.youtube.com/vi/$videoId/0.jpg")
            .into(imageViewYoutubeThumbnail)

        // 전화번호 클릭 시 전화 걸기 인텐트 실행
        binding.textviewRestaurantCallNumber.apply {
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

        buttonIntentMap.setOnClickListener {
            openWebPage(restaurant.naverLink) // 네이버 링크 열기
        }
        cardViewNaverMapIntent.setOnClickListener {
            openWebPage(restaurant.naverLink) // 네이버 링크 열기
        }
        cardViewKakaoMapIntent.setOnClickListener {
            openKakaoMap(restaurant) // 카카오맵 링크 열기
        }
        buttonCopyAddress.setOnClickListener {
            // restaurant 객체의 address 속성 클립보드에 복사
            copyTextToClipboard(restaurant.address)
        }

        // 유튜브 플레이어 준비
        youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youtubePlayer: YouTubePlayer) {
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
                            // 재생이 종료되면 썸네일 다시 보여주기
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
    }

    // 유튜브 동영상 ID 추출 함수
    private fun extractYoutubeVideoId(youtubeUrl: String): String {
        val uri = Uri.parse(youtubeUrl)
        return uri.getQueryParameter("v") ?: ""
    }

    // 웹 페이지 이동
    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        startActivity(intent)
    }

    //카카오 맵 페이지로 이동
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

    private fun loadNaverMapPreview(restaurant: RestaurantModel) {
        val clientId = getString(R.string.naver_client_id)
        val clientSecret = getString(R.string.naver_client_secret)

        // 레스토랑 이름 인코딩 (URL에서 사용하기 위해)
        val encodedRestaurantName = URLEncoder.encode(restaurant.restaurantName, "UTF-8")

        // 마커 설정 (툴팁 마커, 레스토랑 위치, 레스토랑 이름 라벨)
        val marker = "type:t|pos:${restaurant.longitude} ${restaurant.latitude}|color:0x80D7E0|label:$encodedRestaurantName"

        // 지도 URL 생성 (마커 포함)
        val mapUrl = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?" +
                "w=600&h=300&center=${restaurant.longitude},${restaurant.latitude}&level=16&scale=2&markers=$marker" +
                "&X-NCP-APIGW-API-KEY-ID=$clientId&X-NCP-APIGW-API-KEY=$clientSecret"

        Glide.with(this)
            .load(mapUrl)
            .into(binding.imageViewMapPreview)
    }

    // 주소를 클립보드에 복사하는 함수
    private fun copyTextToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("restaurant_address", text)
        clipboard.setPrimaryClip(clip)

        // Android 12L(API 수준 32) 이하에서만 토스트 메시지를 표시
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(this, "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //카카오톡 공유하기 함수
    private fun sendKakaoLink(restaurant: RestaurantModel) {
        val playStoreUrl = "https://play.google.com/store/apps/details?id=com.adam.tastylog"
        val defaultFeed = FeedTemplate(
            content = Content(
                title = restaurant.restaurantName,
                description = restaurant.address,
                imageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/${(restaurant.representativeImage)}",
                link = Link(
                    mobileWebUrl = playStoreUrl,
                    webUrl = playStoreUrl // 웹 브라우저용 링크
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
        if (ShareClient.instance.isKakaoTalkSharingAvailable(this)) {
            // 카카오톡으로 카카오톡 공유 가능
            ShareClient.instance.shareDefault(this, defaultFeed) { sharingResult, error ->
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
                KakaoCustomTabsClient.openWithDefault(this, sharerUrl)
            } catch(e: UnsupportedOperationException) {
                // CustomTabsServiceConnection 지원 브라우저가 없을 때 처리
            }

            try {
                KakaoCustomTabsClient.open(this, sharerUrl)
            } catch (e: ActivityNotFoundException) {
                // 디바이스에 설치된 인터넷 브라우저가 없을 때 처리
            }
        }
    }


    // 구글 ADS 함수
//    private fun setBannerAds() {
//        MobileAds.initialize(this)
//        val adRequest = AdRequest.Builder().build()
//        binding.adView.loadAd(adRequest)
//        // 애드뷰 리스너 추가
//        binding.adView.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                Log.d("ads log", "배너 광고가 로드되었습니다.")
//            }
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Log.d("ads log", "배너 광고가 로드 시패했습니다. ${adError.responseInfo}")
//            }
//            override fun onAdOpened() {
//                Log.d("ads log", "배너 광고를 열었습니다.")
//            }
//            override fun onAdClicked() {
//                Log.d("ads log", "배너 광고를 클랙했습니다.")
//            }
//            override fun onAdClosed() {
//                Log.d("ads log", "배너 광고를 닫았습니다.")
//            }
//        }
//    }
}
