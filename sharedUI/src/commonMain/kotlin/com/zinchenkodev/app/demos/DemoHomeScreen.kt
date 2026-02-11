package com.zinchenkodev.app.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zinchenkodev.app.DemoRoute

@Immutable
private data class DemoEntry(
    val title: String,
    val description: String,
    val route: DemoRoute,
)

@Composable
fun DemoHomeScreen(
    modifier: Modifier = Modifier,
    tiltAvailable: Boolean,
    onSelect: (DemoRoute) -> Unit,
) {

    val entries = buildList {
        add(
            DemoEntry(
                title = "PhysicsBoxState API",
                description = "Hands-on controls for pause, gravity, step config, solver, and commands.",
                route = DemoRoute.StateApi,
            ),
        )
        add(
            DemoEntry(
                title = "Boundaries config",
                description = "Tune wall restitution, friction, thickness, and toggle boundaries.",
                route = DemoRoute.Boundaries,
            ),
        )
        add(
            DemoEntry(
                title = "Ping Pong",
                description = "Retro ping-pong with AI opponent and PhysicsBox collisions.",
                route = DemoRoute.PingPong,
            ),
        )
        add(
            DemoEntry(
                title = "Baseline stacking",
                description = "Baseline PhysicsBox scene with draggable bodies and boundaries.",
                route = DemoRoute.Basic,
            ),
        )
        add(
            DemoEntry(
                title = "Air hockey",
                description = "Zero-gravity scene with paddles and puck using drag + fling.",
                route = DemoRoute.AirHockey,
            ),
        )
        add(
            DemoEntry(
                title = "Material mix",
                description = "Side-by-side materials with different friction, restitution, and gravityScale.",
                route = DemoRoute.MaterialMix,
            ),
        )
        if (tiltAvailable) {
            add(
                DemoEntry(
                    title = "Tilt gravity (Android)",
                    description = "Device tilt controls gravity, with collision haptics.",
                    route = DemoRoute.Tilt,
                ),
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = 16.dp, vertical = 20.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Demo gallery",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Open a scenario to explore PhysicsBox behavior, drag interactions, and constraints.",
            style = MaterialTheme.typography.bodyMedium,
        )

        entries.forEach { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelect(entry.route) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (tiltAvailable.not()) {
            Text(
                text = "Tilt gravity demo appears on Android devices with motion sensors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
