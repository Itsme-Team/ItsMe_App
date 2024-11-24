package com.project.kakao_login.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun Kakao_loginTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearAppColorPalette,
        typography = Typography,
        content = content
    )
}