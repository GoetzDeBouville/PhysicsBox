package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable

/**
 * Shape descriptor used by the body registration API.
 *
 * The shape describes the collision geometry that will be created for the body.
 * The actual dimensions are derived from the composable's measured size unless explicitly
 * provided by the shape implementation.
 *
 * The initial API exposes only [Box] and [Circle]. Additional shapes can be added later without
 * breaking source compatibility.
 */
@Immutable
sealed interface PhysicsShape {
    /**
     * Rectangle shape based on the composable bounds.
     *
     * The engine uses the current measured width/height of the node as the box extents.
     */
    data object Box : PhysicsShape

    /**
     * Circle shape.
     *
     * If [radiusPx] is `null`, the implementation may infer the radius from the current layout size
     * (for example, `min(width, height) / 2`).
     *
     * @property radiusPx Circle radius in pixels. Must be > 0 when specified.
     */
    data class Circle(val radiusPx: Float? = null) : PhysicsShape {
        init {
            if (radiusPx != null) {
                require(radiusPx > 0f) { "PhysicsShape.Circle.radiusPx must be > 0 when specified." }
            }
        }
    }
}
