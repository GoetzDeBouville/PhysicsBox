package com.zinchenkodev.app.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.rememberPhysicsBoxState
import kotlin.random.Random

@Immutable
private data class BodyItem(
    val key: String,
    val sizeDp: Dp,
    val label: String,
    val isCircle: Boolean,
    val startXpx: Float,
    val startYpx: Float,
    val rotationDegrees: Float,
)

@Suppress("d")
@Composable
fun BasicStackingDemoScreen(
    modifier: Modifier = Modifier,
    resetSignal: Int = 0,
) {
    val state = rememberPhysicsBoxState()
    val items = remember { mutableStateListOf<BodyItem>() }
    var nextBodyIndex by remember { mutableIntStateOf(0) }
    var spawnVersion by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
            val initial = buildBodies(startIndex = 0, count = 12)
            items.addAll(initial)
            nextBodyIndex = initial.size
        }
    }

    LaunchedEffect(paused) {
        if (paused) state.pause() else state.resume()
    }

    LaunchedEffect(spawnVersion) {
        if (spawnVersion <= 0) return@LaunchedEffect
        val spawned = buildBodies(startIndex = nextBodyIndex, count = 2)
        items.addAll(spawned)
        nextBodyIndex += spawned.size
    }

    LaunchedEffect(resetSignal) {
        if (resetSignal <= 0) return@LaunchedEffect
        state.reset()
        paused = false
        items.clear()
        val initial = buildBodies(startIndex = 0, count = 12)
        items.addAll(initial)
        nextBodyIndex = initial.size
    }

    Column(
        modifier = modifier,
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
            Button(onClick = { paused = !paused }) {
                Text(if (paused) "Resume" else "Pause")
            }
            Text(
                text = if (paused) "Paused" else "Running",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        PhysicsBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE7EDF4))
                .padding(10.dp),
            state = state,
            config = PhysicsBoxConfig(
                boundaries = BoundariesConfig(
                    enabled = true,
                    restitution = 0.23f,
                    friction = 1f,
                    thicknessPx = 56f,
                ),
            ),
        ) {
            items.forEach { item ->
                val visualShape = if (item.isCircle) CircleShape else RoundedCornerShape(14.dp)
                val physicsShape = if (item.isCircle) PhysicsShape.Circle() else PhysicsShape.Box
                val bodyColor = demoPalette[item.key.hashCode().ushr(1) % demoPalette.size]
                val contentColor = if (bodyColor.luminance() > 0.5f) Color.Black else Color.White

                Box(
                    modifier = Modifier
                        .size(item.sizeDp)
                        .physicsBody(
                            key = item.key,
                            config = PhysicsBodyConfig(
                                bodyType = BodyType.Dynamic,
                                density = 1f,
                                friction = 0.32f,
                                restitution = 0.26f,
                                linearDamping = 0.05f,
                                angularDamping = 0.08f,
                                initialTransform = PhysicsTransform(
                                    vector2 = PhysicsVector2(item.startXpx, item.startYpx),
                                    rotationDegrees = item.rotationDegrees,
                                ),
                            ),
                            shape = physicsShape,
                            isDraggable = true,
                            dragConfig = DragConfig(
                                maxForce = 1_600f,
                                frequencyHz = 7f,
                                dampingRatio = 0.85f,
                                useJointStyleDrag = true,
                                maxFlingVelocityPxPerSec = 6_000f,
                            ),
                        )
                        .clip(visualShape)
                        .background(bodyColor)
                        .border(1.dp, Color(0x33000000), visualShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private fun buildBodies(startIndex: Int, count: Int): List<BodyItem> {
    val result = ArrayList<BodyItem>(count)
    for (offset in 0 until count) {
        val index = startIndex + offset
        val column = index % 4
        val row = index / 4
        val isCircle = index % 3 == 0
        val size = when (index % 3) {
            0 -> 54.dp
            1 -> 66.dp
            else -> 78.dp
        }

        result += BodyItem(
            key = "body-$index",
            sizeDp = size,
            label = if (isCircle) "C$index" else "#$index",
            isCircle = isCircle,
            startXpx = 86f + column * 96f,
            startYpx = 84f + row * 90f,
            rotationDegrees = Random(index).nextFloat() * 360f,
        )
    }
    return result
}

private val demoPalette = listOf(
    Color(0xFF2E7D32),
    Color(0xFF1565C0),
    Color(0xFFC62828),
    Color(0xFF6A1B9A),
    Color(0xFFEF6C00),
    Color(0xFF00838F),
)
