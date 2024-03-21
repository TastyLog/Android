package com.adam.tastylog.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R
import com.adam.tastylog.model.RealTimeSearchTermModel



class RealTimeSearchTermAdapter(private var context: Context, private var data: List<RealTimeSearchTermModel>) :
    RecyclerView.Adapter<RealTimeSearchTermAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val rankTextView: TextView = view.findViewById(R.id.textView_search_rank)
        private val keywordTextView: TextView = view.findViewById(R.id.textView_search_rank_keyword)
        private val imageViewSearchRankChange: ImageView = view.findViewById(R.id.imageview_search_rank_change)


        fun bind(item: RealTimeSearchTermModel, context: Context) {
            rankTextView.text = item.rank.toString()
            keywordTextView.text = item.keyword

            val iconRes = when (item.state) {
                "new" -> R.drawable.icon_arrow_new
                "up" -> R.drawable.icon_arrow_up
                "down" -> R.drawable.icon_arrow_down
                "none" -> R.drawable.icon_arrow_same
                else -> 0 // 기본값이나 오류 상태를 위한 아이콘. 필요에 따라 변경 가능
            }

            if (iconRes != 0) {
                imageViewSearchRankChange.setImageResource(iconRes)
                imageViewSearchRankChange.visibility = View.VISIBLE
            } else {
                imageViewSearchRankChange.visibility = View.GONE
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_real_time_search_term, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], context)
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<RealTimeSearchTermModel>) {
        val reorderedData = rearrangeData(newData) // 데이터 재배치
        this.data = reorderedData
        notifyDataSetChanged()
    }

    private fun rearrangeData(originalData: List<RealTimeSearchTermModel>): List<RealTimeSearchTermModel> {
        val result = mutableListOf<RealTimeSearchTermModel>()
        // 데이터 재배치 로직
        val halfSize = originalData.size / 2
        for (i in 0 until halfSize) {
            result.add(originalData[i])
            if (i + halfSize < originalData.size) {
                result.add(originalData[i + halfSize])
            }
        }
        return result
    }
}


