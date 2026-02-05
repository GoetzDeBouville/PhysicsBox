package com.zinchenkodev.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedback

private class AndroidHaptics(
    private val hapticFeedback: HapticFeedback,
) : Haptics {
    override fun collisionTick(intensity01: Float) {
        val clamped = intensity01.coerceIn(0f, 1f)
        val type = if (clamped >= 0.7f) {
            HapticFeedbackType.LongPress
        } else {
            HapticFeedbackType.TextHandleMove
        }
        hapticFeedback.performHapticFeedback(type)
    }
}

@Composable
actual fun rememberHaptics(): Haptics {
    val haptics = LocalHapticFeedback.current
    return remember(haptics) { AndroidHaptics(haptics) }
}
