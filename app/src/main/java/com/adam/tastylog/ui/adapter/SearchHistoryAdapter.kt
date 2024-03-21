package com.adam.tastylog.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adam.tastylog.R

class SearchHistoryAdapter(
    private var items: List<String>,
    private val onClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit // 검색 기록 삭제를 위한 콜백 추가
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onClick, onDeleteClick) // bind 메소드에 삭제 콜백 추가
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String, onClick: (String) -> Unit, onDeleteClick: (String) -> Unit) {
            itemView.findViewById<TextView>(R.id.textview_search_keyword).text = item
            itemView.findViewById<TextView>(R.id.textview_search_keyword).setOnClickListener {
                onClick(item)
            }
            // 삭제 아이콘 클릭 리스너 설정
            itemView.findViewById<FrameLayout>(R.id.frame_layout_delete_search_keyword).setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    fun updateItems(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}
