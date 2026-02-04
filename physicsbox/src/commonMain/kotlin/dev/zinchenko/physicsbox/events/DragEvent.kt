package dev.zinchenko.physicsbox.events

/**
 * Pointer-driven drag event payload.
 */
data class DragEvent(
    val key: Any,
    val xPx: Float,
    val yPx: Float,
)
