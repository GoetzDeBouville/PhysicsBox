package dev.zinchenko.physicsbox.utils

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Shape
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates a regular convex [PhysicsShape.Polygon] in normalized local space.
 *
 * The returned polygon is centered at the body's origin and uses
 * [PhysicsShape.Polygon.VertexSpace.Normalized], where `x = -0.5..0.5` maps to the composable's
 * width and `y = -0.5..0.5` maps to its height. When applied to a non-square composable, the
 * result is stretched to match the measured bounds in the same way as the physics fixture.
 *
 * Vertices are generated in screen-oriented coordinates ([PhysicsVector2] uses `y > 0` downward).
 * The default [rotationDegrees] of `-90f` places the first vertex at the top.
 *
 * @param sides Number of polygon sides. Must stay within the physics backend vertex limit.
 * @param radius Distance from the center to each vertex in normalized local space. Must be finite
 *   and `> 0`. Values above `0.5f` are allowed, but they make the shape extend beyond the
 *   composable bounds.
 * @param rotationDegrees Clockwise rotation in degrees in the screen-oriented coordinate space.
 *   Must be finite.
 */
fun regularPolygonNormalized(
    sides: Int,
    radius: Float = 0.48f,
    rotationDegrees: Float = -90f,
): PhysicsShape.Polygon {
    require(sides in 3..8) { "sides must be in 3..8 for jbox2d PolygonShape." }
    require(radius.isFinite()) { "radius must be finite." }
    require(radius > 0f) { "radius must be > 0." }
    require(rotationDegrees.isFinite()) { "rotationDegrees must be finite." }

    val rot = rotationDegrees * (PI.toFloat() / 180f)
    val step = (2f * PI.toFloat()) / sides

    val verts = List(sides) { i ->
        val a = rot + step * i
        PhysicsVector2(
            x = cos(a) * radius,
            y = sin(a) * radius,
        )
    }

    return PhysicsShape.Polygon(
        vertices = verts,
        space = PhysicsShape.Polygon.VertexSpace.Normalized,
    )
}

/**
 * Creates a Compose [Shape] that matches a [PhysicsShape.Polygon].
 *
 * The mapping uses the same local-origin and vertex-space rules as the physics fixture adapter, so
 * the visual clip aligns with the collision geometry for both normalized and pixel-based polygons.
 *
 * @param polygon Polygon descriptor to render as a Compose shape.
 */
fun polygonComposeShape(polygon: PhysicsShape.Polygon): Shape =
    GenericShape { size, _ ->
        val verts = polygon.vertices
        if (verts.size < 3 || size.width <= 0f || size.height <= 0f) return@GenericShape

        val halfWidth = size.width * 0.5f
        val halfHeight = size.height * 0.5f

        when (polygon.space) {
            PhysicsShape.Polygon.VertexSpace.Normalized -> {
                moveTo(
                    x = halfWidth + verts[0].x * size.width,
                    y = halfHeight + verts[0].y * size.height,
                )
                for (i in 1 until verts.size) {
                    val vertex = verts[i]
                    lineTo(
                        x = halfWidth + vertex.x * size.width,
                        y = halfHeight + vertex.y * size.height,
                    )
                }
            }

            PhysicsShape.Polygon.VertexSpace.Px -> {
                moveTo(
                    x = halfWidth + verts[0].x,
                    y = halfHeight + verts[0].y,
                )
                for (i in 1 until verts.size) {
                    val vertex = verts[i]
                    lineTo(
                        x = halfWidth + vertex.x,
                        y = halfHeight + vertex.y,
                    )
                }
            }
        }
        close()
    }
