package dev.zinchenko.physicsbox.events

import androidx.compose.runtime.Immutable

/**
 * Global world step event payload.
 */
@Immutable
data class StepEvent(
    val deltaSeconds: Float,
    val subSteps: Int,
    val stepIndex: Long,
)
