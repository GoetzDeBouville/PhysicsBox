package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Convenience view over solver iteration counts.
 */
@Immutable
data class SolverIterations(
    val velocity: Int = 8,
    val position: Int = 3,
)
