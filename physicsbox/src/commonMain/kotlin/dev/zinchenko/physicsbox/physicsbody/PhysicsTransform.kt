package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.PhysicsVector2

/**
 * Initial transform for a body before the simulation starts.
 *
 * UI-facing interpretation:
 * - [vector2] is a 2D position vector.
 * - [rotationDegrees] is the initial rotation in degrees.
 *
 * Coordinate system follows Compose:
 * `+X` to the right, `+Y` downward.
 *
 * Runtime conversion helpers (see `units/PhysicsUnits.kt`) may map this DTO to physics units
 * (meters/radians) depending on the configured world scale.
 *
 * @property vector2 Initial position (UI space).
 * @property rotationDegrees Initial rotation in degrees.
 */
@Immutable
data class PhysicsTransform(
    val vector2: PhysicsVector2 = PhysicsVector2.Zero,
    val rotationDegrees: Float = 0f,
)
