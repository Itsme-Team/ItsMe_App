package com.project.itsme

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper
import com.project.itsme.R
class DefaultRepliesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ToggleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_replies)

        // SharedPreferences에서 사용자 이름 가져오기
        val sharedPreferences = getSharedPreferences(KakaoAuthViewModel.PREF_NAME, Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString(KakaoAuthViewModel.USER_NAME, "사용자") ?: "사용자"

        // 설명 텍스트 업데이트
        val descriptionText: TextView = findViewById(R.id.descriptionText)
        descriptionText.text = "   이 답변들이 ${userName}님의 말투로 변환 되어요!"

        // 초기 데이터
        val items = mutableListOf(
            ToggleItem("알겠습니다.", true),
            ToggleItem("가고있습니다", false),
            ToggleItem("무슨일있어요?", false),
            ToggleItem("나중에 연락드릴게요", false),
            ToggleItem("전화로 해주세요", false),
            ToggleItem("알바중이에요", false),
            ToggleItem("운전중입니다", false),
            ToggleItem("회의중입니다", false),
        )

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerViewDefaultReplies)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ToggleListAdapter(items)
        recyclerView.adapter = adapter

        // SpacingItemDecoration 추가
        val spacingInPixels = (15 * resources.displayMetrics.density).toInt() // 16dp를 픽셀로 변환
        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        // ItemTouchHelper 연결
        val itemTouchHelper = ItemTouchHelper(DragDropHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }


        // 두 번째 검색 필드 (추가된 searchEditText)
        val searchEditText: EditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString()) // 검색 입력값에 따라 필터링
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // BottomNavigationView 설정
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        NavigationUtils.setupBottomNavigation(this, bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_replies
    }
}

