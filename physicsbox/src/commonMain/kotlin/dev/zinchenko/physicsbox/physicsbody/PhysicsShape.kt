package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable

/**
 * Shape descriptor used by the body registration API.
 *
 * Only [Box] and [Circle] are exposed in the initial API surface.
 * Polygon/custom shapes can be added later without breaking this contract.
 */
@Immutable
sealed interface PhysicsShape {
    /**
     * Use the composable bounds as a rectangle shape.
     */
    data object Box : PhysicsShape

    /**
     * Circle shape.
     *
     * If [radiusPx] is null, implementation may infer radius from current layout size.
     */
    data class Circle(val radiusPx: Float? = null) : PhysicsShape {
        init {
            if (radiusPx != null) {
                require(radiusPx > 0f) { "PhysicsShape.Circle.radiusPx must be > 0 when specified." }
            }
        }
    }
}
