package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Immutable snapshot of public world controls.
 */
@Immutable
data class PhysicsWorldSnapshot(
    val isPaused: Boolean,
    val gravity: PhysicsVector2,
    val stepConfig: StepConfig,
    val solverIterations: SolverIterations,
)
