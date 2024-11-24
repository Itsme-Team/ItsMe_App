package com.project.kakao_login

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragDropHelperCallback(
    private val adapter: ToggleListAdapter
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // 드래그 동작을 활성화 (위/아래 방향)
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0) // 스와이프 동작은 0으로 비활성화
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // 어댑터에 드래그된 위치 변경 요청
        adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프 동작은 처리하지 않음
    }

    override fun isLongPressDragEnabled(): Boolean {
        // 항목을 길게 눌러 드래그 활성화
        return true
    }
}