package com.project.kakao_login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class ChatItemAdapter(
    private var items: List<ChatItem>,
    private val onItemClick: (ChatItem) -> Unit,
    private val onToggleChanged: (ChatItem, Boolean) -> Unit,
    private val defaultReplies: List<String>
) : RecyclerView.Adapter<ChatItemAdapter.ViewHolder>() {

    private var filteredItems = items.toList()

    inner class ViewHolder(private val binding: View) : RecyclerView.ViewHolder(binding) {
        private val ivUserIcon: ImageView = binding.findViewById(R.id.ivUserIcon)
        private val tvChatRoomName: TextView = binding.findViewById(R.id.tvChatRoomName)
//        private val switchToggle: SwitchCompat = binding.findViewById(R.id.switchToggle)

        fun bind(item: ChatItem) {
            tvChatRoomName.text = item.room
//            switchToggle.isChecked = item.isEnabled

            // 아이템 클릭 리스너
            binding.setOnClickListener {
                onItemClick(item)
            }

            // 스위치 토글 리스너
//            switchToggle.setOnCheckedChangeListener { _, isChecked ->
//                onToggleChanged(item, isChecked)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_room_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position])
    }

    override fun getItemCount() = filteredItems.size

    fun getItems() = items

    fun updateItems(newItems: List<ChatItem>) {
        items = newItems
        filter(lastFilter)
    }

    private var lastFilter = ""

    fun filter(query: String) {
        lastFilter = query
        filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter { it.room.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}