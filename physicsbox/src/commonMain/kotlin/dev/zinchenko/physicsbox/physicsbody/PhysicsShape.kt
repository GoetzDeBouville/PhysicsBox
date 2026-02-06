package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.PhysicsVector2

/**
 * Shape descriptor used by the body registration API.
 *
 * The shape describes the collision geometry that will be created for the body.
 * The actual dimensions are derived from the composable's measured size unless explicitly
 * provided by the shape implementation.
 *
 * The public API exposes [Box], [Circle], and [Polygon]. Additional shapes can be added later
 * without breaking source compatibility.
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

    /**
     * Convex polygon shape (Box2D/jbox2d-style).
     *
     * IMPORTANT:
     * - Only **convex** polygons are supported by `PolygonShape`.
     * - Vertex count is limited by the physics backend (typically <= 8).
     * - Vertices must be non-self-intersecting and should not be degenerate (zero area).
     *
     * Vertex coordinates are expressed in local body space relative to the body's center.
     *
     * @property vertices Polygon vertices in local space (see [space]).
     * @property space How to interpret [vertices]:
     *  - [VertexSpace.Normalized] means each vertex is in normalized bounds relative to composable size:
     *      `x = -0.5..0.5` maps to `-width/2 .. width/2`,
     *      `y = -0.5..0.5` maps to `-height/2 .. height/2`.
     *    Values outside [-0.5, 0.5] are allowed but will make the physical shape exceed the UI bounds.
     *  - [VertexSpace.Px] means each vertex is expressed directly in pixels relative to the center.
     */
    data class Polygon(
        val vertices: List<PhysicsVector2>,
        val space: VertexSpace = VertexSpace.Normalized,
    ) : PhysicsShape {

        enum class VertexSpace { Normalized, Px }

        init {
            require(vertices.size >= 3) { "PhysicsShape.Polygon must have at least 3 vertices." }
            require(vertices.all { it.x.isFinite() && it.y.isFinite() }) { "Polygon vertices must be finite." }
        }
    }
}
