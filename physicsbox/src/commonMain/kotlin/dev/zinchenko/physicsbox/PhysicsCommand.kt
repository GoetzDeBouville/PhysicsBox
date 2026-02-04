package dev.zinchenko.physicsbox

import dev.zinchenko.physicsbox.events.DragConfig

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

    data class BeginDrag(
        val key: Any,
        val grabPointPx: PhysicsVector2,
        val pointerId: Long? = null,
        val targetPx: PhysicsVector2,
        val dragConfig: DragConfig,
    ) : PhysicsCommand

    data class UpdateDrag(
        val key: Any,
        val targetPx: PhysicsVector2,
    ) : PhysicsCommand

    data class EndDrag(
        val key: Any,
        val velocityPxPerSec: PhysicsVector2,
    ) : PhysicsCommand

    data class CancelDrag(
        val key: Any,
    ) : PhysicsCommand

    data object ResetWorld : PhysicsCommand
}
