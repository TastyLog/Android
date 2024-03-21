package com.adam.tastylog.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.red
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R
import com.adam.tastylog.model.YoutuberModel
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView


class FilterYoutuberAdapter(
    private var youtuberList: List<YoutuberModel>,
    private val selectedYoutubers: Set<String>,
    private val onYoutuberSelected: (YoutuberModel) -> Unit
) : RecyclerView.Adapter<FilterYoutuberAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textviewYoutuberName: TextView = view.findViewById(R.id.textview_youtuber_tag)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_youtuber_filter, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val youtuber = youtuberList[position]

        holder.textviewYoutuberName.text = youtuber.youtuberName

        // 선택된 상태에 따라 투명도 설정
        if (youtuber.youtuberName in selectedYoutubers) {
            // 선택된 아이템은 투명도를 적용
            holder.textviewYoutuberName.setTextColor(Color.WHITE)
            holder.textviewYoutuberName.setBackgroundResource(R.drawable.textview_corner_radius_black)


        } else {
            // 선택되지 않은 아이템은 투명도를 원래대로
            holder.textviewYoutuberName.setTextColor(Color.BLACK)
            holder.textviewYoutuberName.setBackgroundResource(R.drawable.textview_corner_radius)

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
