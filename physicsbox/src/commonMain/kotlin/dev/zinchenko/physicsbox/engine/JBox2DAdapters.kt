package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.CollisionFilter
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.units.PhysicsUnits
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Settings
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef
import kotlin.math.min
import org.jbox2d.dynamics.BodyType as JBoxBodyType

internal fun BodyType.toJBoxBodyType(): JBoxBodyType = when (this) {
    BodyType.Static -> JBoxBodyType.STATIC
    BodyType.Dynamic -> JBoxBodyType.DYNAMIC
    BodyType.Kinematic -> JBoxBodyType.KINEMATIC
}

internal fun CollisionFilter.toJBoxFilter(): Filter = Filter().also { jFilter ->
    jFilter.categoryBits = categoryBits
    jFilter.maskBits = maskBits
    jFilter.groupIndex = groupIndex
}

internal fun createFixtureDef(
    shape: PhysicsShape,
    widthPx: Int,
    heightPx: Int,
    config: PhysicsBodyConfig,
    filter: CollisionFilter,
    units: PhysicsUnits,
): FixtureDef? {
    if (widthPx <= 0 || heightPx <= 0) return null

    val fixtureDef = FixtureDef()
    fixtureDef.density = config.density
    fixtureDef.friction = config.friction
    fixtureDef.restitution = config.restitution
    fixtureDef.filter = filter.toJBoxFilter()
    fixtureDef.shape = when (shape) {
        is PhysicsShape.Box -> {
            val halfWidthMeters = units.pxToMeters(widthPx.toFloat()) * 0.5f
            val halfHeightMeters = units.pxToMeters(heightPx.toFloat()) * 0.5f
            if (halfWidthMeters <= 0f || halfHeightMeters <= 0f) return null
            PolygonShape().apply { setAsBox(halfWidthMeters, halfHeightMeters) }
        }

        is PhysicsShape.Circle -> {
            val radiusPx = shape.radiusPx ?: (min(widthPx, heightPx) * 0.5f)
            val radiusMeters = units.pxToMeters(radiusPx)
            if (radiusMeters <= 0f) return null
            CircleShape().apply { m_radius = radiusMeters }
        }

        is PhysicsShape.Polygon -> {
            val maxVerts = Settings.maxPolygonVertices
            val vertsSrc = shape.vertices
            if (vertsSrc.size > maxVerts) return null

            val vertsMeters = Array(vertsSrc.size) { i ->
                val v = vertsSrc[i]
                val localPx = when (shape.space) {
                    PhysicsShape.Polygon.VertexSpace.Normalized -> {
                        PhysicsVector2(
                            x = v.x * widthPx.toFloat(),
                            y = v.y * heightPx.toFloat(),
                        )
                    }

                    PhysicsShape.Polygon.VertexSpace.Px -> v
                }

                Vec2(
                    units.pxToMeters(localPx.x),
                    units.pxToMeters(localPx.y),
                )
            }

            val area = signedArea(vertsMeters)
            if (kotlin.math.abs(area) < 1e-6f) return null

            val vertsCCW = if (area < 0f) vertsMeters.reversedArray() else vertsMeters

            PolygonShape().apply { set(vertsCCW, vertsCCW.size) }
        }
    }
    return fixtureDef
}

private fun signedArea(vertices: Array<Vec2>): Float {
    var sum = 0f
    val n = vertices.size
    for (i in 0 until n) {
        val a = vertices[i]
        val b = vertices[(i + 1) % n]
        sum += (a.x * b.y - b.x * a.y)
    }
    return 0.5f * sum
}

private fun <T> Array<T>.reversedArray(): Array<T> {
    val out = this.copyOf()
    out.reverse()
    return out
}
