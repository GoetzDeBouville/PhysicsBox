package com.zinchenkodev.app

import androidx.compose.runtime.Composable
import com.zinchenkodev.app.theme.AppTheme

@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {

}
