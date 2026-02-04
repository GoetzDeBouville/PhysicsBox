package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Small immutable 2D vector used by PhysicsBox API.
 *
 * Gravity uses a screen-oriented coordinate system by default:
 * `x > 0` to the right, `y > 0` downward.
 */
@Immutable
data class PhysicsVector2(
    val x: Float,
    val y: Float,
) {
    companion object {
        val Zero: PhysicsVector2 = PhysicsVector2(0f, 0f)
    }
}
