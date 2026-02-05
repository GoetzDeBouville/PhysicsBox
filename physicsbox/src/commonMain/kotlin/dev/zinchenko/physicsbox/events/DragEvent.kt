package dev.zinchenko.physicsbox.events

/**
 * Phase of a pointer-driven drag interaction for a physics body.
 *
 * - [Start] Drag has started (pointer captured / controller created).
 * - [Move] Pointer moved while dragging.
 * - [End] Drag finished normally (pointer released).
 * - [Cancel] Drag aborted (gesture cancelled, pointer lost, interruption).
 */
enum class DragPhase {
    Start,
    Move,
    End,
    Cancel,
}

/**
 * Pointer-driven drag event payload in `PhysicsBox` container coordinates (pixels).
 *
 * Two positions are provided:
 * - [pointerXPx]/[pointerYPx]: the raw pointer location (finger/mouse) in container px.
 * - [targetXPx]/[targetYPx]: the effective target used by the drag controller (may differ from
 *   pointer position due to anchoring, clamping, or other engine-defined adjustments).
 *
 * Velocity components ([velocityXPxPerSec], [velocityYPxPerSec]) represent the estimated pointer
 * velocity in px/s near the time of the event (useful for fling on release and UI effects).
 *
 * @param key Physics body key associated with this drag.
 * @param phase Drag lifecycle phase.
 * @param pointerXPx Raw pointer X in container px.
 * @param pointerYPx Raw pointer Y in container px.
 * @param targetXPx Target X in container px used by the drag controller.
 * @param targetYPx Target Y in container px used by the drag controller.
 * @param velocityXPxPerSec Estimated pointer velocity X in px/s.
 * @param velocityYPxPerSec Estimated pointer velocity Y in px/s.
 * @param uptimeMillis Event time in uptime millis (suitable for ordering and duration calculations).
 */
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
    /**
     * Convenience constructor for a simple move/update event where the drag target equals the
     * pointer position.
     */
    constructor(key: Any, xPx: Float, yPx: Float) : this(
        key = key,
        phase = DragPhase.Move,
        pointerXPx = xPx,
        pointerYPx = yPx,
        targetXPx = xPx,
        targetYPx = yPx,
    )
}
