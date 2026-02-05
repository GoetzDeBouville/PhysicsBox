package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Fixed-step simulation settings for the PhysicsBox world.
 *
 * PhysicsBox advances the world using a **fixed timestep** (a constant `dt`) rather than the raw
 * frame delta. This makes the simulation more stable and predictable across different refresh rates
 * (60/90/120 Hz) and under temporary frame drops.
 *
 * ## Fixed timestep model
 * Let `dt = 1 / hz`.
 *
 * Each rendered frame, PhysicsBox accumulates real time (`frameDt`) and performs a bounded number of
 * fixed steps:
 *
 * - `accumulator += clamp(frameDt, 0..maxDeltaSeconds)`
 * - `while (accumulator >= dt && steps < maxSubSteps) { world.step(dt); accumulator -= dt; steps++ }`
 *
 * If the device lags heavily, the loop will stop at [maxSubSteps] to prevent a “spiral of death”
 * (endless catch-up) and to keep UI responsive.
 *
 * ## Parameters
 * ### [hz]
 * Target simulation frequency in Hertz. The fixed step duration is `dt = 1 / hz` seconds.
 *
 * Guidance:
 * - `60` is a good default for UI physics.
 * - Higher values (e.g., `90`, `120`) can improve responsiveness but increase CPU cost.
 * - Lower values (e.g., `30`) reduce CPU but can feel sluggish and reduce stability for fast motion.
 *
 * ### [velocityIterations] and [positionIterations]
 * Solver iteration counts used by Box2D-style engines.
 *
 * - [velocityIterations] improves velocity constraint solving (friction, restitution, joint constraints).
 * - [positionIterations] improves positional correction (reduces penetration and improves stacking).
 *
 * Guidance:
 * - Defaults like `8 / 3` are common for interactive scenes.
 * - Increase if stacks jitter or objects penetrate too much (CPU cost rises).
 * - Decrease for performance if the scene is simple and tolerates more softness.
 *
 * ### [maxSubSteps]
 * Maximum number of fixed steps allowed per rendered frame.
 *
 * This caps the amount of catch-up work when frames are slow. If the app stalls and a large time gap
 * occurs, the simulation will not attempt to “replay” all missed steps in one frame.
 *
 * Guidance:
 * - `3..8` is typical. Higher values improve catch-up smoothness but can spike CPU.
 * - If you see stuttering after temporary lag, you may increase this (within reason).
 *
 * ### [maxDeltaSeconds]
 * Maximum allowed `frameDt` contributed to the accumulator, in seconds.
 *
 * This prevents extremely large deltas (e.g., app in background, debugger stop, window dragged)
 * from injecting a huge time step into the simulation, which often destabilizes the solver.
 *
 * Guidance:
 * - `1/15` (~0.066s) means PhysicsBox will treat any single-frame delta larger than ~66ms as 66ms.
 * - Lower values make the simulation more resistant to big stalls but can slow catch-up.
 *
 * ## Interaction with world scale
 * These settings operate in **seconds** and are independent of `PxPerMeter`. However, stability is
 * affected indirectly by scale: extremely small bodies in meters (from very large `PxPerMeter`)
 * can require higher iteration counts or gentler forces/velocities to remain stable.
 */
@Immutable
data class StepConfig(
    val hz: Float = 60f,
    val velocityIterations: Int = 8,
    val positionIterations: Int = 3,
    val maxSubSteps: Int = 3,
    val maxDeltaSeconds: Float = 1f / 15f,
) {
    init {
        require(hz > 0f) { "StepConfig.hz must be > 0." }
        require(velocityIterations > 0) { "StepConfig.velocityIterations must be > 0." }
        require(positionIterations > 0) { "StepConfig.positionIterations must be > 0." }
        require(maxSubSteps > 0) { "StepConfig.maxSubSteps must be > 0." }
        require(maxDeltaSeconds > 0f) { "StepConfig.maxDeltaSeconds must be > 0." }
    }
}
