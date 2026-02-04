package dev.zinchenko.physicsbox.events

/**
 * Collision callback payload for body-level listeners.
 *
 * Keys map events back to composables registered via `Modifier.physicsBody(key = ...)`.
 */
data class CollisionEvent(
    val selfKey: Any,
    val otherKey: Any,
    val impulse: Float,
    val normalX: Float,
    val normalY: Float,
)
