package com.adam.tastylog.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R
import com.adam.tastylog.model.YoutuberModel
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView


class ListYoutuberChipAdapter(
    private var youtuberList: List<YoutuberModel>,
    private val selectedYoutubers: Set<String>,
    private val onYoutuberSelected: (YoutuberModel) -> Unit
) : RecyclerView.Adapter<ListYoutuberChipAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textviewYoutuberName: TextView = view.findViewById(R.id.textview_youtuber_name)
        val circleImageViewYoutuber: CircleImageView = view.findViewById(R.id.circleImageView_influencer)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_influencer_chip, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val youtuber = youtuberList[position]
        val imageUrl = "https://food-log-bucket.s3.ap-northeast-2.amazonaws.com/" + youtuber.youtuberProfile

        holder.textviewYoutuberName.text = youtuber.youtuberName
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.circleImageViewYoutuber)

        // "전체" 옵션의 경우 기본 이미지 설정
        if (youtuber.youtuberName == "전체") {
            Glide.with(holder.itemView.context)
                .load(youtuber.youtuberProfile) // 이미지 URL 로드
                .into(holder.circleImageViewYoutuber)
        }

        // 선택된 상태에 따라 투명도 설정
        if (youtuber.youtuberName in selectedYoutubers) {
            // 선택된 아이템은 투명도를 적용
            holder.textviewYoutuberName.alpha = 0.6f // 예시 값
            holder.circleImageViewYoutuber.alpha = 0.6f // 예시 값
        } else {
            // 선택되지 않은 아이템은 투명도를 원래대로
            holder.textviewYoutuberName.alpha = 1.0f
            holder.circleImageViewYoutuber.alpha = 1.0f
        }

        holder.itemView.setOnClickListener {
            onYoutuberSelected(youtuber)
            notifyItemChanged(position)
        }
    }
    override fun getItemCount() = youtuberList.size

    fun updateYoutuberData(newData: List<YoutuberModel>) {
        youtuberList = newData
        notifyDataSetChanged()
    }

}
