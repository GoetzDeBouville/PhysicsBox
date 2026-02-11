package com.zinchenkodev.app.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.zinchenko.physicsbox.BoundariesConfig
import dev.zinchenko.physicsbox.PhysicsBoxConfig
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.rememberPhysicsBoxState

@Immutable
private data class BoundaryBody(
    val key: String,
    val label: String,
    val widthDp: Dp,
    val heightDp: Dp,
    val shape: PhysicsShape,
    val startXpx: Float,
    val startYpx: Float,
    val rotationDegrees: Float,
    val color: Color,
)

@Suppress("d")
@Composable
fun BoundariesConfigDemoScreen(
    modifier: Modifier = Modifier,
    resetSignal: Int = 0,
) {
    val state = rememberPhysicsBoxState()
    val bodies = remember { buildBoundaryBodies() }

    var enabled by remember { mutableStateOf(true) }
    var restitution by remember { mutableFloatStateOf(0.2f) }
    var friction by remember { mutableFloatStateOf(0.3f) }
    var thickness by remember { mutableFloatStateOf(64f) }

    LaunchedEffect(resetSignal) {
        if (resetSignal <= 0) return@LaunchedEffect
        state.reset()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ControlCard(title = "Wall settings") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                )
                Text("Enabled")
            }
            Text("Restitution: ${formatFloat(restitution)}")
            Slider(
                value = restitution,
                onValueChange = { restitution = it },
                valueRange = 0f..1.2f,
            )
            Text("Friction: ${formatFloat(friction)}")
            Slider(
                value = friction,
                onValueChange = { friction = it },
                valueRange = 0f..1000f,
            )
            Text("Thickness: ${formatFloat(thickness)} px")
            Slider(
                value = thickness,
                onValueChange = { thickness = it },
                valueRange = 0.01f..1024f,
            )
            Text(
                text = "Thickness is wall depth. Larger values help prevent tunneling on fast motion.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ControlCard(title = "Actions") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        bodies.forEach { body ->
                            state.enqueueImpulse(
                                key = body.key,
                                impulseX = 0f,
                                impulseY = -1_200f,
                            )
                        }
                    },
                ) {
                    Text("Impulse Up")
                }
                Button(
                    onClick = {
                        val strong = 18_000f
                        val diagonal = 12_000f
                        bodies.forEachIndexed { index, body ->
                            val impulse = when (index % 4) {
                                0 -> PhysicsVector2(strong, -diagonal)
                                1 -> PhysicsVector2(-strong, -diagonal)
                                2 -> PhysicsVector2(strong, diagonal)
                                else -> PhysicsVector2(-strong, diagonal)
                            }
                            state.enqueueImpulse(
                                key = body.key,
                                impulseX = impulse.x,
                                impulseY = impulse.y,
                            )
                        }
                    },
                ) {
                    Text("Stress Test")
                }
                TextButton(onClick = { state.reset() }) {
                    Text("Reset bodies")
                }
            }
        }

        PhysicsBox(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp, max = 520.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFEFF3F7))
                .padding(10.dp),
            state = state,
            config = PhysicsBoxConfig(
                boundaries = BoundariesConfig(
                    enabled = enabled,
                    restitution = restitution,
                    friction = friction,
                    thicknessPx = thickness,
                ),
            ),
        ) {
            bodies.forEach { body ->
                val isCircle = body.shape is PhysicsShape.Circle
                val clipShape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)
                val contentColor = if (body.color.luminance() > 0.5f) Color.Black else Color.White

                Column(
                    modifier = Modifier
                        .size(width = body.widthDp, height = body.heightDp)
                        .physicsBody(
                            key = body.key,
                            config = PhysicsBodyConfig(
                                bodyType = BodyType.Dynamic,
                                density = 1f,
                                friction = 0.45f,
                                restitution = 0.4f,
                                linearDamping = 0.04f,
                                angularDamping = 0.08f,
                                initialTransform = PhysicsTransform(
                                    vector2 = PhysicsVector2(body.startXpx, body.startYpx),
                                    rotationDegrees = body.rotationDegrees,
                                ),
                            ),
                            shape = body.shape,
                            isDraggable = true,
                        )
                        .clip(clipShape)
                        .background(body.color)
                        .border(1.dp, Color(0x33000000), clipShape),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = body.label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
        )
        content()
    }
}

private fun buildBoundaryBodies(): List<BoundaryBody> = listOf(
    BoundaryBody(
        key = "boundary-a",
        label = "A",
        widthDp = 86.dp,
        heightDp = 66.dp,
        shape = PhysicsShape.Box,
        startXpx = 110f,
        startYpx = 90f,
        rotationDegrees = 8f,
        color = Color(0xFF4E9CB5),
    ),
    BoundaryBody(
        key = "boundary-b",
        label = "B",
        widthDp = 72.dp,
        heightDp = 72.dp,
        shape = PhysicsShape.Circle(),
        startXpx = 220f,
        startYpx = 90f,
        rotationDegrees = 0f,
        color = Color(0xFFE07A5F),
    ),
    BoundaryBody(
        key = "boundary-c",
        label = "C",
        widthDp = 96.dp,
        heightDp = 64.dp,
        shape = PhysicsShape.Box,
        startXpx = 150f,
        startYpx = 170f,
        rotationDegrees = -12f,
        color = Color(0xFF7B9E59),
    ),
    BoundaryBody(
        key = "boundary-d",
        label = "D",
        widthDp = 70.dp,
        heightDp = 70.dp,
        shape = PhysicsShape.Circle(),
        startXpx = 250f,
        startYpx = 170f,
        rotationDegrees = 0f,
        color = Color(0xFF9B6FA6),
    ),
)

private fun formatFloat(value: Float): String = "%.2f".format(value)
