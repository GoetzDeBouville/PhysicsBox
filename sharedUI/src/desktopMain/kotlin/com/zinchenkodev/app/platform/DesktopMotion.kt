package com.zinchenkodev.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

private object DesktopMotionProvider : MotionProvider {
    override val isAvailable: Boolean = false

    @Composable
    override fun rememberTiltGravityVector(): State<GravityVector> =
        remember { mutableStateOf(GravityVector(0f, 0f, 0f)) }
}

@Composable
actual fun rememberMotionProvider(): MotionProvider = remember { DesktopMotionProvider }
