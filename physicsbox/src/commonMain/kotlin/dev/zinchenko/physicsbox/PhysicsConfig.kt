package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.units.PhysicsUnits

/**
 * Global configuration for a [PhysicsBox] world.
 *
 * @property boundaries world edge settings. When enabled, bodies collide with container bounds.
 * @property worldScale conversion ratio from UI pixels to physics meters (`px / meter`).
 * @property step fixed-step simulation settings.
 */
@Immutable
data class PhysicsBoxConfig(
    val boundaries: BoundariesConfig = BoundariesConfig(),
    val worldScale: PxPerMeter = PhysicsDefaults.WorldScale,
    val step: StepConfig = StepConfig(),
)

/**
 * Creates a unit-conversion helper bound to [PhysicsBoxConfig.worldScale].
 *
 * This allocates a new [PhysicsUnits] instance on each call. Runtime code should cache it
 * per world/state if used in a hot loop.
 */
fun PhysicsBoxConfig.units(): PhysicsUnits = PhysicsUnits(worldScale)
