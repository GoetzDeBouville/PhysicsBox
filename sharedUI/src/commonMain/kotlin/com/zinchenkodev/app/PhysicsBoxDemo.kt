package com.zinchenkodev.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

@Immutable
internal data class PhysicsBoxDemoControls(
    val spawnVersion: Int = 0,
    val resetVersion: Int = 0,
    val paused: Boolean = false,
)

internal val LocalPhysicsBoxDemoControls = staticCompositionLocalOf { PhysicsBoxDemoControls() }
