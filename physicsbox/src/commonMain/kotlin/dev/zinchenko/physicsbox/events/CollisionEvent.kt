package dev.zinchenko.physicsbox.events

/**
 * Collision callback payload for body-level listeners.
 *
 * The engine reports a collision between two registered bodies. Bodies are identified by their
 * registration keys from `Modifier.physicsBody(key = ...)`.
 *
 * ### Impulse and normal
 * - [impulse] is a scalar magnitude representing the strength of the contact resolution for this
 * event (engine-specific; typically proportional to momentum exchange).
 * - ([normalX], [normalY]) is the contact normal in world/container space as reported by the engine.
 * In the current backend the normal points from this body toward the other body; conventions may
 * differ across physics engines.
 *
 * All numeric values are intended for UI feedback / effects (sound volume, haptics, particles) and
 * should not be treated as strictly physically accurate across different engines or solver settings.
 *
 * @param selfKey Key of the body that owns the listener (the “this” body).
 * @param otherKey Key of the other body involved in the collision.
 * @param impulse Contact impulse magnitude (engine-defined units).
 * @param normalX X component of the reported collision normal.
 * @param normalY Y component of the reported collision normal.
 */
data class CollisionEvent(
    val selfKey: Any,
    val otherKey: Any,
    val impulse: Float,
    val normalX: Float,
    val normalY: Float,
)
