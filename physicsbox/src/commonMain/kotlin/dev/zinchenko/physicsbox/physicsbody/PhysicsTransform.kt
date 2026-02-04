package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.PhysicsVector2

/**
 * Initial placement for a body before simulation starts.
 *
 * Position is expressed in pixels in container coordinates.
 */
@Immutable
data class PhysicsTransform(
    val positionPx: PhysicsVector2 = PhysicsVector2.Companion.Zero,
    val rotationDegrees: Float = 0f,
)
