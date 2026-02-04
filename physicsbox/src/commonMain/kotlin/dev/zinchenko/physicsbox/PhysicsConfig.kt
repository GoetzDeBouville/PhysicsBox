package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Global configuration for a [PhysicsBox] world.
 *
 * @property boundaries world edge settings. When enabled, bodies collide with container bounds.
 * @property worldScale conversion ratio from UI pixels to physics meters.
 * @property step fixed-step simulation settings.
 */
@Immutable
data class PhysicsBoxConfig(
    val boundaries: BoundariesConfig = BoundariesConfig(),
    val worldScale: PxPerMeter = PxPerMeter(),
    val step: StepConfig = StepConfig(),
)
