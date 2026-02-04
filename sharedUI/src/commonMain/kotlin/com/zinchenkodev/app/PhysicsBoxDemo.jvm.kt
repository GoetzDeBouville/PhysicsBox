package com.zinchenkodev.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import dev.zinchenko.physicsbox.PxPerMeter
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.rememberPhysicsBoxState

internal data class BodyItem(
    val key: String,
    val sizeDp: Dp,
    val label: String,
    val isCircle: Boolean,
    val startXpx: Float,
    val startYpx: Float,
)

@Composable
fun PhysicsBoxDemo(modifier: Modifier) {
    val controls = LocalPhysicsBoxDemoControls.current
    val state = rememberPhysicsBoxState()
    val items = remember { mutableStateListOf<BodyItem>() }
    var nextBodyIndex by remember { mutableIntStateOf(0) }

    val config = remember {
        PhysicsBoxConfig(
            worldScale = PxPerMeter(90f),
            boundaries = BoundariesConfig(
                enabled = true,
                restitution = 0.45f,
                friction = 0.25f,
                thicknessPx = 56f,
            ),
        )
    }

    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
            val initial = buildBodies(startIndex = 0, count = 12)
            items.addAll(initial)
            nextBodyIndex = initial.size
        }
    }

    LaunchedEffect(controls.paused) {
        if (controls.paused) state.pause() else state.resume()
    }

    LaunchedEffect(controls.spawnVersion) {
        if (controls.spawnVersion <= 0) return@LaunchedEffect
        val spawned = buildBodies(startIndex = nextBodyIndex, count = 2)
        items.addAll(spawned)
        nextBodyIndex += spawned.size
    }

    LaunchedEffect(controls.resetVersion) {
        if (controls.resetVersion <= 0) return@LaunchedEffect
        state.reset()
        items.clear()
        val initial = buildBodies(startIndex = 0, count = 12)
        items.addAll(initial)
        nextBodyIndex = initial.size
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE7EDF4)),
    ) {
        PhysicsBox(
            modifier = Modifier.fillMaxSize(),
            state = state,
            config = config,
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
                                    positionPx = PhysicsVector2(item.startXpx, item.startYpx),
                                    rotationDegrees = 0f,
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
