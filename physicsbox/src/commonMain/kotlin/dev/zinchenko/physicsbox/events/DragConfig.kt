package dev.zinchenko.physicsbox.events

import androidx.compose.runtime.Immutable

/**
 * Drag behavior configuration used by [Modifier.physicsBody].
 */
@Immutable
data class DragConfig(
    val maxForce: Float = 1_000f,
    val frequencyHz: Float = 5f,
    val dampingRatio: Float = 0.7f,
    val useJointStyleDrag: Boolean = true,
) {
    init {
        require(maxForce > 0f) { "DragConfig.maxForce must be > 0." }
        require(frequencyHz > 0f) { "DragConfig.frequencyHz must be > 0." }
        require(dampingRatio >= 0f) { "DragConfig.dampingRatio must be >= 0." }
    }
}
