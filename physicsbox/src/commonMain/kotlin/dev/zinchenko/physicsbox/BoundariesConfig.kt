package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Optional static boundaries around the [PhysicsBox] container.
 *
 * The boundaries are typically left/top/right/bottom walls generated from the container size.
 */
@Immutable
data class BoundariesConfig(
    val enabled: Boolean = true,
    val restitution: Float = 0.2f,
    val friction: Float = 0.3f,
    val thicknessPx: Float = 64f,
) {
    init {
        require(restitution >= 0f) { "Boundaries restitution must be >= 0." }
        require(friction >= 0f) { "Boundaries friction must be >= 0." }
        require(thicknessPx > 0f) { "Boundaries thicknessPx must be > 0." }
    }
}
