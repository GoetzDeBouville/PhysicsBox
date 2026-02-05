package com.zinchenkodev.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

@Immutable
data class GravityVector(
    val x: Float,
    val y: Float,
    val z: Float,
)

@Stable
interface MotionProvider {
    val isAvailable: Boolean

    @Composable
    fun rememberTiltGravityVector(): State<GravityVector>
}

@Stable
interface Haptics {
    fun collisionTick(intensity01: Float)
}

@Composable
expect fun rememberMotionProvider(): MotionProvider

@Composable
expect fun rememberHaptics(): Haptics
