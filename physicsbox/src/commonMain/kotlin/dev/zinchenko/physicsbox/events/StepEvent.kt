package dev.zinchenko.physicsbox.events

import androidx.compose.runtime.Immutable

/**
 * Global world step event payload.
 *
 * Emitted after the physics world advances the simulation time.
 * This event is typically used to:
 * - drive UI state derived from the simulation (FPS counters, timers)
 * - synchronize external systems with the simulation clock
 *
 * @param deltaSeconds Simulated time advanced by this step, in seconds.
 * @param subSteps Number of internal sub-steps performed to reach this advance (engine-defined;
 * commonly used when accumulating variable frame times into fixed timesteps).
 * @param stepIndex Monotonically increasing step counter.
 */
@Immutable
data class StepEvent(
    val deltaSeconds: Float,
    val subSteps: Int,
    val stepIndex: Long,
)
