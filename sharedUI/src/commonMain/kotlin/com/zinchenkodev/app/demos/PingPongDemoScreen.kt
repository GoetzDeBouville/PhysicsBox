package com.zinchenkodev.app.demos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zinchenkodev.app.platform.Haptics
import dev.zinchenko.physicsbox.BoundariesConfig
import dev.zinchenko.physicsbox.PhysicsBoxConfig
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.rememberPhysicsBoxState
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

@Suppress("d")
@Composable
fun PingPongDemoScreen(
    modifier: Modifier = Modifier,
    resetSignal: Int = 0,
    haptics: Haptics,
) {
    val state = rememberPhysicsBoxState()
    val density = LocalDensity.current

    var scorePlayer by remember { mutableIntStateOf(0) }
    var scoreCpu by remember { mutableIntStateOf(0) }
    var roundId by remember { mutableIntStateOf(0) }
    var goalLocked by remember { mutableStateOf(false) }

    var playerTargetY by remember { mutableFloatStateOf(0f) }
    var cpuTargetY by remember { mutableFloatStateOf(0f) }

    var playerCenterY by remember { mutableFloatStateOf(0f) }
    var cpuCenterY by remember { mutableFloatStateOf(0f) }
    var ballCenterX by remember { mutableFloatStateOf(0f) }
    var ballCenterY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF050B0F))
            .border(1.dp, Color(0xFF1E2B2A), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "YOU",
                color = RETRO_GREEN,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$scorePlayer : $scoreCpu",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "CPU",
                color = RETRO_GREEN,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF050B0F))
                .border(1.dp, Color(0xFF1E2B2A), RoundedCornerShape(14.dp)),
        ) {
            val arenaWidthPx = with(density) { maxWidth.toPx() }
            val arenaHeightPx = with(density) { maxHeight.toPx() }
            val paddleWidthPx = with(density) { PADDLE_WIDTH_DP.toPx() }
            val paddleHeightPx = with(density) { PADDLE_HEIGHT_DP.toPx() }
            val wallThicknessPx = with(density) { WALL_THICKNESS_DP.toPx() }
            val paddleInsetPx = with(density) { PADDLE_INSET_DP.toPx() }

            val paddleMinY = wallThicknessPx + paddleHeightPx * 0.5f
            val paddleMaxY = arenaHeightPx - wallThicknessPx - paddleHeightPx * 0.5f

            LaunchedEffect(arenaWidthPx, arenaHeightPx) {
                if (arenaWidthPx <= 0f || arenaHeightPx <= 0f) return@LaunchedEffect
                state.setWorldGravity(PhysicsVector2(0f, 0f))
                playerTargetY = arenaHeightPx * 0.5f
                cpuTargetY = arenaHeightPx * 0.5f
            }

            LaunchedEffect(resetSignal, arenaWidthPx, arenaHeightPx) {
                if (resetSignal <= 0) return@LaunchedEffect
                if (arenaWidthPx <= 0f || arenaHeightPx <= 0f) return@LaunchedEffect
                goalLocked = true
                scorePlayer = 0
                scoreCpu = 0
                playerTargetY = arenaHeightPx * 0.5f
                cpuTargetY = arenaHeightPx * 0.5f
                state.reset()
                state.setWorldGravity(PhysicsVector2(0f, 0f))
                roundId += 1
            }

            LaunchedEffect(roundId, arenaWidthPx, arenaHeightPx) {
                if (arenaWidthPx <= 0f || arenaHeightPx <= 0f) return@LaunchedEffect
                state.setWorldGravity(PhysicsVector2(0f, 0f))
                goalLocked = true
                delay(24)
                val direction = if (Random.nextBoolean()) 1f else -1f
                val vy = (Random.nextFloat() * 2f - 1f) * BALL_MAX_VY
                state.enqueueVelocity(
                    key = BALL_KEY,
                    velocityX = BALL_SPEED * direction,
                    velocityY = vy,
                )
                ballCenterX = arenaWidthPx * 0.5f
                ballCenterY = arenaHeightPx * 0.5f
                goalLocked = false
            }

            DisposableEffect(state, arenaWidthPx, arenaHeightPx, paddleMinY, paddleMaxY) {
                state.setOnStepListener { _ ->
                    val playerVy = clampVelocity(
                        (playerTargetY - playerCenterY) * PLAYER_FOLLOW,
                        PLAYER_SPEED,
                    )
                    val desiredBallY = if (ballCenterY > 0f) ballCenterY else arenaHeightPx * 0.5f
                    val cpuAim = cpuTargetY + (desiredBallY - cpuTargetY) * CPU_REACTION_ALPHA
                    cpuTargetY = cpuAim.coerceIn(paddleMinY, paddleMaxY)
                    val cpuVy = clampVelocity(
                        (cpuTargetY - cpuCenterY) * CPU_FOLLOW,
                        CPU_SPEED,
                    )

                    state.enqueueVelocity(PLAYER_KEY, 0f, playerVy)
                    state.enqueueVelocity(CPU_KEY, 0f, cpuVy)

                    if (!goalLocked && ballCenterX < -GOAL_MARGIN_PX) {
                        goalLocked = true
                        scoreCpu += 1
                        roundId += 1
                        state.reset()
                    } else if (!goalLocked && ballCenterX > arenaWidthPx + GOAL_MARGIN_PX) {
                        goalLocked = true
                        scorePlayer += 1
                        roundId += 1
                        state.reset()
                    }
                }

                onDispose { state.setOnStepListener(null) }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(arenaHeightPx, paddleMinY, paddleMaxY) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            playerTargetY = down.position.y.coerceIn(paddleMinY, paddleMaxY)

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                playerTargetY = change.position.y.coerceIn(paddleMinY, paddleMaxY)
                                if (change.changedToUpIgnoreConsumed()) break
                            }
                        }
                    },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dashHeight = with(density) { DASH_HEIGHT_DP.toPx() }
                    val gap = with(density) { DASH_GAP_DP.toPx() }
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = RETRO_GREEN.copy(alpha = 0.6f),
                            topLeft = Offset(size.width * 0.5f - 1f, y),
                            size = Size(2f, dashHeight),
                        )
                        y += dashHeight + gap
                    }

                    val lineSpacing = with(density) { SCANLINE_SPACING_DP.toPx() }
                    var scanY = 0f
                    while (scanY < size.height) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.05f),
                            topLeft = Offset(0f, scanY),
                            size = Size(size.width, 1f),
                        )
                        scanY += lineSpacing
                    }

                    drawRect(
                        color = RETRO_GREEN.copy(alpha = 0.2f),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, wallThicknessPx),
                    )
                    drawRect(
                        color = RETRO_GREEN.copy(alpha = 0.2f),
                        topLeft = Offset(0f, size.height - wallThicknessPx),
                        size = Size(size.width, wallThicknessPx),
                    )
                }

                PhysicsBox(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    config = PhysicsBoxConfig(
                        boundaries = BoundariesConfig(enabled = false),
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(WALL_THICKNESS_DP)
                            .physicsBody(
                                key = TOP_WALL_KEY,
                                config = PhysicsBodyConfig(
                                    bodyType = BodyType.Static,
                                    friction = 0f,
                                    restitution = 1.2f,
                                    initialTransform = PhysicsTransform(
                                        vector2 = PhysicsVector2(
                                            arenaWidthPx * 0.5f,
                                            wallThicknessPx * 0.5f,
                                        ),
                                    ),
                                ),
                                shape = PhysicsShape.Box,
                                isDraggable = false,
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(WALL_THICKNESS_DP)
                            .physicsBody(
                                key = BOTTOM_WALL_KEY,
                                config = PhysicsBodyConfig(
                                    bodyType = BodyType.Static,
                                    friction = 0f,
                                    restitution = 1f,
                                    initialTransform = PhysicsTransform(
                                        vector2 = PhysicsVector2(
                                            arenaWidthPx * 0.5f,
                                            arenaHeightPx - wallThicknessPx * 0.5f,
                                        ),
                                    ),
                                ),
                                shape = PhysicsShape.Box,
                                isDraggable = false,
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .width(PADDLE_WIDTH_DP)
                            .height(PADDLE_HEIGHT_DP)
                            .physicsBody(
                                key = PLAYER_KEY,
                                config = PhysicsBodyConfig(
                                    bodyType = BodyType.Kinematic,
                                    density = 2f,
                                    friction = 0f,
                                    restitution = 1f,
                                    fixedRotation = true,
                                    gravityScale = 0f,
                                    initialTransform = PhysicsTransform(
                                        vector2 = PhysicsVector2(
                                            paddleInsetPx + paddleWidthPx * 0.5f,
                                            arenaHeightPx * 0.5f,
                                        ),
                                    ),
                                ),
                                shape = PhysicsShape.Box,
                                isDraggable = false,
                                onCollision = { event ->
                                    val intensity = if (event.impulse > 0f) {
                                        (event.impulse / 10f).coerceIn(0.1f, 1f)
                                    } else {
                                        0.35f
                                    }
                                    haptics.collisionTick(intensity)
                                }
                            )
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInParent()
                                playerCenterY = pos.y + coords.size.height * 0.5f
                            }
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White),
                    )

                    Box(
                        modifier = Modifier
                            .width(PADDLE_WIDTH_DP)
                            .height(PADDLE_HEIGHT_DP)
                            .physicsBody(
                                key = CPU_KEY,
                                config = PhysicsBodyConfig(
                                    bodyType = BodyType.Kinematic,
                                    density = 2f,
                                    friction = 0f,
                                    restitution = 1f,
                                    fixedRotation = true,
                                    gravityScale = 0f,
                                    initialTransform = PhysicsTransform(
                                        vector2 = PhysicsVector2(
                                            arenaWidthPx - paddleInsetPx - paddleWidthPx * 0.5f,
                                            arenaHeightPx * 0.5f,
                                        ),
                                    ),
                                ),
                                shape = PhysicsShape.Box,
                                isDraggable = false,
                            )
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInParent()
                                cpuCenterY = pos.y + coords.size.height * 0.5f
                            }
                            .clip(RoundedCornerShape(6.dp))
                            .background(RETRO_GREEN),
                    )

                    Box(
                        modifier = Modifier
                            .size(BALL_SIZE_DP)
                            .physicsBody(
                                key = BALL_KEY,
                                config = PhysicsBodyConfig(
                                    bodyType = BodyType.Dynamic,
                                    density = 0.8f,
                                    friction = 0f,
                                    restitution = 0.98f,
                                    linearDamping = 0f,
                                    angularDamping = 0f,
                                    fixedRotation = true,
                                    isBullet = true,
                                    gravityScale = 0f,
                                    initialTransform = PhysicsTransform(
                                        vector2 = PhysicsVector2(
                                            arenaWidthPx * 0.5f,
                                            arenaHeightPx * 0.5f,
                                        ),
                                    ),
                                ),
                                shape = PhysicsShape.Circle(),
                                isDraggable = false,
                            )
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInParent()
                                ballCenterX = pos.x + coords.size.width * 0.5f
                                ballCenterY = pos.y + coords.size.height * 0.5f
                            }
                            .clip(CircleShape)
                            .background(Color.White),
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Drag to move",
                        color = RETRO_GREEN.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

private fun clampVelocity(raw: Float, maxAbs: Float): Float {
    if (abs(raw) <= maxAbs) return raw
    return sign(raw) * maxAbs
}

private const val BALL_KEY = "pong-ball"
private const val PLAYER_KEY = "pong-paddle-player"
private const val CPU_KEY = "pong-paddle-cpu"
private const val TOP_WALL_KEY = "pong-wall-top"
private const val BOTTOM_WALL_KEY = "pong-wall-bottom"
private const val PLAYER_SPEED = 1_450f
private const val CPU_SPEED = 1_200f
private const val PLAYER_FOLLOW = 7.5f
private const val CPU_FOLLOW = 5f
private const val CPU_REACTION_ALPHA = 0.18f
private const val BALL_SPEED = 1_250f
private const val BALL_MAX_VY = 520f
private const val GOAL_MARGIN_PX = 24f
private val RETRO_GREEN = Color(0xFF77FFAA)
private val PADDLE_WIDTH_DP = 14.dp
private val PADDLE_HEIGHT_DP = 76.dp
private val BALL_SIZE_DP = 12.dp
private val WALL_THICKNESS_DP = 12.dp
private val PADDLE_INSET_DP = 24.dp
private val DASH_HEIGHT_DP = 10.dp
private val DASH_GAP_DP = 10.dp
private val SCANLINE_SPACING_DP = 5.dp
