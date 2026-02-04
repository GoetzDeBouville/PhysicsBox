package dev.zinchenko.physicsbox

/**
 * Commands that can be queued from UI code and consumed by runtime simulation code.
 */
sealed interface PhysicsCommand {
    data class EnqueueImpulse(
        val key: Any,
        val impulseX: Float,
        val impulseY: Float,
        val wake: Boolean = true,
    ) : PhysicsCommand

    data class EnqueueVelocity(
        val key: Any,
        val velocityX: Float,
        val velocityY: Float,
    ) : PhysicsCommand

    data object ResetWorld : PhysicsCommand
}
