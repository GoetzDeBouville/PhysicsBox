package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform

/**
 * Immutable world snapshot for UI/runtime synchronization.
 *
 * World-level values ([gravity], [stepConfig], [solverIterations]) stay in physics units.
 * Body transforms and linear velocities in [bodies] are exported in pixel space.
 */
@Immutable
data class PhysicsWorldSnapshot(
    val isPaused: Boolean,
    val gravity: PhysicsVector2,
    val stepConfig: StepConfig,
    val solverIterations: SolverIterations,
    val bodies: List<PhysicsBodySnapshot> = emptyList(),
    val stepIndex: Long = 0L,
)

/**
 * Immutable body state exported as part of [PhysicsWorldSnapshot].
 */
@Immutable
data class PhysicsBodySnapshot(
    val key: Any,
    val transformPx: PhysicsTransform,
    val isAwake: Boolean,
    val linearVelocityPxPerSecond: PhysicsVector2,
)
