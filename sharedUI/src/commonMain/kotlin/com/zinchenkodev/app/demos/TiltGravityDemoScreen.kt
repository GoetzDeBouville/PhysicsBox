package com.zinchenkodev.app.demos

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.zinchenkodev.app.platform.GravityVector
import com.zinchenkodev.app.platform.Haptics
import com.zinchenkodev.app.platform.MotionProvider
import dev.zinchenko.physicsbox.BoundariesConfig
import dev.zinchenko.physicsbox.PhysicsBoxConfig
import dev.zinchenko.physicsbox.PhysicsDefaults
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.rememberPhysicsBoxState
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.random.Random

@Immutable
private data class TiltBodyItem(
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
fun TiltGravityDemoScreen(
    modifier: Modifier = Modifier,
    resetSignal: Int = 0,
    motionProvider: MotionProvider,
    haptics: Haptics,
) {
    if (motionProvider.isAvailable.not()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Motion sensors are not available on this device.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        return
    }

    val state = rememberPhysicsBoxState()
    val items = remember { mutableStateListOf<TiltBodyItem>() }
    val flashVersions = remember { mutableStateMapOf<String, Int>() }

    var sensitivity by remember { mutableFloatStateOf(1.2f) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var filteredGravity by remember {
        mutableStateOf(PhysicsVector2(PhysicsDefaults.Gravity.x, PhysicsDefaults.Gravity.y))
    }
    var lastAppliedGravity by remember {
        mutableStateOf(PhysicsVector2(PhysicsDefaults.Gravity.x, PhysicsDefaults.Gravity.y))
    }
    val lastHapticMs = remember { mutableLongStateOf(0L) }
    val tiltVector by motionProvider.rememberTiltGravityVector()

    fun triggerFlash(key: Any) {
        val k = key as? String ?: return
        flashVersions[k] = (flashVersions[k] ?: 0) + 1
    }

    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
            items.addAll(buildTiltBodies())
        }
        state.setWorldGravity(PhysicsDefaults.Gravity)
    }

    LaunchedEffect(resetSignal) {
        if (resetSignal <= 0) return@LaunchedEffect
        state.reset()
        items.clear()
        items.addAll(buildTiltBodies())
        state.setWorldGravity(PhysicsDefaults.Gravity)
    }

    LaunchedEffect(tiltVector, sensitivity) {
        val target = tiltToWorldGravity(
            tilt = tiltVector,
            baseMagnitude = sqrt(
                PhysicsDefaults.Gravity.x * PhysicsDefaults.Gravity.x +
                        PhysicsDefaults.Gravity.y * PhysicsDefaults.Gravity.y,
            ),
            sensitivity = sensitivity,
        )
        val next = PhysicsVector2(
            x = -(filteredGravity.x + GRAVITY_FILTER_ALPHA * (target.x - filteredGravity.x)),
            y = -(filteredGravity.y + GRAVITY_FILTER_ALPHA * (target.y - filteredGravity.y)),
        )
        filteredGravity = next

        val deltaX = next.x - lastAppliedGravity.x
        val deltaY = next.y - lastAppliedGravity.y
        if (sqrt(deltaX * deltaX + deltaY * deltaY) > GRAVITY_EPSILON) {
            state.setWorldGravity(next)
            lastAppliedGravity = next
        }
    }


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Tilt the device to steer gravity. Drag bodies to interact.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Sensitivity: ${"%.2f".format(sensitivity)}x",
                style = MaterialTheme.typography.labelLarge,
            )
            Slider(
                value = sensitivity,
                onValueChange = { sensitivity = it },
                valueRange = 0.15f..15f,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { hapticsEnabled = it },
                )
                Text(
                    text = "Haptics on collisions",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        PhysicsBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFEFF3F7))
                .padding(10.dp),
            state = state,
            config = PhysicsBoxConfig(
                boundaries = BoundariesConfig(
                    enabled = true,
                    restitution = 0.25f,
                    friction = 0.8f,
                    thicknessPx = 56f,
                ),
            ),
        ) {
            items.forEach { item ->
                val visualShape = if (item.isCircle) CircleShape else RoundedCornerShape(12.dp)
                val physicsShape = if (item.isCircle) PhysicsShape.Circle() else PhysicsShape.Box
                val bodyColor = tiltPalette[item.key.hashCode().ushr(1) % tiltPalette.size]

                val flashVersion = flashVersions[item.key] ?: 0
                var isFlashing by remember { mutableStateOf(false) }

                LaunchedEffect(flashVersion) {
                    if (flashVersion == 0) return@LaunchedEffect
                    isFlashing = true
                    delay(COLLISION_FLASH_MS)
                    isFlashing = false
                }

                val animatedBg by animateColorAsState(
                    targetValue = if (isFlashing) Color.White else bodyColor,
                    label = "collisionFlash-${item.key}",
                )

                val contentColor = if (bodyColor.luminance() > 0.5f) Color.Black else Color.White

                Text(
                    text = "Acceleration vector (x; y) = \n${filteredGravity.x}; \n${filteredGravity.y}",
                    color = Color.Black
                )
                Column(
                    modifier = Modifier
                        .size(item.sizeDp)
                        .physicsBody(
                            key = item.key,
                            config = PhysicsBodyConfig(
                                bodyType = BodyType.Dynamic,
                                density = 1f,
                                friction = 0.3f,
                                restitution = 0.35f,
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
                                maxForce = 1_800f,
                                frequencyHz = 7f,
                                dampingRatio = 0.85f,
                                maxFlingVelocityPxPerSec = 6_500f,
                            ),
                            onCollision = { event ->
                                triggerFlash(event.selfKey)
                                triggerFlash(event.otherKey)

                                if (hapticsEnabled.not()) return@physicsBody
                                val nowMs = System.currentTimeMillis()
                                if (nowMs - lastHapticMs.longValue < HAPTIC_COOLDOWN_MS) return@physicsBody
                                lastHapticMs.longValue = nowMs
                                val intensity = if (event.impulse > 0f) {
                                    (event.impulse / 10f).coerceIn(0.1f, 1f)
                                } else {
                                    0.35f
                                }
                                haptics.collisionTick(intensity)
                            },
                        )
                        .clip(visualShape)
                        .background(animatedBg)
                        .border(1.dp, Color(0x33000000), visualShape),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
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

private fun tiltToWorldGravity(
    tilt: GravityVector,
    baseMagnitude: Float,
    sensitivity: Float,
): PhysicsVector2 {
    val magnitude = sqrt(tilt.x * tilt.x + tilt.y * tilt.y + tilt.z * tilt.z)
    if (magnitude <= 0f) return PhysicsVector2(0f, baseMagnitude * sensitivity)
    val normalizedX = tilt.x / magnitude
    val normalizedY = -tilt.y / magnitude
    return PhysicsVector2(
        x = normalizedX * baseMagnitude * sensitivity,
        y = normalizedY * baseMagnitude * sensitivity,
    )
}

private fun buildTiltBodies(): List<TiltBodyItem> {
    val result = ArrayList<TiltBodyItem>(3)
    for (index in 0 until 3) {
        val column = index % 2
        val row = index / 2
        val isCircle = index % 2 == 0
        val size = if (isCircle) 58.dp else 72.dp

        result += TiltBodyItem(
            key = "tilt-$index",
            sizeDp = size,
            label = if (isCircle) "T$index" else "#$index",
            isCircle = isCircle,
            startXpx = 84f + column * 94f,
            startYpx = 90f + row * 88f,
            rotationDegrees = Random(index + 5).nextFloat() * 360f,
        )
    }
    return result
}

private const val GRAVITY_FILTER_ALPHA: Float = 0.2f
private const val GRAVITY_EPSILON: Float = 0.05f
private const val HAPTIC_COOLDOWN_MS: Long = 80L
private const val COLLISION_FLASH_MS: Long = 120L

private val tiltPalette = listOf(
    Color(0xFF1E88E5),
    Color(0xFF43A047),
    Color(0xFFF4511E),
    Color(0xFF7E57C2),
    Color(0xFF00897B),
    Color(0xFFF9A825),
)
