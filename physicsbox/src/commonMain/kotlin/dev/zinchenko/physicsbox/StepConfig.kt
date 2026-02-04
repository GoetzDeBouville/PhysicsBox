package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Fixed-step simulation settings.
 *
 * `hz` controls target simulation frequency (`1 / hz` seconds per step).
 */
@Immutable
data class StepConfig(
    val hz: Float = 60f,
    val velocityIterations: Int = 8,
    val positionIterations: Int = 3,
    val maxSubSteps: Int = 3,
    val maxDeltaSeconds: Float = 1f / 15f,
) {
    init {
        require(hz > 0f) { "StepConfig.hz must be > 0." }
        require(velocityIterations > 0) { "StepConfig.velocityIterations must be > 0." }
        require(positionIterations > 0) { "StepConfig.positionIterations must be > 0." }
        require(maxSubSteps > 0) { "StepConfig.maxSubSteps must be > 0." }
        require(maxDeltaSeconds > 0f) { "StepConfig.maxDeltaSeconds must be > 0." }
    }
}
