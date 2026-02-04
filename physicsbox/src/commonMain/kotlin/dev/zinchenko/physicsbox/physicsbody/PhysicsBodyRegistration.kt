package dev.zinchenko.physicsbox.physicsbody

import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent

internal data class PhysicsBodyRegistration(
    val key: Any,
    val config: PhysicsBodyConfig,
    val shape: PhysicsShape,
    val filter: CollisionFilter,
    val isDraggable: Boolean,
    val dragConfig: DragConfig,
    val onCollision: ((CollisionEvent) -> Unit)?,
    val onSleepChanged: ((Boolean) -> Unit)?,
    val onDragStart: ((DragEvent) -> Unit)?,
    val onDragEnd: ((DragEvent) -> Unit)?,
)
