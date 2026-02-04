package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Small immutable 2D vector used by PhysicsBox API.
 *
 * Unit is context-dependent (`px`, `m`, `px/s`, `m/s`, and so on),
 * but axis orientation is always screen-oriented:
 * - `x > 0`: to the right
 * - `y > 0`: downward
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
