package com.zinchenkodev.app.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.zinchenko.physicsbox.PhysicsBoxState
import dev.zinchenko.physicsbox.PhysicsCommand
import dev.zinchenko.physicsbox.PhysicsDefaults
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.PhysicsWorldSnapshot
import dev.zinchenko.physicsbox.StepConfig
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.rememberPhysicsBoxState
import kotlin.math.roundToInt

@Immutable
private data class StateDemoBody(
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
fun PhysicsBoxStateDemoScreen(
    modifier: Modifier = Modifier,
    resetSignal: Int = 0,
) {
    val state = rememberPhysicsBoxState()
    val bodies = remember { buildStateDemoBodies() }

    var selectedKey by remember { mutableStateOf(bodies.first().key) }
    var gravityInputX by remember { mutableFloatStateOf(state.gravity.x) }
    var gravityInputY by remember { mutableFloatStateOf(state.gravity.y) }

    var lastSnapshot by remember { mutableStateOf<PhysicsWorldSnapshot?>(null) }

    LaunchedEffect(resetSignal) {
        if (resetSignal <= 0) return@LaunchedEffect
        state.reset()
        gravityInputX = state.gravity.x
        gravityInputY = state.gravity.y
        lastSnapshot = null
    }

    fun applyGravityInputWithUpdate() {
        state.updateGravity(gravityInputX, gravityInputY)
    }

    fun applyGravityInputWithSetWorld() {
        state.setWorldGravity(PhysicsVector2(gravityInputX, gravityInputY))
    }

    fun applyPresetWithUpdateVector(x: Float, y: Float) {
        gravityInputX = x
        gravityInputY = y
        state.updateGravity(PhysicsVector2(x, y))
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = "Gravity") {
            Text("Input X: ${formatFloat(gravityInputX)}")
            Slider(
                value = gravityInputX,
                onValueChange = { gravityInputX = it },
                valueRange = -20f..20f,
            )
            Text("Input Y: ${formatFloat(gravityInputY)}")
            Slider(
                value = gravityInputY,
                onValueChange = { gravityInputY = it },
                valueRange = -20f..20f,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { applyGravityInputWithUpdate() }) {
                    Text("Apply updateGravity")
                }
                TextButton(onClick = { applyGravityInputWithSetWorld() }) {
                    Text("Apply setWorldGravity")
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    applyPresetWithUpdateVector(
                        0f,
                        PhysicsDefaults.Gravity.y
                    )
                }) {
                    Text("Preset: Down")
                }
                TextButton(onClick = {
                    applyPresetWithUpdateVector(
                        0f,
                        -PhysicsDefaults.Gravity.y
                    )
                }) {
                    Text("Preset: Up")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    applyPresetWithUpdateVector(
                        -PhysicsDefaults.Gravity.y,
                        0f
                    )
                }) {
                    Text("Preset: Left")
                }
                TextButton(onClick = { applyPresetWithUpdateVector(0f, 0f) }) {
                    Text("Preset: Zero")
                }
            }
        }

        SectionCard(title = "Step + Solver") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { state.updateStepConfig(stepPresetLowPower(state)) }) {
                    Text("30 Hz")
                }
                Button(onClick = { state.updateStepConfig(stepPresetDefault(state)) }) {
                    Text("60 Hz")
                }
                Button(onClick = { state.updateStepConfig(stepPresetSmooth(state)) }) {
                    Text("120 Hz")
                }
            }
            Spacer(modifier = Modifier.size(2.dp))
            val velocityIterations = state.stepConfig.velocityIterations
            val positionIterations = state.stepConfig.positionIterations
            Text("Velocity iterations: $velocityIterations")
            Slider(
                value = velocityIterations.toFloat(),
                onValueChange = { next ->
                    val v = next.roundToInt().coerceAtLeast(1)
                    state.setSolverIterations(v, positionIterations)
                },
                valueRange = 1f..20f,
                steps = 18,
            )
            Text("Position iterations: $positionIterations")
            Slider(
                value = positionIterations.toFloat(),
                onValueChange = { next ->
                    val p = next.roundToInt().coerceAtLeast(1)
                    state.setSolverIterations(velocityIterations, p)
                },
                valueRange = 1f..20f,
                steps = 18,
            )
        }

        SectionCard(title = "Commands") {
            Text("Target body:")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                bodies.forEach { body ->
                    TextButton(onClick = { selectedKey = body.key }) {
                        val marker = if (selectedKey == body.key) "✓ " else ""
                        Text("$marker${body.label}")
                    }
                }
            }
            Text(
                text = "Impulse is in px-scaled units. Velocity is in px/sec.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        state.enqueueImpulse(
                            key = selectedKey,
                            impulseX = 0f,
                            impulseY = -1_200f,
                        )
                    },
                ) {
                    Text("Impulse Up")
                }
                Button(
                    onClick = {
                        state.enqueueImpulse(
                            key = selectedKey,
                            impulseX = 900f,
                            impulseY = 0f,
                        )
                    },
                ) {
                    Text("Impulse Right")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        state.enqueueVelocity(
                            key = selectedKey,
                            velocityX = 1_000f,
                            velocityY = 0f,
                        )
                    },
                ) {
                    Text("Velocity Right")
                }
                TextButton(
                    onClick = {
                        state.enqueueVelocity(
                            key = selectedKey,
                            velocityX = 0f,
                            velocityY = 0f,
                        )
                    },
                ) {
                    Text("Velocity Stop")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { state.enqueueCommand(PhysicsCommand.ResetWorld) },
                ) {
                    Text("enqueueCommand(ResetWorld)")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        state.enqueueCommand(
                            PhysicsCommand.SetWorldGravity(
                                PhysicsVector2(gravityInputX, gravityInputY),
                            ),
                        )
                    },
                ) {
                    Text("enqueueCommand(SetWorldGravity)")
                }
            }
        }

        SectionCard(title = "Snapshot") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { lastSnapshot = state.snapshot() }) {
                    Text("Capture snapshot")
                }
                TextButton(onClick = { lastSnapshot = null }) {
                    Text("Clear")
                }
            }
            val snapshot = lastSnapshot
            if (snapshot == null) {
                Text(
                    text = "Snapshot is empty. Press Capture to read PhysicsBoxState.snapshot().",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "Snapshot: paused=${snapshot.isPaused}, gravity=${formatVector(snapshot.gravity)}",
                )
                Text(
                    text = "Snapshot step: ${formatFloat(snapshot.stepConfig.hz)} Hz, " +
                            "solver v${snapshot.solverIterations.velocity} / p${snapshot.solverIterations.position}",
                )
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
                    enabled = true,
                    restitution = 0.23f,
                    friction = 0.9f,
                    thicknessPx = 56f,
                ),
            ),
        ) {
            bodies.forEach { body ->
                val isCircle = body.shape is PhysicsShape.Circle
                val clipShape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)
                val isSelected = body.key == selectedKey
                val borderColor = if (isSelected) Color(0xFF1E88E5) else Color(0x33000000)
                val borderWidth = if (isSelected) 2.dp else 1.dp

                Column(
                    modifier = Modifier
                        .size(width = body.widthDp, height = body.heightDp)
                        .physicsBody(
                            key = body.key,
                            config = PhysicsBodyConfig(
                                bodyType = BodyType.Dynamic,
                                density = 1f,
                                friction = 0.35f,
                                allowSleep = false,
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
                        .border(borderWidth, borderColor, clipShape),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = body.label,
                        color = if (body.color.luminance() > 0.5f) Color.Black else Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
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

private fun stepPresetBase(state: PhysicsBoxState): StepConfig = state.stepConfig

private fun stepPresetLowPower(state: PhysicsBoxState): StepConfig =
    stepPresetBase(state).copy(
        hz = 30f,
        maxSubSteps = 2,
        maxDeltaSeconds = 1f / 20f,
    )

private fun stepPresetDefault(state: PhysicsBoxState): StepConfig =
    stepPresetBase(state).copy(
        hz = 60f,
        maxSubSteps = 3,
        maxDeltaSeconds = 1f / 15f,
    )

private fun stepPresetSmooth(state: PhysicsBoxState): StepConfig =
    stepPresetBase(state).copy(
        hz = 120f,
        maxSubSteps = 5,
        maxDeltaSeconds = 1f / 30f,
    )

private fun buildStateDemoBodies(): List<StateDemoBody> = listOf(
    StateDemoBody(
        key = "state-a",
        label = "A",
        widthDp = 86.dp,
        heightDp = 66.dp,
        shape = PhysicsShape.Box,
        startXpx = 110f,
        startYpx = 90f,
        rotationDegrees = 8f,
        color = Color(0xFF4E9CB5),
    ),
    StateDemoBody(
        key = "state-b",
        label = "B",
        widthDp = 72.dp,
        heightDp = 72.dp,
        shape = PhysicsShape.Circle(),
        startXpx = 220f,
        startYpx = 90f,
        rotationDegrees = 0f,
        color = Color(0xFFE07A5F),
    ),
    StateDemoBody(
        key = "state-c",
        label = "C",
        widthDp = 96.dp,
        heightDp = 64.dp,
        shape = PhysicsShape.Box,
        startXpx = 150f,
        startYpx = 170f,
        rotationDegrees = -12f,
        color = Color(0xFF7B9E59),
    ),
)

private fun formatFloat(value: Float): String = "%.2f".format(value)

private fun formatVector(vector: PhysicsVector2): String =
    "(${formatFloat(vector.x)}, ${formatFloat(vector.y)})"
