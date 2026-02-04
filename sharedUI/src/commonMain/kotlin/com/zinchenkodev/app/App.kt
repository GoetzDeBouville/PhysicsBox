package com.zinchenkodev.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zinchenkodev.app.theme.AppTheme

@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
) = AppTheme(onThemeChanged) {
    var spawnVersion by remember { mutableIntStateOf(0) }
    var resetVersion by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }

    val controls = remember(spawnVersion, resetVersion, paused) {
        PhysicsBoxDemoControls(
            spawnVersion = spawnVersion,
            resetVersion = resetVersion,
            paused = paused,
        )
    }

    CompositionLocalProvider(LocalPhysicsBoxDemoControls provides controls) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { spawnVersion++ }) {
                    Text("Spawn")
                }
                Button(onClick = {
                    resetVersion++
                    paused = false
                }) {
                    Text("Reset")
                }
                Button(onClick = { paused = !paused }) {
                    Text(if (paused) "Resume" else "Pause")
                }
                Text(
                    text = if (paused) "Paused" else "Running",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(10.dp),
            ) {
                PhysicsBoxDemo(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
