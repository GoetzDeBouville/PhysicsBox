package com.zinchenkodev.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zinchenkodev.app.demos.AirHockeyDemoScreen
import com.zinchenkodev.app.demos.BasicStackingDemoScreen
import com.zinchenkodev.app.demos.DemoHomeScreen
import com.zinchenkodev.app.demos.MaterialMixDemoScreen
import com.zinchenkodev.app.demos.TiltGravityDemoScreen
import com.zinchenkodev.app.demos.PingPongDemoScreen
import com.zinchenkodev.app.platform.rememberHaptics
import com.zinchenkodev.app.platform.rememberMotionProvider
import com.zinchenkodev.app.theme.AppTheme

sealed interface DemoRoute {
    data object Home : DemoRoute
    data object Basic : DemoRoute
    data object Tilt : DemoRoute
    data object AirHockey : DemoRoute
    data object MaterialMix : DemoRoute
    data object PingPong : DemoRoute
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("d")
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
) = AppTheme(onThemeChanged) {
    var route by remember { mutableStateOf<DemoRoute>(DemoRoute.Home) }
    val motionProvider = rememberMotionProvider()
    val haptics = rememberHaptics()
    var resetSignal by remember(route) { mutableIntStateOf(0) }

    val title = when (route) {
        DemoRoute.Home -> "PhysicsBox Demo Gallery"
        DemoRoute.Basic -> "Baseline stacking"
        DemoRoute.Tilt -> "Tilt gravity (Android)"
        DemoRoute.AirHockey -> "Air hockey"
        DemoRoute.MaterialMix -> "Material mix"
        DemoRoute.PingPong -> "Ping Pong"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (route != DemoRoute.Home) {
                        TextButton(onClick = { route = DemoRoute.Home }) {
                            Text("Back")
                        }
                    }
                },
                actions = {
                    if (route != DemoRoute.Home) {
                        TextButton(onClick = { resetSignal++ }) {
                            Text("Reset")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (route) {
                DemoRoute.Home -> DemoHomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    tiltAvailable = motionProvider.isAvailable,
                    onSelect = { route = it },
                )

                DemoRoute.Basic -> BasicStackingDemoScreen(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    resetSignal = resetSignal,
                )

                DemoRoute.Tilt -> TiltGravityDemoScreen(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    resetSignal = resetSignal,
                    motionProvider = motionProvider,
                    haptics = haptics,
                )

                DemoRoute.AirHockey -> AirHockeyDemoScreen(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    resetSignal = resetSignal,
                )

                DemoRoute.MaterialMix -> MaterialMixDemoScreen(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    resetSignal = resetSignal,
                )

                DemoRoute.PingPong -> PingPongDemoScreen(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    resetSignal = resetSignal,
                    haptics = haptics,
                )
            }
        }
    }
}
