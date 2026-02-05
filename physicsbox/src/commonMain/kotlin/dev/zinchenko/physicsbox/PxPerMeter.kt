package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Pixels-per-meter ratio used to map UI coordinates (px) to physics coordinates (m).
 *
 * ## Definition
 * `value` is **px per 1 meter** (`px/m`).
 *
 * Conversions:
 * - `meters = pixels / value`
 * - `pixels = meters * value`
 *
 * ## What changing `value` does
 * - Larger `value` ⇒ **more pixels per meter**:
 *   - The same UI size in pixels becomes **smaller** in meters.
 *   - The same physical speed in `m/s` becomes **faster** on screen in `px/s`.
 * - Smaller `value` ⇒ fewer pixels per meter:
 *   - UI sizes become **larger** in meters.
 *   - The same physical speed in `m/s` becomes **slower** on screen in `px/s`.
 *
 * ## Practical guidance
 * For stable UI physics, aim for typical body sizes to land roughly in the **0.1–2.0 m** range.
 * Example for a 120 px card:
 * - `PxPerMeter(120f)` → ~1.0 m
 * - `PxPerMeter(240f)` → ~0.5 m
 *
 * Too large `value` can make objects extremely small in meters, which may degrade stacking stability.
 *
 * Example: with `PxPerMeter(100f)`, an object moving by `1` meter in physics moves by `100` px on screen.
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
