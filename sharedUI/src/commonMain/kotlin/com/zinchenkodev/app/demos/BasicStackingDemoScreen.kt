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
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.graphics.Shape
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Immutable
private data class BodyItem(
    val key: String,
    val widthDp: Dp,
    val heightDp: Dp,
    val label: String,
    val physicsShape: PhysicsShape,
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
                val bodyColor = demoPalette[item.key.hashCode().ushr(1) % demoPalette.size]
                val contentColor = if (bodyColor.luminance() > 0.5f) Color.Black else Color.White

                val clipShape: Shape = when (val s = item.physicsShape) {
                    PhysicsShape.Box -> RoundedCornerShape(14.dp)
                    is PhysicsShape.Circle -> CircleShape
                    is PhysicsShape.Polygon -> polygonComposeShape(s)
                }

                // Для круга делаем UI-квадрат, чтобы CircleShape выглядел корректно
                val w = item.widthDp
                val h = item.heightDp
                val circleSize = if (w < h) w else h

                Box(
                    modifier = Modifier
                        .size(
                            width = if (item.physicsShape is PhysicsShape.Circle) circleSize else w,
                            height = if (item.physicsShape is PhysicsShape.Circle) circleSize else h,
                        )
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
                            shape = item.physicsShape, // <-- главное изменение
                            isDraggable = true,
                            dragConfig = DragConfig(
                                maxForce = 1_600f,
                                frequencyHz = 7f,
                                dampingRatio = 0.85f,
                                useJointStyleDrag = true,
                                maxFlingVelocityPxPerSec = 6_000f,
                            ),
                        )
                        .clip(clipShape)
                        .background(bodyColor)
                        .border(1.dp, Color(0x33000000), clipShape),
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

    val presets: List<Pair<String, Pair<Pair<Dp, Dp>, PhysicsShape>>> = listOf(
        "Square" to ((72.dp to 72.dp) to PhysicsShape.Box),
        "Rect" to ((104.dp to 62.dp) to PhysicsShape.Box),
        "Trap" to ((110.dp to 70.dp) to trapezoidNormalized()),
        "Circle" to ((72.dp to 72.dp) to PhysicsShape.Circle()),
        "P5" to ((86.dp to 86.dp) to regularPolygonNormalized(5)),
        "P6" to ((90.dp to 90.dp) to regularPolygonNormalized(6)),
        "P7" to ((94.dp to 94.dp) to regularPolygonNormalized(7)),
        "P8" to ((98.dp to 98.dp) to regularPolygonNormalized(8)),
    )

    for (offset in 0 until count) {
        val index = startIndex + offset
        val column = index % 4
        val row = index / 4

        val presetIndex = index % presets.size
        val (label, sizeAndShape) = presets[presetIndex]
        val (size, shape) = sizeAndShape
        val (w, h) = size

        result += BodyItem(
            key = "body-$index",
            widthDp = w,
            heightDp = h,
            label = "$label$index",
            physicsShape = shape,
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

private fun trapezoidNormalized(): PhysicsShape.Polygon =
    PhysicsShape.Polygon(
        vertices = listOf(
            PhysicsVector2(-0.50f, 0.50f),
            PhysicsVector2(0.50f, 0.50f),
            PhysicsVector2(0.28f, -0.50f),
            PhysicsVector2(-0.28f, -0.50f),
        ),
        space = PhysicsShape.Polygon.VertexSpace.Normalized,
    )

private fun regularPolygonNormalized(
    sides: Int,
    radius: Float = 0.48f,
    rotationDegrees: Float = -90f,
): PhysicsShape.Polygon {
    require(sides in 3..8) { "sides must be in 3..8 for jbox2d PolygonShape." }

    val rot = rotationDegrees * (PI.toFloat() / 180f)
    val step = (2f * PI.toFloat()) / sides

    val verts = List(sides) { i ->
        val a = rot + step * i
        PhysicsVector2(
            x = cos(a) * radius,
            y = sin(a) * radius,
        )
    }

    return PhysicsShape.Polygon(
        vertices = verts,
        space = PhysicsShape.Polygon.VertexSpace.Normalized,
    )
}

/**
 * Visual clip shape matching [PhysicsShape.Polygon].
 * Supports both Normalized and Px vertex spaces.
 */
private fun polygonComposeShape(polygon: PhysicsShape.Polygon): Shape =
    GenericShape { size, _ ->
        val verts = polygon.vertices
        if (verts.isEmpty()) return@GenericShape

        fun map(v: PhysicsVector2): Pair<Float, Float> {
            val cx = size.width * 0.5f
            val cy = size.height * 0.5f
            return when (polygon.space) {
                PhysicsShape.Polygon.VertexSpace.Normalized -> {
                    val x = cx + v.x * size.width
                    val y = cy + v.y * size.height
                    x to y
                }

                PhysicsShape.Polygon.VertexSpace.Px -> {
                    val x = cx + v.x
                    val y = cy + v.y
                    x to y
                }
            }
        }

        val (x0, y0) = map(verts[0])
        moveTo(x0, y0)
        for (i in 1 until verts.size) {
            val (x, y) = map(verts[i])
            lineTo(x, y)
        }
        close()
    }
