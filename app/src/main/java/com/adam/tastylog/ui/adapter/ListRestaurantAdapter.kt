package com.adam.tastylog.ui.adapter

import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R
import com.adam.tastylog.model.RestaurantModel
import android.view.ViewGroup
import com.adam.tastylog.ui.activity.DetailRestaurantActivity
import com.bumptech.glide.Glide
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import de.hdodenhof.circleimageview.CircleImageView
import java.text.NumberFormat
import java.util.Locale


class ListRestaurantAdapter(private var restaurantList: List<RestaurantModel>) :
    RecyclerView.Adapter<ListRestaurantAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_restaurant, parent, false)
        return ViewHolder(view) { position ->
            // 클릭 이벤트 처리
            val context = view.context
            val intent = Intent(context, DetailRestaurantActivity::class.java).apply {
                // 인텐트에 레스토랑 데이터 추가
                putExtra("restaurantData", restaurantList[position])
            }
            context.startActivity(intent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurantList[position]
        holder.bind(restaurant)

        Log.i("ListRestaurantAdapter", "Processing restaurant at distance: ${restaurant.distance}")
    }

    // 데이터 바인딩을 위한 메서드
    private fun ViewHolder.bind(restaurant: RestaurantModel) {
        textViewName.text = restaurant.restaurantName
        textViewCategory.text = restaurant.category
        val textviewReviewsText = "리뷰 ${
            NumberFormat.getNumberInstance(Locale.getDefault()).format(restaurant.totalReviews)}개"
        textViewRateReviews.text = textviewReviewsText
        textviewListRestaurantDistance.text = restaurant.distance
        ratingbarRating.rating = restaurant.rating.toFloat()
        textViewRating.text = "${restaurant.rating} / 5"

        val youtuberImageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/" + restaurant.youtuberProfile
        Glide.with(itemView.context)
            .load(youtuberImageUrl)
            .into(circlerImageViewInfluencer)


        //        val youtubersText = "유튜버 '${restaurant.youtuberName}'가 방문한 맛집 입니다!"
        //        textviewListRestaurantYoutuber.text = youtubersText


//        val youtubersText = "유튜버 '${restaurant.youtuberName}'가 방문한 맛집 입니다!"
//        val spannableString = SpannableString(youtubersText)
//        // '${restaurant.youtuberName}' 부분의 시작과 끝 인덱스를 찾습니다.
//        val startIndex = youtubersText.indexOf(restaurant.youtuberName)
//        val endIndex = startIndex + restaurant.youtuberName.length
//        // 해당 부분에 볼드체 스타일을 적용합니다.
//        spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        // TextView에 SpannableString을 설정합니다.
//        textviewListRestaurantYoutuber.text = spannableString


        // Glide를 사용하여 이미지 로드
        val imageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/${restaurant.representativeImage}"
        Glide.with(itemView.context)
            .load(imageUrl)
            .into(imageViewListRestaurantPicture)
    }

    override fun getItemCount() = restaurantList.size

    fun updateRestaurantData(newData: List<RestaurantModel>) {
        restaurantList = newData
        notifyDataSetChanged()
    }

    class ViewHolder(view: View, onClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val imageViewListRestaurantPicture: ImageView = view.findViewById(R.id.imageview_list_restaurant_picture)
        val textViewName: TextView = view.findViewById(R.id.textview_list_restaurant_name)
        val textViewCategory: TextView = view.findViewById(R.id.textview_list_restaurant_category)
        val textViewRateReviews: TextView = view.findViewById(R.id.textview_list_restaurant_reviews)
        val textviewListRestaurantDistance: TextView = view.findViewById(R.id.textview_list_restaurant_distance)
        //  val textviewListRestaurantYoutuber: TextView = view.findViewById(R.id.textview_list_restaurant_youtuber)
        val circlerImageViewInfluencer: CircleImageView = view.findViewById(R.id.circleImageView_list_restaurant_influencer)
        val ratingbarRating: SimpleRatingBar = view.findViewById(R.id.ratingbar_list_restaurant_rating)
        val textViewRating: TextView = view.findViewById(R.id.textview_list_restaurant_rating)
        init {
            view.setOnClickListener {
                onClick(adapterPosition)
            }

            // 이미지뷰에도 동일한 클릭 이벤트 설정(페이지 인텐트)
            imageViewListRestaurantPicture.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }
}
