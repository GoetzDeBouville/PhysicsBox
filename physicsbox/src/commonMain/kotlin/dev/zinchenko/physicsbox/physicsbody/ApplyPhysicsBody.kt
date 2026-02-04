package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.ui.Modifier
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent

internal fun applyPhysicsBody(
    modifier: Modifier,
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
): Modifier {
    require(key != Unit) { "physicsBody key must be a stable identity, Unit is not allowed." }
    return modifier.then(
        PhysicsBodyModifierElement(
            registration = PhysicsBodyRegistration(
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
            ),
        ),
    )
}
