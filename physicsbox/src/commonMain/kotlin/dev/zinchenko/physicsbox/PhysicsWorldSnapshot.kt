package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform

/**
 * Immutable world snapshot used for UI/runtime synchronization.
 *
 * A snapshot is typically produced by the runtime after stepping the simulation and then consumed by
 * Compose layout/placement code to render the latest transforms.
 *
 * Conventions:
 * - World-level values ([gravity], [stepConfig], [solverIterations]) are expressed in runtime/world units.
 * - Exported body transforms and velocities are expressed in *pixel space* (see [PhysicsBodySnapshot]).
 *
 * @property isPaused Whether the simulation is currently paused.
 * @property gravity World gravity vector (runtime/world units).
 * @property stepConfig Current step configuration (timestep/iteration counts/etc.).
 * @property solverIterations Convenience view over solver iteration counts.
 * @property bodies Flat list of exported body snapshots.
 * @property bodiesByKey Pre-indexed lookup map for fast key-to-body access during layout.
 * @property stepIndex Monotonic step counter (useful for diagnostics and frame correlation).
 */
@Immutable
data class PhysicsWorldSnapshot(
    val isPaused: Boolean,
    val gravity: PhysicsVector2,
    val stepConfig: StepConfig,
    val solverIterations: SolverIterations,
    val bodies: List<PhysicsBodySnapshot> = emptyList(),
    val bodiesByKey: Map<Any, PhysicsBodySnapshot> = bodies.associateBy { it.key },
    val stepIndex: Long = 0L,
)

/**
 * Immutable body state exported as part of [PhysicsWorldSnapshot].
 *
 * This DTO is intended for render-time usage: it contains only the data required to apply
 * translation/rotation and optionally drive effects based on motion.
 *
 * @property key Stable identifier of the body (`Modifier.physicsBody(key = ...)`).
 * @property transformPx Body transform in container pixel space (translation + rotation).
 * @property isAwake Whether the body is considered awake by the backend (not sleeping).
 * @property linearVelocityPxPerSecond Linear velocity in pixel space (px/s).
 */
@Immutable
data class PhysicsBodySnapshot(
    val key: Any,
    val transformPx: PhysicsTransform,
    val isAwake: Boolean,
    val linearVelocityPxPerSecond: PhysicsVector2,
)
