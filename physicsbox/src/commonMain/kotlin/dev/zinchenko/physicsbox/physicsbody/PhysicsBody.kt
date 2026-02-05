package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.ui.Modifier
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent

/**
 * Registers this composable as a physics body in the nearest
 * [dev.zinchenko.physicsbox.layout.PhysicsBox] scope.
 *
 * The modifier binds a measured Compose node to a simulation body and enables the engine to
 * drive its visual transform (translation/rotation) during placement.
 *
 * ### Keys
 * [key] must be stable across recompositions and unique within a single `PhysicsBox` subtree.
 * Prefer simple immutable keys (`String`, `Long`, enums). Reusing a key for different nodes or
 * using non-stable keys leads to undefined behavior (including misrouted events).
 *
 * ### Dragging
 * When [isDraggable] is enabled, pointer input may control the body via a drag controller.
 * The behavior is tuned by [dragConfig]. Drag callbacks use [DragEvent].
 *
 * ### Collisions
 * [onCollision] receives contact information involving this body (see [CollisionEvent]).
 * Use [filter] to configure collision inclusion/exclusion.
 *
 * @param key Stable identity linking composable and body across recompositions.
 * @param config Body material/motion parameters.
 * @param shape Shape descriptor used to create the collision geometry.
 * @param filter Collision filtering rules (category/mask/group override).
 * @param isDraggable Enables pointer-driven dragging for this body.
 * @param dragConfig Drag tuning parameters.
 * @param onCollision Optional callback for contact events involving this body.
 * @param onSleepChanged Optional callback when sleeping state changes (engine-defined).
 * @param onDragStart Optional callback when drag interaction starts.
 * @param onDragEnd Optional callback when drag interaction ends (including cancel).
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
