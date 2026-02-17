package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Stable
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragEvent

/**
 * Runtime callback holder for a single physics body key.
 *
 * This type is internal and intentionally decoupled from engine/runtime implementations.
 */
@Stable
internal class PhysicsBodyCallbacks(
    val onCollision: ((CollisionEvent) -> Unit)? = null,
    val onSleepChanged: ((Boolean) -> Unit)? = null,
    val onDragStart: ((DragEvent) -> Unit)? = null,
    val onDragEnd: ((DragEvent) -> Unit)? = null,
)
