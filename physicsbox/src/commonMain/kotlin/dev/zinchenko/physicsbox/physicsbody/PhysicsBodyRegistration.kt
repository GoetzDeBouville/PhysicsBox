package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Stable
import dev.zinchenko.physicsbox.events.DragConfig

@Stable
internal data class PhysicsBodyRegistration(
    val key: Any,
    val config: PhysicsBodyConfig,
    val shape: PhysicsShape,
    val filter: CollisionFilter,
    val isDraggable: Boolean,
    val dragConfig: DragConfig,
    val callbacks: PhysicsBodyCallbacks,
) {
    val onCollision get() = callbacks.onCollision

    val onSleepChanged get() = callbacks.onSleepChanged

    val onDragStart get() = callbacks.onDragStart

    val onDragEnd get() = callbacks.onDragEnd
}
