package com.project.kakao_login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReplyListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReplyListAdapter
    private lateinit var roomName: String
    private var isEnabled: Boolean = true
    private var replyList: MutableList<String> = mutableListOf()
    private var originalReplyList: List<String> = listOf()
    private lateinit var btnSaveChanges: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply_list)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        roomName = intent.getStringExtra("ROOM_NAME") ?: "Unknown Room"
        val replyListString = intent.getStringExtra("REPLY_LIST") ?: "[]"
        isEnabled = intent.getBooleanExtra("IS_ENABLED", true)
        val savedDate = intent.getStringExtra("SAVED_DATE") ?: ""
        title = roomName
        findViewById<TextView>(R.id.tvSavedDate).text = "$savedDate"
        val tvRoomName = findViewById<TextView>(R.id.tvRoomName)
        tvRoomName.text = roomName

        recyclerView = findViewById(R.id.replyListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        originalReplyList = parseReplyList(replyListString)
        replyList = originalReplyList.toMutableList()
        adapter = ReplyListAdapter(replyList, ::onReplyItemClick)
        recyclerView.adapter = adapter

        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnSaveChanges.visibility = View.GONE

        btnSaveChanges.isEnabled = isEnabled
        btnSaveChanges.alpha = if (isEnabled) 1.0f else 0.5f

        btnSaveChanges.setOnClickListener {
            if (isEnabled) {
                saveChanges()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reply_list_menu, menu)
        menu.findItem(R.id.action_save)?.isEnabled = isEnabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save -> {
                if (isEnabled) {
                    saveChanges()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onReplyItemClick(position: Int) {
        if (!isEnabled) {
            Toast.makeText(this, "토글이 비활성화된 상태에서는 수정할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentReply = replyList[position]
        val editText = EditText(this).apply {
            setText(currentReply)
        }

        AlertDialog.Builder(this)
            .setTitle("답변 수정")
            .setView(editText)
            .setPositiveButton("확인") { _, _ ->
                val newReply = editText.text.toString()
                if (newReply.isNotEmpty()) {
                    replyList[position] = newReply
                    adapter.notifyItemChanged(position)
                    checkForChanges()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun checkForChanges() {
        val hasChanges = !replyList.equals(originalReplyList)
        btnSaveChanges.visibility = if (hasChanges) View.VISIBLE else View.GONE
    }

    private fun saveChanges() {
        val intent = Intent().apply {
            putExtra("ROOM_NAME", roomName)
            putExtra("REPLY_LIST", replyList.joinToString(","))
            putExtra("IS_ENABLED", isEnabled)
        }
        setResult(Activity.RESULT_OK, intent)
        Toast.makeText(this, "변경 사항이 저장되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun parseReplyList(replyListString: String): List<String> {
        return replyListString.replace("[", "").replace("]", "").replace("'","").split(",").map { it.trim() }
    }
}