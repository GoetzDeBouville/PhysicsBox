package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Pixels-per-meter ratio used to map Compose coordinates to physics coordinates.
 *
 * Example: with `PxPerMeter(100f)`, an object moving by `1` meter in physics moves by `100` px on screen.
 */
@Immutable
data class PxPerMeter(
    val value: Float = 100f,
) {
    init {
        require(value > 0f) { "PxPerMeter value must be > 0." }
    }
}
