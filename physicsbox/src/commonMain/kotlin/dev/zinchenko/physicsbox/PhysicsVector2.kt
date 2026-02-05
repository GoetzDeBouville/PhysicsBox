package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Small immutable 2D vector used by PhysicsBox API.
 *
 * ## Axis orientation
 * Coordinates follow the screen-oriented convention:
 * - `x > 0` → to the right
 * - `y > 0` → downward
 *
 * ## Units are context-dependent
 * This type is deliberately unit-agnostic: the meaning depends on where it is used:
 * - Positions can be in **pixels** (`px`) or **meters** (`m`)
 * - Velocities can be in **px/s** or **m/s**
 * - Accelerations can be in **px/s²** or **m/s²**
 *
 * The API will specify the expected unit in parameter/property names or KDoc
 * (e.g., `positionPx`, `velocityPxPerSec`, `gravityMps2`).
 *
 * ## Relationship to `PxPerMeter`
 * When converting between UI and physics:
 * - `meters = pixels / pxPerMeter.value`
 * - `pixels = meters * pxPerMeter.value`
 *
 * That means for vectors:
 * - `vMeters = vPixels / pxPerMeter`
 * - `vPixels = vMeters * pxPerMeter`
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
