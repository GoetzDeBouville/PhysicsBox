package dev.zinchenko.physicsbox

import dev.zinchenko.physicsbox.events.DragConfig

/**
 * Commands produced by UI/layout code and consumed by the runtime physics backend.
 *
 * Commands are enqueued via [PhysicsBoxState.enqueueCommand] (or its convenience helpers) and
 * drained by the runtime integration.
 *
 * Naming convention:
 * - fields with `Px` / `PxPerSec` suffix are expressed in *container pixel* space
 * - fields without suffix are interpreted by the runtime (often physics/world units)
 */
sealed interface PhysicsCommand {

    /**
     * Applies a linear impulse to a body.
     *
     * @param key Body key used in `Modifier.physicsBody(key = ...)`.
     * @param impulseXPx X component of impulse in container pixels (Px).
     * Converted internally to physics impulse units by [dev.zinchenko.physicsbox.units.PhysicsUnits].
     * @param impulseYPx Y component of impulse in container pixels (Px).
     * Converted internally to physics impulse units by [dev.zinchenko.physicsbox.units.PhysicsUnits].
     * @param wake Whether the backend should wake the body if supported.
     */
    data class EnqueueImpulse(
        val key: Any,
        val impulseXPx: Float,
        val impulseYPx: Float,
        val wake: Boolean = true,
    ) : PhysicsCommand

    /**
     * Sets/overwrites linear velocity of a body.
     *
     * @param key Body key used in `Modifier.physicsBody(key = ...)`.
     * @param velocityXPxPerSec X component in container pixels per second (Px/s).
     * Converted internally to meters per second by [dev.zinchenko.physicsbox.units.PhysicsUnits].
     * @param velocityYPxPerSec Y component in container pixels per second (Px/s).
     * Converted internally to meters per second by [dev.zinchenko.physicsbox.units.PhysicsUnits].
     */
    data class EnqueueVelocity(
        val key: Any,
        val velocityXPxPerSec: Float,
        val velocityYPxPerSec: Float,
    ) : PhysicsCommand

    /**
     * Begins a drag interaction for a body.
     *
     * @param key Body key.
     * @param grabPointPx Point on the body that was "grabbed" (container px).
     * @param pointerId Optional pointer identifier (multi-touch / pointer tracking).
     * @param targetPx Initial drag target (container px).
     * @param dragConfig Drag tuning parameters.
     */
    data class BeginDrag(
        val key: Any,
        val grabPointPx: PhysicsVector2,
        val pointerId: Long? = null,
        val targetPx: PhysicsVector2,
        val dragConfig: DragConfig,
    ) : PhysicsCommand

    /**
     * Updates drag target for an active drag interaction.
     *
     * @param key Body key.
     * @param targetPx New target position (container px).
     */
    data class UpdateDrag(
        val key: Any,
        val targetPx: PhysicsVector2,
    ) : PhysicsCommand

    /**
     * Ends a drag interaction and provides release velocity.
     *
     * @param key Body key.
     * @param velocityPxPerSec Release velocity estimated from pointer movement (px/s).
     */
    data class EndDrag(
        val key: Any,
        val velocityPxPerSec: PhysicsVector2,
    ) : PhysicsCommand

    /**
     * Cancels an active drag interaction.
     */
    data class CancelDrag(
        val key: Any,
    ) : PhysicsCommand

    /**
     * Updates world gravity vector.
     *
     * Units are interpreted by the runtime (typically physics units, e.g. m/s²).
     */
    data class SetWorldGravity(
        val gravity: PhysicsVector2,
    ) : PhysicsCommand

    /**
     * Requests a full runtime world reset.
     *
     * Backends should recreate the world state and clear transient contacts/joints. UI-level state
     * (pause/gravity/step config) is managed by [PhysicsBoxState.reset].
     */
    data object ResetWorld : PhysicsCommand
}
