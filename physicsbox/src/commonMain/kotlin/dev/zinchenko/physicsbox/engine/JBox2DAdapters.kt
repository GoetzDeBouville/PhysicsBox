package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.CollisionFilter
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.units.PhysicsUnits
import kotlin.math.min
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyType as JBoxBodyType
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef

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
        PhysicsShape.Box -> {
            val halfWidthMeters = units.pxToMeters(widthPx.toFloat()) * 0.5f
            val halfHeightMeters = units.pxToMeters(heightPx.toFloat()) * 0.5f
            if (halfWidthMeters <= 0f || halfHeightMeters <= 0f) return null
            PolygonShape().apply { setAsBox(halfWidthMeters, halfHeightMeters) }
        }

        is PhysicsShape.Circle -> {
            val radiusPx = shape.radiusPx ?: min(widthPx, heightPx) * 0.5f
            val radiusMeters = units.pxToMeters(radiusPx)
            if (radiusMeters <= 0f) return null
            CircleShape().apply { m_radius = radiusMeters }
        }
    }
    return fixtureDef
}
