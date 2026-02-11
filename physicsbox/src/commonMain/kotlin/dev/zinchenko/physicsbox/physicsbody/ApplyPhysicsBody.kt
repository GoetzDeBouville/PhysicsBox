package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Velocity
import dev.zinchenko.physicsbox.LocalPhysicsBoxCoordinates
import dev.zinchenko.physicsbox.LocalPhysicsBoxState
import dev.zinchenko.physicsbox.PhysicsCommand
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent
import dev.zinchenko.physicsbox.events.DragPhase

@Suppress("D")
internal fun applyPhysicsBody(
    modifier: Modifier,
    key: Any,
    config: PhysicsBodyConfig = PhysicsBodyConfig(),
    shape: PhysicsShape = PhysicsShape.Box,
    collisionFilter: CollisionFilter = CollisionFilter.Default,
    isDraggable: Boolean = true,
    dragConfig: DragConfig = DragConfig(),
    onCollision: ((CollisionEvent) -> Unit)? = null,
    onSleepChanged: ((Boolean) -> Unit)? = null,
    onDragStart: ((DragEvent) -> Unit)? = null,
    onDragEnd: ((DragEvent) -> Unit)? = null,
): Modifier {
    require(key != Unit) { "physicsBody key must be a stable identity, Unit is not allowed." }
    return modifier.composed {
        val state = LocalPhysicsBoxState.current
        val containerCoordinatesState = rememberUpdatedState(LocalPhysicsBoxCoordinates.current)
        var bodyCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

        val onCollisionState = rememberUpdatedState(onCollision)
        val onSleepChangedState = rememberUpdatedState(onSleepChanged)
        val onDragStartState = rememberUpdatedState(onDragStart)
        val onDragEndState = rememberUpdatedState(onDragEnd)

        val callbacks = remember(key) {
            PhysicsBodyCallbacks(
                onCollision = { event -> onCollisionState.value?.invoke(event) },
                onSleepChanged = { isSleeping -> onSleepChangedState.value?.invoke(isSleeping) },
                onDragStart = { event -> onDragStartState.value?.invoke(event) },
                onDragEnd = { event -> onDragEndState.value?.invoke(event) },
            )
        }

        val registration =
            remember(key, config, shape, collisionFilter, isDraggable, dragConfig, callbacks) {
                PhysicsBodyRegistration(
                    key = key,
                    config = config,
                    shape = shape,
                    filter = collisionFilter,
                    isDraggable = isDraggable,
                    dragConfig = dragConfig,
                    callbacks = callbacks,
                )
            }

        DisposableEffect(state, key, callbacks) {
            state?.registerBodyCallbacks(key, callbacks)
            onDispose { state?.unregisterBodyCallbacks(key) }
        }

        val dragModifier = if (isDraggable && state != null) {
            Modifier.pointerInput(state, key, isDraggable, dragConfig) {
                awaitEachGesture {
                    if (state.isPaused) return@awaitEachGesture
                    val bodyCoords = bodyCoordinates ?: return@awaitEachGesture
                    val containerCoords = containerCoordinatesState.value ?: return@awaitEachGesture
                    if (bodyCoords.isAttached.not() || containerCoords.isAttached.not()) return@awaitEachGesture

                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    val downContainer = toContainerPosition(
                        bodyCoordinates = bodyCoords,
                        containerCoordinates = containerCoords,
                        pointInBody = down.position,
                    ) ?: return@awaitEachGesture

                    val velocityTracker = VelocityTracker()
                    velocityTracker.addPosition(down.uptimeMillis, downContainer)

                    val targetPx = downContainer.toPhysicsVector()
                    state.enqueueCommand(
                        PhysicsCommand.BeginDrag(
                            key = key,
                            grabPointPx = down.position.toPhysicsVector(),
                            pointerId = pointerId.value,
                            targetPx = targetPx,
                            dragConfig = dragConfig,
                        ),
                    )
                    state.dispatchDragToBody(
                        DragEvent(
                            key = key,
                            phase = DragPhase.Start,
                            pointerXPx = downContainer.x,
                            pointerYPx = downContainer.y,
                            targetXPx = targetPx.x,
                            targetYPx = targetPx.y,
                            uptimeMillis = down.uptimeMillis,
                        ),
                    )
                    down.consume()

                    var endedByUp = false
                    var lastPosition = downContainer
                    var lastUptimeMillis = down.uptimeMillis

                    while (true) {
                        val event = awaitPointerEvent()
                        val pointerChange =
                            event.changes.firstOrNull { it.id == pointerId } ?: break

                        val bodyCoordsNow = bodyCoordinates ?: break
                        val containerCoordsNow = containerCoordinatesState.value ?: break
                        if (bodyCoordsNow.isAttached.not() || containerCoordsNow.isAttached.not()) break

                        val containerPosition = toContainerPosition(
                            bodyCoordinates = bodyCoordsNow,
                            containerCoordinates = containerCoordsNow,
                            pointInBody = pointerChange.position,
                        ) ?: break

                        velocityTracker.addPosition(pointerChange.uptimeMillis, containerPosition)
                        lastPosition = containerPosition
                        lastUptimeMillis = pointerChange.uptimeMillis

                        if (pointerChange.changedToUpIgnoreConsumed()) {
                            val rawVelocity = velocityTracker.calculateVelocity()
                            val clampedVelocity = clampVelocity(
                                velocity = rawVelocity,
                                maxAbs = dragConfig.maxFlingVelocityPxPerSec,
                            )

                            state.enqueueCommand(
                                PhysicsCommand.EndDrag(
                                    key = key,
                                    velocityPxPerSec = PhysicsVector2(
                                        x = clampedVelocity.x,
                                        y = clampedVelocity.y,
                                    ),
                                ),
                            )
                            state.dispatchDragToBody(
                                DragEvent(
                                    key = key,
                                    phase = DragPhase.End,
                                    pointerXPx = containerPosition.x,
                                    pointerYPx = containerPosition.y,
                                    targetXPx = containerPosition.x,
                                    targetYPx = containerPosition.y,
                                    velocityXPxPerSec = clampedVelocity.x,
                                    velocityYPxPerSec = clampedVelocity.y,
                                    uptimeMillis = pointerChange.uptimeMillis,
                                ),
                            )
                            pointerChange.consume()
                            endedByUp = true
                            break
                        }

                        val target = containerPosition.toPhysicsVector()
                        state.enqueueCommand(
                            PhysicsCommand.UpdateDrag(
                                key = key,
                                targetPx = target,
                            ),
                        )
                        state.dispatchDragToBody(
                            DragEvent(
                                key = key,
                                phase = DragPhase.Move,
                                pointerXPx = containerPosition.x,
                                pointerYPx = containerPosition.y,
                                targetXPx = target.x,
                                targetYPx = target.y,
                                uptimeMillis = pointerChange.uptimeMillis,
                            ),
                        )
                        if (pointerChange.positionChanged()) {
                            pointerChange.consume()
                        }
                    }

                    if (endedByUp.not()) {
                        state.enqueueCommand(PhysicsCommand.CancelDrag(key))
                        state.dispatchDragToBody(
                            DragEvent(
                                key = key,
                                phase = DragPhase.Cancel,
                                pointerXPx = lastPosition.x,
                                pointerYPx = lastPosition.y,
                                targetXPx = lastPosition.x,
                                targetYPx = lastPosition.y,
                                uptimeMillis = lastUptimeMillis,
                            ),
                        )
                    }
                }
            }
        } else {
            Modifier
        }

        this
            .onGloballyPositioned { coordinates -> bodyCoordinates = coordinates }
            .then(PhysicsBodyModifierElement(registration))
            .then(dragModifier)
    }
}

private fun Offset.toPhysicsVector(): PhysicsVector2 = PhysicsVector2(x = x, y = y)

private fun clampVelocity(
    velocity: Velocity,
    maxAbs: Float,
): Velocity {
    val clampedX = velocity.x.coerceIn(-maxAbs, maxAbs)
    val clampedY = velocity.y.coerceIn(-maxAbs, maxAbs)
    return Velocity(clampedX, clampedY)
}

private fun toContainerPosition(
    bodyCoordinates: LayoutCoordinates,
    containerCoordinates: LayoutCoordinates,
    pointInBody: Offset,
): Offset? {
    if (bodyCoordinates.isAttached.not() || containerCoordinates.isAttached.not()) return null
    return containerCoordinates.localPositionOf(bodyCoordinates, pointInBody)
}
