package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.BoundariesConfig
import dev.zinchenko.physicsbox.units.PhysicsUnits
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World

internal enum class BoundaryKey {
    Left,
    Top,
    Right,
    Bottom,
}

internal class BoundariesHandle {
    private var leftBody: Body? = null
    private var topBody: Body? = null
    private var rightBody: Body? = null
    private var bottomBody: Body? = null
    private var cachedState: CacheState? = null

    fun update(
        world: World,
        widthPx: Int,
        heightPx: Int,
        config: BoundariesConfig,
        units: PhysicsUnits,
    ) {
        if (world.isLocked) return

        val nextState = CacheState(widthPx, heightPx, config)
        if (cachedState == nextState && hasAllWalls()) return

        if (!config.enabled || widthPx <= 0 || heightPx <= 0) {
            destroy(world)
            cachedState = nextState
            return
        }

        destroy(world)

        val thicknessPx = config.thicknessPx
        val widthF = widthPx.toFloat()
        val heightF = heightPx.toFloat()
        val expandedWidthPx = widthF + thicknessPx * 2f
        val expandedHeightPx = heightF + thicknessPx * 2f

        leftBody = createWallBody(
            world = world,
            centerXPx = -thicknessPx * 0.5f,
            centerYPx = heightF * 0.5f,
            wallWidthPx = thicknessPx,
            wallHeightPx = expandedHeightPx,
            config = config,
            units = units,
            key = BoundaryKey.Left,
        )

        topBody = createWallBody(
            world = world,
            centerXPx = widthF * 0.5f,
            centerYPx = -thicknessPx * 0.5f,
            wallWidthPx = expandedWidthPx,
            wallHeightPx = thicknessPx,
            config = config,
            units = units,
            key = BoundaryKey.Top,
        )

        rightBody = createWallBody(
            world = world,
            centerXPx = widthF + thicknessPx * 0.5f,
            centerYPx = heightF * 0.5f,
            wallWidthPx = thicknessPx,
            wallHeightPx = expandedHeightPx,
            config = config,
            units = units,
            key = BoundaryKey.Right,
        )

        bottomBody = createWallBody(
            world = world,
            centerXPx = widthF * 0.5f,
            centerYPx = heightF + thicknessPx * 0.5f,
            wallWidthPx = expandedWidthPx,
            wallHeightPx = thicknessPx,
            config = config,
            units = units,
            key = BoundaryKey.Bottom,
        )

        cachedState = nextState
    }

    fun destroy(world: World) {
        if (world.isLocked) return
        destroyBody(world, leftBody)
        destroyBody(world, topBody)
        destroyBody(world, rightBody)
        destroyBody(world, bottomBody)
        leftBody = null
        topBody = null
        rightBody = null
        bottomBody = null
    }

    private fun hasAllWalls(): Boolean {
        return leftBody != null && topBody != null && rightBody != null && bottomBody != null
    }

    private fun createWallBody(
        world: World,
        centerXPx: Float,
        centerYPx: Float,
        wallWidthPx: Float,
        wallHeightPx: Float,
        config: BoundariesConfig,
        units: PhysicsUnits,
        key: BoundaryKey,
    ): Body? {
        if (wallWidthPx <= 0f || wallHeightPx <= 0f) return null

        val bodyDef = BodyDef().apply {
            type = BodyType.STATIC
            position.set(units.pxToMeters(centerXPx), units.pxToMeters(centerYPx))
            userData = BodyKey(key)
        }
        val body = world.createBody(bodyDef) ?: return null

        val halfWidthMeters = units.pxToMeters(wallWidthPx) * 0.5f
        val halfHeightMeters = units.pxToMeters(wallHeightPx) * 0.5f
        if (halfWidthMeters <= 0f || halfHeightMeters <= 0f) {
            world.destroyBody(body)
            return null
        }

        val shape = PolygonShape().apply { setAsBox(halfWidthMeters, halfHeightMeters) }
        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 0f
            friction = config.friction
            restitution = config.restitution
        }
        body.createFixture(fixtureDef)
        return body
    }

    private fun destroyBody(world: World, body: Body?) {
        if (body == null) return
        world.destroyBody(body)
    }

    private data class CacheState(
        val widthPx: Int,
        val heightPx: Int,
        val config: BoundariesConfig,
    )
}
