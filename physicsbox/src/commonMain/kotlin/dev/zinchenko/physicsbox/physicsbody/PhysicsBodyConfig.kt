package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable

/**
 * Physical parameters for a composable body.
 */
@Immutable
data class PhysicsBodyConfig(
    val bodyType: BodyType = BodyType.Dynamic,
    val density: Float = 1f,
    val friction: Float = 0.3f,
    val restitution: Float = 0.2f,
    val linearDamping: Float = 0f,
    val angularDamping: Float = 0f,
    val fixedRotation: Boolean = false,
    val allowSleep: Boolean = true,
    val isBullet: Boolean = false,
    val gravityScale: Float = 1f,
    val initialTransform: PhysicsTransform = PhysicsTransform(),
) {
    init {
        require(density >= 0f) { "PhysicsBodyConfig.density must be >= 0." }
        require(friction >= 0f) { "PhysicsBodyConfig.friction must be >= 0." }
        require(restitution >= 0f) { "PhysicsBodyConfig.restitution must be >= 0." }
        require(linearDamping >= 0f) { "PhysicsBodyConfig.linearDamping must be >= 0." }
        require(angularDamping >= 0f) { "PhysicsBodyConfig.angularDamping must be >= 0." }
    }
}
