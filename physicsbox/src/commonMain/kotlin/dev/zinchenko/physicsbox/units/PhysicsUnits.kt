package dev.zinchenko.physicsbox.units

import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.PxPerMeter
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Converts values between UI space (px/degrees) and physics space (meters/radians).
 *
 * Coordinate system contract (same for UI and physics):
 * - `+X` points right.
 * - `+Y` points down.
 *
 * This keeps conversion logic straightforward and allows using gravity as `(+0, +9.8)`.
 *
 * Scale contract:
 * - `meters = px / pxPerMeter`
 * - `px = meters * pxPerMeter`
 */
class PhysicsUnits(
    val pxPerMeter: PxPerMeter,
) {
    private val scale: Float = pxPerMeter.value
    private val inverseScale: Float = 1f / pxPerMeter.value

    fun pxToMeters(px: Float): Float = normalizeZero(px * inverseScale)

    fun metersToPx(meters: Float): Float = normalizeZero(meters * scale)

    fun pxToMeters(px: Int): Float = pxToMeters(px.toFloat())

    fun metersToIntPx(meters: Float): Int {
        val px = metersToPx(meters)
        return clamp(
            value = px,
            minValue = Int.MIN_VALUE.toFloat(),
            maxValue = Int.MAX_VALUE.toFloat(),
        ).roundToInt()
    }

    fun pxVecToMeters(v: PhysicsVector2): PhysicsVector2 = PhysicsVector2(
        x = pxToMeters(v.x),
        y = pxToMeters(v.y),
    )

    fun metersVecToPx(v: PhysicsVector2): PhysicsVector2 = PhysicsVector2(
        x = metersToPx(v.x),
        y = metersToPx(v.y),
    )

    fun radiansToDegrees(rad: Float): Float = normalizeZero(rad * RAD_TO_DEG)

    fun degreesToRadians(deg: Float): Float = normalizeZero(deg * DEG_TO_RAD)

    /**
     * Converts transform fields from `px + degrees` into `meters + radians`.
     *
     * Returned object keeps the same DTO type for API simplicity; only numeric interpretation changes.
     */
    fun transformPxToMeters(t: PhysicsTransform): PhysicsTransform = PhysicsTransform(
        positionPx = pxVecToMeters(t.positionPx),
        rotationDegrees = degreesToRadians(t.rotationDegrees),
    )

    /**
     * Converts transform fields from `meters + radians` into `px + degrees`.
     *
     * Returned object keeps the same DTO type for API simplicity; only numeric interpretation changes.
     */
    fun transformMetersToPx(t: PhysicsTransform): PhysicsTransform = PhysicsTransform(
        positionPx = metersVecToPx(t.positionPx),
        rotationDegrees = radiansToDegrees(t.rotationDegrees),
    )

    fun velocityPxToMetersPerSecond(pxPerSecond: Float): Float = pxToMeters(pxPerSecond)

    fun velocityMetersToPxPerSecond(metersPerSecond: Float): Float = metersToPx(metersPerSecond)

    fun velocityVecPxToMetersPerSecond(v: PhysicsVector2): PhysicsVector2 = pxVecToMeters(v)

    fun velocityVecMetersToPxPerSecond(v: PhysicsVector2): PhysicsVector2 = metersVecToPx(v)

    /**
     * Converts impulse from UI-scaled length domain to physics-scaled length domain.
     *
     * At API level mass is not modeled yet, so only length scale is converted.
     */
    fun impulsePxToPhysics(impulse: Float): Float = pxToMeters(impulse)

    /**
     * Converts impulse from physics-scaled length domain to UI-scaled length domain.
     *
     * At API level mass is not modeled yet, so only length scale is converted.
     */
    fun impulsePhysicsToPx(impulse: Float): Float = metersToPx(impulse)

    fun impulseVecPxToPhysics(v: PhysicsVector2): PhysicsVector2 = pxVecToMeters(v)

    fun impulseVecPhysicsToPx(v: PhysicsVector2): PhysicsVector2 = metersVecToPx(v)

    private fun normalizeZero(value: Float): Float = if (abs(value) <= EPS) 0f else value

    private companion object {
        private const val RAD_TO_DEG: Float = (180.0 / PI).toFloat()
        private const val DEG_TO_RAD: Float = (PI / 180.0).toFloat()
    }
}

internal const val EPS: Float = 1e-5f

internal fun approxEquals(a: Float, b: Float, eps: Float = EPS): Boolean {
    require(eps >= 0f) { "eps must be >= 0." }
    return abs(a - b) <= eps
}

internal fun clamp(
    value: Float,
    minValue: Float,
    maxValue: Float,
): Float {
    require(minValue <= maxValue) { "minValue must be <= maxValue." }
    return when {
        value < minValue -> minValue
        value > maxValue -> maxValue
        else -> value
    }
}
