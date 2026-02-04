package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.PhysicsVector2

/**
 * Initial placement for a body before simulation starts.
 *
 * UI-facing interpretation:
 * - [positionPx] is in pixels.
 * - [rotationDegrees] is in degrees.
 *
 * Axis orientation follows Compose coordinates:
 * `+X` right, `+Y` downward.
 *
 * Conversion helpers in `units/PhysicsUnits.kt` can map this DTO to
 * meter/radian space for runtime integration.
 */
@Immutable
data class PhysicsTransform(
    val positionPx: PhysicsVector2 = PhysicsVector2.Zero,
    val rotationDegrees: Float = 0f,
)
