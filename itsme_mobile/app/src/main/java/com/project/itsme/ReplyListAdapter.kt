package com.project.itsme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReplyListAdapter(
    private val replyList: List<String>,
    private val onEditClick: (Int) -> Unit
) : RecyclerView.Adapter<ReplyListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val replyText: TextView = view.findViewById(R.id.tvReplyText)
        val editIcon: ImageView = view.findViewById(R.id.ivEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reply_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.replyText.text = replyList[position]
        holder.editIcon.setOnClickListener {
            onEditClick(position)
        }
    }

    override fun getItemCount() = replyList.size
}