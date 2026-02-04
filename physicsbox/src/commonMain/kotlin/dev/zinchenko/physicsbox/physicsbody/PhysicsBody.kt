package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.ui.Modifier
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent

/**
 * Registers this composable as a physics body in the nearest [dev.zinchenko.physicsbox.PhysicsBox] scope.
 *
 * @param key stable identity linking composable and body across recompositions.
 * Prefer simple immutable keys (`String`, `Long`, enum) that do not change while the item exists.
 * @param config body material and motion parameters.
 * @param shape body shape descriptor.
 * @param filter collision filtering rules.
 * @param isDraggable enables pointer-driven drag behavior.
 * @param dragConfig drag behavior tuning.
 * @param onCollision optional callback for contact events involving this body.
 * @param onSleepChanged optional callback when sleeping state changes.
 * @param onDragStart optional callback when drag interaction starts.
 * @param onDragEnd optional callback when drag interaction ends.
 */
fun Modifier.physicsBody(
    key: Any,
    config: PhysicsBodyConfig = PhysicsBodyConfig(),
    shape: PhysicsShape = PhysicsShape.Box,
    filter: CollisionFilter = CollisionFilter.Default,
    isDraggable: Boolean = true,
    dragConfig: DragConfig = DragConfig(),
    onCollision: ((CollisionEvent) -> Unit)? = null,
    onSleepChanged: ((Boolean) -> Unit)? = null,
    onDragStart: ((DragEvent) -> Unit)? = null,
    onDragEnd: ((DragEvent) -> Unit)? = null,
): Modifier = applyPhysicsBody(
    modifier = this,
    key = key,
    config = config,
    shape = shape,
    filter = filter,
    isDraggable = isDraggable,
    dragConfig = dragConfig,
    onCollision = onCollision,
    onSleepChanged = onSleepChanged,
    onDragStart = onDragStart,
    onDragEnd = onDragEnd,
)
