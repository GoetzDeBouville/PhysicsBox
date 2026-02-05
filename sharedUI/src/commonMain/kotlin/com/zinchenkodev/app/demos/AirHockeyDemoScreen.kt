package com.zinchenkodev.app.demos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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

private data class HockeyBody(
    val key: String,
    val label: String,
    val sizeDp: Dp,
    val color: Color,
    val config: PhysicsBodyConfig,
    val dragConfig: DragConfig,
    val isDraggable: Boolean,
)

@Composable
fun AirHockeyDemoScreen(
    modifier: Modifier = Modifier,
    resetSignal: Int = 0,
) {
    val state = rememberPhysicsBoxState()
    val bodies = remember { buildHockeyBodies() }

    LaunchedEffect(Unit) {
        state.setWorldGravity(PhysicsVector2(0f, 0f))
    }

    LaunchedEffect(resetSignal) {
        if (resetSignal <= 0) return@LaunchedEffect
        state.reset()
        state.setWorldGravity(PhysicsVector2(0f, 0f))
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Drag paddles to hit the puck. Fling for quick shots.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0E1B2B))
                .border(1.dp, Color(0x332A3A4A), RoundedCornerShape(18.dp))
                .padding(10.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 3f)
                drawLine(
                    color = Color(0xFF1F3A5A),
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height),
                    strokeWidth = stroke.width,
                )
                drawCircle(
                    color = Color(0xFF1F3A5A),
                    radius = size.minDimension * 0.2f,
                    center = center,
                    style = stroke,
                )
            }

            PhysicsBox(
                modifier = Modifier.fillMaxSize(),
                state = state,
                config = PhysicsBoxConfig(
                    boundaries = BoundariesConfig(
                        enabled = true,
                        restitution = 0.2f,
                        friction = 1.1f,
                        thicknessPx = 64f,
                    ),
                ),
            ) {
                bodies.forEach { body ->
                    Box(
                        modifier = Modifier
                            .size(body.sizeDp)
                            .physicsBody(
                                key = body.key,
                                config = body.config,
                                shape = PhysicsShape.Circle(),
                                isDraggable = body.isDraggable,
                                dragConfig = body.dragConfig,
                            )
                            .clip(CircleShape)
                            .background(body.color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = body.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

private fun buildHockeyBodies(): List<HockeyBody> = listOf(
    HockeyBody(
        key = "paddle-a",
        label = "A",
        sizeDp = 70.dp,
        color = Color(0xFF4DD0E1),
        isDraggable = true,
        dragConfig = DragConfig(
            maxForce = 2_400f,
            frequencyHz = 9f,
            dampingRatio = 1f,
            maxFlingVelocityPxPerSec = 7_000f,
        ),
        config = PhysicsBodyConfig(
            bodyType = BodyType.Dynamic,
            density = 3f,
            friction = 0.4f,
            restitution = 0.15f,
            linearDamping = 0.2f,
            fixedRotation = true,
            initialTransform = PhysicsTransform(
                vector2 = PhysicsVector2(140f, 220f),
            ),
        ),
    ),
    HockeyBody(
        key = "paddle-b",
        label = "B",
        sizeDp = 70.dp,
        color = Color(0xFFFFA726),
        isDraggable = true,
        dragConfig = DragConfig(
            maxForce = 2_400f,
            frequencyHz = 9f,
            dampingRatio = 1f,
            maxFlingVelocityPxPerSec = 7_000f,
        ),
        config = PhysicsBodyConfig(
            bodyType = BodyType.Dynamic,
            density = 3f,
            friction = 0.4f,
            restitution = 0.15f,
            linearDamping = 0.2f,
            fixedRotation = true,
            initialTransform = PhysicsTransform(
                vector2 = PhysicsVector2(420f, 220f),
            ),
        ),
    ),
    HockeyBody(
        key = "puck",
        label = "P",
        sizeDp = 44.dp,
        color = Color(0xFFECEFF1),
        isDraggable = true,
        dragConfig = DragConfig(
            maxForce = 1_800f,
            frequencyHz = 7f,
            dampingRatio = 0.9f,
            maxFlingVelocityPxPerSec = 8_000f,
        ),
        config = PhysicsBodyConfig(
            bodyType = BodyType.Dynamic,
            density = 1.4f,
            friction = 0.9f,
            restitution = 0.35f,
            linearDamping = 0.9f,
            fixedRotation = true,
            initialTransform = PhysicsTransform(
                vector2 = PhysicsVector2(280f, 220f),
            ),
        ),
    ),
)
