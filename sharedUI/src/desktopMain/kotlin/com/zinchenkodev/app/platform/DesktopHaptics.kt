package com.zinchenkodev.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object DesktopHaptics : Haptics {
    override fun collisionTick(intensity01: Float) = Unit
}

@Composable
actual fun rememberHaptics(): Haptics = remember { DesktopHaptics }
