package com.project.itsme.presentation

import android.content.Intent
//import android.content.res.Resources.Theme
import android.os.Bundle
//import android.util.Log
//import android.widget.Button
import android.widget.Toast
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import androidx.activity.ComponentActivity
//import com.project.kakao_login.R

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.project.itsme.presentation.theme.Kakao_loginTheme

//import android.os.Message
//import androidx.activity.compose.setContent
//import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.material.Chip
//import androidx.compose.material.icons.Icons

import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Devices
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed

import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
//import androidx.wear.compose.material.AppCard
//import androidx.wear.compose.material.Icon
//import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.project.itsme.presentation.theme.Typography
//import com.project.kakao_login.presentation.theme.Kakao_loginTheme


class ButtonActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Kakao_loginTheme {
                ButtonScreen()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMainActivity()
    }

    @Composable
    fun ButtonScreen() {
        val listState = rememberScalingLazyListState()
        val context = LocalContext.current
        val replyList = remember { MainActivity.replyList }
        val senderList = remember { MainActivity.senderList }
        val sender = remember { MainActivity.MessageSender }
        val senderGroup = remember{MainActivity.MessageSenderGroup}
        val defaultReplyList = listOf(
            "알겠습니다",
            "가고 있습니다.",
            "무슨 일이에요?",
            "나중에 연락드릴게요",
            "전화로 해주세요",
            "알바 중이에요",
            "운전 중입니다",
            "회의 중입니다"
        )

        // 찾고자 하는 sender의 인덱스 찾기
        val senderIndex = senderList.indexOf(sender)

        // 버튼 리스트 설정
        val buttons = if (senderIndex != -1 && senderIndex < replyList.size) {
            replyList[senderIndex].take(8) // sender에 해당하는 리스트를 최대 8개까지 가져옴
        } else {
            defaultReplyList.take(8) // 기본 리스트에서 최대 8개까지 가져옴
        }

        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Add a spacer to create space between the time and the first button
                item {
                    Spacer(modifier = Modifier.height(16.dp)) // Adjust the height as needed
                }

                itemsIndexed(buttons) { index, buttonText ->
                    DynamicButton(buttonText, context) {
                        if (sender != null && senderGroup != null){
                            sendMessage(buttonText, context, sender, senderGroup)
                            Toast.makeText(context, "$sender 에게 $buttonText 전송되었습니다.", Toast.LENGTH_SHORT).show()
                            navigateToHomeScreen()
                        }else{
                            Toast.makeText(context, "발신자 정보가 없습니다./$sender/$senderGroup/", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DynamicButton(text: String, context: android.content.Context, onClickAction: () -> Unit) {
        androidx.wear.compose.material.Chip(
            onClick = onClickAction,
            label = {
                Text(
                    text = text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography.body2
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .heightIn(max = 50.dp)
        )
    }

    private fun sendMessage(text: String, context: android.content.Context, sender : String, senderGroup : String) {
        val putDataMapRequest = PutDataMapRequest.create("/button_text").apply {
            dataMap.putString("button_text", text)
            dataMap.putLong("version", System.currentTimeMillis()) // 중복메세지를 피하기 위해서 타임스탬프 추가
            dataMap.putString("sender", sender)
            dataMap.putString("senderGroup", senderGroup)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(putDataRequest)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun ButtonActivity.navigateToHomeScreen() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}