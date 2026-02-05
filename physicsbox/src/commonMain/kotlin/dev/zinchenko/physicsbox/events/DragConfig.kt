package dev.zinchenko.physicsbox.events

import androidx.compose.runtime.Immutable

/**
 * Drag behavior configuration used by [Modifier.physicsBody].
 *
 * This configuration controls how a body follows the pointer (finger/mouse) during dragging
 * and what happens when the drag ends (fling).
 *
 * PhysicsBox typically implements dragging in one of two ways:
 * 1) **Joint-style drag** (recommended): the body is attached to an invisible target via a
 *    spring-like constraint (e.g., Box2D/JBox2D `MouseJoint`). This feels natural and stable.
 * 2) **Direct control** (fallback): the engine drives the body toward the pointer target using
 *    velocity/impulses without creating a joint. This can be useful for very simple scenes or
 *    when joint behavior is undesirable.
 *
 * ## Parameters
 * ### [maxForce]
 * Upper bound for how much force the drag controller may apply to move the body toward the target.
 * Larger values make the body track the pointer more aggressively, but very large values can:
 * - cause jitter (especially with small bodies in meters, see `PxPerMeter`)
 * - create unrealistic “snapping”
 * - destabilize stacks/collisions
 *
 * When joint-style drag is used, the effective force is often scaled by body mass
 * (implementation detail), so heavier bodies may require more force to feel responsive.
 *
 * ### [frequencyHz]
 * Spring frequency in Hz when using joint-style drag. Higher values behave like a stiffer spring
 * (faster convergence to the pointer target). Typical UI-friendly values: `3..10`.
 *
 * - Low frequency (e.g., `1..2`) → “rubbery”, laggy follow
 * - High frequency (e.g., `10..20`) → very stiff, can jitter if timestep/scale is not tuned
 *
 * ### [dampingRatio]
 * Damping ratio of the spring system when using joint-style drag.
 * - `0.0` → no damping (oscillates a lot)
 * - `~0.7` → near “critically damped” feel for UI (fast settle with minimal oscillation)
 * - `> 1.0` → overdamped (sluggish, but stable)
 *
 * Note: the solver may still exhibit small oscillations depending on timestep, collisions, and scale.
 *
 * ### [useJointStyleDrag]
 * Selects the drag mode:
 * - `true` → joint-style drag (recommended default)
 * - `false` → direct control (engine-specific strategy; typically velocity/impulse toward target)
 *
 * Joint-style drag usually provides the best interaction feel in the presence of collisions,
 * because the body remains a fully simulated object constrained to a moving target.
 *
 * ### [maxFlingVelocityPxPerSec]
 * Upper bound for fling velocity applied when the pointer is released.
 * This clamps the velocity computed from pointer movement (VelocityTracker) to avoid extreme speeds
 * from noisy input sampling (especially on desktop mice) and to reduce tunneling through boundaries.
 *
 * Units are **pixels per second**. The engine converts this to physics units (`m/s`) using
 * the current `PxPerMeter` world scale:
 *
 * `velocityMps = velocityPxPerSec / pxPerMeter`
 *
 * ## Tuning notes
 * - If dragging feels “too heavy” or lags behind the finger: increase [maxForce] or [frequencyHz].
 * - If dragging jitters or shakes: reduce [frequencyHz], increase [dampingRatio], or reduce [maxForce].
 * - If fling launches objects too hard: reduce [maxFlingVelocityPxPerSec].
 *
 * ## Validation
 * This class enforces basic numeric constraints (positive values where required) to avoid undefined
 * behavior in the underlying physics solver.
 */
@Immutable
data class DragConfig(
    val maxForce: Float = 1_000f,
    val frequencyHz: Float = 5f,
    val dampingRatio: Float = 0.7f,
    val useJointStyleDrag: Boolean = true,
    val maxFlingVelocityPxPerSec: Float = 6_000f,
) {
    init {
        require(maxForce > 0f) { "DragConfig.maxForce must be > 0." }
        require(frequencyHz > 0f) { "DragConfig.frequencyHz must be > 0." }
        require(dampingRatio >= 0f) { "DragConfig.dampingRatio must be >= 0." }
        require(maxFlingVelocityPxPerSec > 0f) {
            "DragConfig.maxFlingVelocityPxPerSec must be > 0."
        }
    }
}
