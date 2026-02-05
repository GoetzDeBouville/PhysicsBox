package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.PhysicsVector2

/**
 * Initial placement for a body before simulation starts.
 *
 * UI-facing interpretation:
 * - [vector2] gravity vector. Use [dev.zinchenko.physicsbox.PhysicsDefaults.Gravity] for default gravity by `y` axis (9.8 **m/s²**). Note, the effect on the screen is also depends on [dev.zinchenko.physicsbox.PhysicsBoxConfig.worldScale] paramater..
 * - [rotationDegrees] is in degrees (when object appears).
 *
 * Axis orientation follows Compose coordinates:
 * `+X` right, `+Y` downward.
 *
 * Conversion helpers in `units/PhysicsUnits.kt` can map this DTO to
 * meter/radian space for runtime integration.
 */
@Immutable
data class PhysicsTransform(
    val vector2: PhysicsVector2 = PhysicsVector2.Zero,
    val rotationDegrees: Float = 0f,
)
