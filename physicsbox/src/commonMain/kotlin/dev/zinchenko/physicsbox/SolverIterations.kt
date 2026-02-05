package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Solver iteration counts used by the physics backend.
 *
 * Many 2D physics engines expose two iteration loops:
 * - **velocity iterations**: solves constraints and applies impulses (stability for stacks/joints)
 * - **position iterations**: corrects penetrations (reduces overlap)
 *
 * Higher values typically improve stability/accuracy at the cost of CPU.
 *
 * @property velocity Number of velocity solver iterations per step (should be >= 0).
 * @property position Number of position solver iterations per step (should be >= 0).
 */
@Immutable
data class SolverIterations(
    val velocity: Int = 8,
    val position: Int = 3,
)
