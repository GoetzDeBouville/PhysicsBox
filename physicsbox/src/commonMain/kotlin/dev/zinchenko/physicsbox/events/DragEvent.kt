package dev.zinchenko.physicsbox.events

/**
 * Pointer-driven drag event payload in PhysicsBox container coordinates (px).
 */
enum class DragPhase {
    Start,
    Move,
    End,
    Cancel,
}

data class DragEvent(
    val key: Any,
    val phase: DragPhase = DragPhase.Move,
    val pointerXPx: Float = 0f,
    val pointerYPx: Float = 0f,
    val targetXPx: Float = pointerXPx,
    val targetYPx: Float = pointerYPx,
    val velocityXPxPerSec: Float = 0f,
    val velocityYPxPerSec: Float = 0f,
    val uptimeMillis: Long = 0L,
) {
    constructor(key: Any, xPx: Float, yPx: Float) : this(
        key = key,
        phase = DragPhase.Move,
        pointerXPx = xPx,
        pointerYPx = yPx,
        targetXPx = xPx,
        targetYPx = yPx,
    )
}
