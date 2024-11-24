package com.project.kakao_login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ToggleListAdapter(
    private val originalItems: MutableList<ToggleItem> // 원본 데이터를 MutableList로 관리
) : RecyclerView.Adapter<ToggleListAdapter.ToggleViewHolder>() {

    private var filteredItems: MutableList<ToggleItem> = originalItems.toMutableList() // 필터된 데이터도 MutableList로 관리

    class ToggleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.itemText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToggleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_toggle_list, parent, false)
        return ToggleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToggleViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.itemText.text = item.text
    }


    override fun getItemCount(): Int = filteredItems.size

    // 필터링 메서드
    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            originalItems.toMutableList() // 원본 데이터 전체를 다시 표시
        } else {
            originalItems.filter { it.text.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged() // RecyclerView 업데이트
    }

    // 항목 이동 메서드
    fun moveItem(fromPosition: Int, toPosition: Int) {
        // filteredItems에서 순서 변경
        val item = filteredItems.removeAt(fromPosition)
        filteredItems.add(toPosition, item)

        // 원본 리스트에서도 순서 변경
        val originalFromIndex = originalItems.indexOf(item)
        if (originalFromIndex != -1) {
            originalItems.removeAt(originalFromIndex)
            originalItems.add(toPosition, item)
        }

        notifyItemMoved(fromPosition, toPosition) // RecyclerView 업데이트
    }
}
