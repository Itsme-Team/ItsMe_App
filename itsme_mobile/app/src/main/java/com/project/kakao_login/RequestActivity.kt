package com.project.kakao_login

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.wearable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RequestActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private val BASE_URL = "https://itsmeweb.site"

    private lateinit var mMyAPI: RequestAPI
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshButton: ImageButton
    private lateinit var infoButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var defaultCardView: CardView

    private lateinit var dataClient: DataClient
    private var messageVersion: Long = 0
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ChatItemAdapter

    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL: Long = 2000
    private val DELETE_REQUEST_CODE = 1

    private val defaultReplies = listOf(
        "알겠습니다", "가고있습니다", "무슨일이신가요", "곧 연락드리겠습니다",
        "문자 주세요", "나중에 연락드리겠습니다", "알바중입니다", "운전중입니다",
        "회의중입니다", "밥먹고있습니다"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_get)

        // 뷰 초기화
        recyclerView = findViewById(R.id.recyclerView)
        refreshButton = findViewById(R.id.btnRefresh)
        infoButton = findViewById(R.id.btnInfo)
        searchEditText = findViewById(R.id.searchEditText)
        defaultCardView = findViewById(R.id.btnDefault)

        val userNameTextView = findViewById<TextView>(R.id.userNameText)
        kakaoAuthViewModel.getUserName()?.let { username ->
            userNameTextView.text = username
        } ?: run {
            userNameTextView.text = "사용자 이름"
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_home
        NavigationUtils.setupBottomNavigation(this, bottomNavigationView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        sharedPreferences = getSharedPreferences("ChatData", Context.MODE_PRIVATE)
        initRequestAPI(BASE_URL)
        dataClient = Wearable.getDataClient(this)

        // 데이터 로딩 시작
        loadData()

        // 검색 기능 구현
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 클릭 리스너 설정
        infoButton.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        defaultCardView.setOnClickListener {
            val intent = Intent(this, DefaultRepliesActivity::class.java)
            startActivity(intent)
        }

        refreshButton.setOnClickListener {
            fetchData(forceRefresh = true)
        }

        lifecycleScope.launch {
            kakaoAuthViewModel.logoutEvent.collect {
                Toast.makeText(this@RequestActivity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RequestActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadData() {
        // 1. 먼저 SharedPreferences에서 저장된 데이터 확인
        val savedData = sharedPreferences.getString("chatData", null)

        if (savedData != null && savedData.isNotEmpty()) {
            // 2. 저장된 데이터가 있으면 바로 표시
            Log.d(TAG, "Loading data from SharedPreferences")
            val chatItems = parseSavedData(savedData)
            if (chatItems.isNotEmpty()) {
                Log.d(TAG, "Found ${chatItems.size} items in SharedPreferences")
                updateRecyclerView(chatItems)
                sendSavedDataToWear(savedData)
            } else {
                // 저장된 데이터가 비어있으면 서버에서 새로 가져옴
                Log.d(TAG, "Saved data was empty, fetching from server")
                fetchData(forceRefresh = false)
            }
        } else {
            // 3. 저장된 데이터가 없으면 서버에서 가져옴
            Log.d(TAG, "No saved data found, fetching from server")
            fetchData(forceRefresh = false)
        }
    }

    private fun showDefaultReplies() {
        val replyList = defaultReplies.joinToString("\n")
        AlertDialog.Builder(this)
            .setTitle("기본 답변 리스트")
            .setMessage(replyList)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun initRequestAPI(baseUrl: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mMyAPI = retrofit.create(RequestAPI::class.java)
    }

    private fun parseSavedData(savedData: String): List<ChatItem> {
        val lines = savedData.split("\n")
        val chatItems = mutableListOf<ChatItem>()
        var currentRoom = ""
        var currentReplyList = ""
        var currentIsEnabled = true
        var currentSavedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        for (line in lines) {
            when {
                line.startsWith("room: ") -> {
                    if (currentRoom.isNotEmpty()) {
                        chatItems.add(ChatItem(
                            currentRoom,
                            currentReplyList,
                            currentIsEnabled,
                            currentSavedDate
                        ))
                        currentReplyList = ""
                        currentIsEnabled = true
                    }
                    currentRoom = line.substringAfter("room: ")
                }
                line.startsWith("reply_list: ") -> {
                    currentReplyList = line.substringAfter("reply_list: ")
                }
                line.startsWith("is_enabled: ") -> {
                    currentIsEnabled = line.substringAfter("is_enabled: ").toBoolean()
                }
                line.startsWith("saved_date: ") -> {
                    currentSavedDate = line.substringAfter("saved_date: ")
                }
            }
        }
        if (currentRoom.isNotEmpty()) {
            chatItems.add(ChatItem(
                currentRoom,
                currentReplyList,
                currentIsEnabled,
                currentSavedDate
            ))
        }
        return chatItems
    }

    private fun updateRecyclerView(chatItems: List<ChatItem>) {
        adapter = ChatItemAdapter(
            chatItems,
            onItemClick = { item ->
                val intent = Intent(this, ReplyListActivity::class.java).apply {
                    putExtra("ROOM_NAME", item.room)
                    putExtra("REPLY_LIST", item.reply_list)
                    putExtra("IS_ENABLED", item.isEnabled)
                    putExtra("SAVED_DATE", item.savedDate)
                }
                startActivityForResult(intent, DELETE_REQUEST_CODE)
            },
            onToggleChanged = { item, isChecked ->
                val updatedItems = adapter.getItems().map {
                    if (it.room == item.room) {
                        it.copy(isEnabled = isChecked)
                    } else {
                        it
                    }
                }
                adapter.updateItems(updatedItems)
                saveDataToSharedPreferences(updatedItems)
                sendSavedDataToWear(convertChatItemsToString(updatedItems))
            },
            defaultReplies = defaultReplies
        )
        recyclerView.adapter = adapter
    }

    private fun sendSavedDataToWear(savedData: String) {
        val lines = savedData.split("\n")
        val roomList = StringBuilder()
        val replyList = StringBuilder()
        var isRoom = true
        for (line in lines) {
            if (line.startsWith("room: ")) {
                roomList.append(line.substringAfter("room: ")).append("<sep>")
                isRoom = false
            } else if (line.startsWith("reply_list: ")) {
                replyList.append(line.substringAfter("reply_list: "))
                isRoom = true
            }
        }
        sendRepListMessage(roomList.toString(), replyList.toString())
    }

    private fun sendRepListMessage(room: String, reply_list: String) {
        val putDataMapRequest = PutDataMapRequest.create("/reply_list").apply {
            dataMap.putString("room", room)
            dataMap.putString("reply_list", reply_list)
            dataMap.putLong("time", System.currentTimeMillis())
            dataMap.putLong("version", messageVersion++)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        dataClient.putDataItem(putDataRequest)
        Log.d("Android -> Wear Message sent", "room : $room, reply_list : $reply_list, version : $messageVersion")
    }

    private fun updateAndSaveData(newItems: List<ChatItem>) {
        val existingData = sharedPreferences.getString("chatData", null)
        val existingItems = if (existingData != null) parseSavedData(existingData) else emptyList()

        // room을 키로 사용하는 Map으로 변환하여 중복 처리
        val itemsMap = existingItems.associateBy { it.room.trim() }.toMutableMap()

        // 새로운 아이템은 기존에 없을 때만 현재 시간으로 저장
        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        newItems.forEach { newItem ->
            val trimmedRoom = newItem.room.trim()
            if (!itemsMap.containsKey(trimmedRoom)) {
                // 새로운 아이템만 현재 시간으로 저장
                itemsMap[trimmedRoom] = newItem.copy(savedDate = currentDateTime)
            }
            // 기존 아이템은 원래 저장된 시간 유지
        }

        val mergedItems = itemsMap.values.toList()

        val result = convertChatItemsToString(mergedItems)
        sharedPreferences.edit().putString("chatData", result).apply()
        updateRecyclerView(mergedItems)
        sendSavedDataToWear(result)

        Log.d(TAG, "Existing items count: ${existingItems.size}")
        Log.d(TAG, "New unique items added: ${mergedItems.size - existingItems.size}")
        Log.d(TAG, "Total items after merge: ${mergedItems.size}")
    }

    private fun fetchData(forceRefresh: Boolean) {
        lifecycleScope.launch {
            try {
                val userId = withContext(Dispatchers.IO) {
                    kakaoAuthViewModel.getUserId()
                }

                if (userId.isNullOrEmpty()) {
                    Log.e(TAG, "User ID is null or empty")
                    return@launch
                }

                val headers = HashMap<String, String>()
                headers["user-id"] = userId

                Log.d(TAG, "Fetching data from server for user: $userId")

                val response = withContext(Dispatchers.IO) {
                    mMyAPI.getPosts(headers).execute()
                }

                if (response.isSuccessful) {
                    val mList = response.body()
                    if (mList != null) {
                        Log.d(TAG, "Received ${mList.size} items from server")
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val chatItems = mList.map {
                            ChatItem(
                                room = it.room,
                                reply_list = it.reply_list,
                                savedDate = currentDate
                            )
                        }

                        // 항상 기존 데이터와 병합하도록 수정
                        updateAndSaveData(chatItems)

                        Log.d(TAG, "Data merged and saved successfully")
                    } else {
                        Log.d(TAG, "Server returned null data")
                    }
                } else {
                    Log.e(TAG, "Server response unsuccessful: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RequestActivity,
                        "데이터를 불러오는데 실패했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun deleteItem(item: ChatItem) {
        lifecycleScope.launch {
            val userId = withContext(Dispatchers.IO) {
                kakaoAuthViewModel.getUserId()
            }

            val deleteRequest = DeleteRequest(userId ?: "", item.room)

            Log.d(TAG, "Sending delete request - User ID: ${userId ?: ""}, Room: ${item.room}")

            try {
                val response = withContext(Dispatchers.IO) {
                    mMyAPI.delete(deleteRequest).execute()
                }
                if (response.isSuccessful) {
                    val updatedItems = adapter.getItems().filterNot { it == item }
                    adapter.updateItems(updatedItems)
                    saveDataToSharedPreferences(updatedItems)
                    sendSavedDataToWear(convertChatItemsToString(updatedItems))
                    Toast.makeText(this@RequestActivity, "항목이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                } else
                    Log.d(TAG, "Delete failed. Status Code : ${response.code()}")
            } catch (e: Exception) {
                Log.d(TAG, "Delete failed. Error: ${e.message}")
                Toast.makeText(this@RequestActivity, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDataToSharedPreferences(items: List<ChatItem>) {
        val result = convertChatItemsToString(items)
        sharedPreferences.edit().putString("chatData", result).apply()
    }

    private fun convertChatItemsToString(items: List<ChatItem>): String {
        return items.joinToString("\n") { item ->
            """
           room: ${item.room}
           reply_list: ${item.reply_list}
           is_enabled: ${item.isEnabled}
           saved_date: ${item.savedDate}
           """.trimIndent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val roomName = data?.getStringExtra("ROOM_NAME")
            val updatedReplyList = data?.getStringExtra("REPLY_LIST")
            val isEnabled = data?.getBooleanExtra("IS_ENABLED", true) ?: true

            if (roomName != null && updatedReplyList != null) {
                val formattedReplyList = "['" + updatedReplyList.split(",").joinToString("', '") + "']"

                val updatedItems = adapter.getItems().map {
                    if (it.room == roomName) {
                        it.copy(reply_list = formattedReplyList, isEnabled = isEnabled)
                    } else {
                        it
                    }
                }
                adapter.updateItems(updatedItems)
                saveDataToSharedPreferences(updatedItems)
                sendSavedDataToWear(convertChatItemsToString(updatedItems))

                Log.d("RequestActivity", "Updated reply list for room $roomName: $formattedReplyList")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reply_list_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity() // 앱 종료
        } else {
            Toast.makeText(this, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}

data class ChatItem(
    val room: String,
    val reply_list: String,
    var isEnabled: Boolean = true,
    val savedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)