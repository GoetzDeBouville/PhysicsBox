package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Pixels-per-meter ratio used to map UI coordinates to physics coordinates.
 *
 * Example: with `PxPerMeter(100f)`, an object moving by `1` meter in physics
 * moves by `100` px on screen.
 *
 * A larger value means "more pixels in one meter", so simulated objects become
 * visually larger for the same physical size.
 */
@Immutable
data class PxPerMeter(
    val value: Float = 100f,
) {
    init {
        require(value.isFinite() && value > 0f) {
            "PxPerMeter.value must be a finite number > 0."
        }
    }
}
