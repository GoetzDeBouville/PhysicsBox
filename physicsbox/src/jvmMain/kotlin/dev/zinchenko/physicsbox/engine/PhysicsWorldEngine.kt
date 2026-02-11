package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.BoundariesConfig
import dev.zinchenko.physicsbox.PhysicsBodySnapshot
import dev.zinchenko.physicsbox.PhysicsCommand
import dev.zinchenko.physicsbox.PhysicsDefaults
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.PhysicsWorldSnapshot
import dev.zinchenko.physicsbox.SolverIterations
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent
import dev.zinchenko.physicsbox.events.StepEvent
import dev.zinchenko.physicsbox.physicsbody.CollisionFilter
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyRegistration
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.units.PhysicsUnits
import kotlin.math.max
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.joints.MouseJoint
import org.jbox2d.dynamics.joints.MouseJointDef
import dev.zinchenko.physicsbox.PhysicsBoxConfig as PhysicsConfig

internal actual class PhysicsWorldEngine actual constructor(
    private val config: PhysicsConfig,
    private val solverIterations: SolverIterations,
    private val boundariesConfig: BoundariesConfig,
    private val units: PhysicsUnits,
    private val eventSink: PhysicsEventSink,
) {
    private val bodyHandlesByKey: MutableMap<Any, BodyHandle> = LinkedHashMap()
    private val activeDragsByKey: MutableMap<Any, DragHandle> = LinkedHashMap()
    private val boundariesHandle = BoundariesHandle()

    private var paused: Boolean = false
    private var accumulatorSeconds: Float = 0f
    private var stepIndex: Long = 0L
    private var containerWidthPx: Int = 0
    private var containerHeightPx: Int = 0

    private val forwardingSink = object : PhysicsEventSink {
        override fun onCollision(event: dev.zinchenko.physicsbox.events.CollisionEvent) {
            eventSink.onCollision(event)
            bodyHandlesByKey[event.selfKey]?.onCollision?.invoke(event)
        }

        override fun onStep(event: StepEvent) {
            eventSink.onStep(event)
        }

        override fun onDrag(event: DragEvent) {
            eventSink.onDrag(event)
        }
    }

    private val world: World = World(
        Vec2(
            PhysicsDefaults.Gravity.x,
            PhysicsDefaults.Gravity.y,
        ),
    ).also { physicsWorld ->
        physicsWorld.setContactListener(
            PhysicsContactListener(
                bodyByJBoxBody = { body -> userDataToKey(body.userData) },
                units = units,
                sink = forwardingSink,
            ),
        )
    }
    private val dragGroundBody: Body = world.createBody(BodyDef()) ?: error("Failed to create drag ground body.")

    private val fixedStepSeconds: Float = 1f / config.step.hz

    actual fun setPaused(paused: Boolean) {
        this.paused = paused
    }

    actual fun step(deltaSeconds: Float): StepResult {
        val clampedDelta = clampDelta(deltaSeconds)
        if (paused || clampedDelta <= 0f) {
            forwardingSink.onStep(
                StepEvent(
                    deltaSeconds = clampedDelta,
                    subSteps = 0,
                    stepIndex = stepIndex,
                ),
            )
            return StepResult(
                stepped = false,
                bodiesCount = bodyHandlesByKey.size,
                contactsCount = world.contactCount,
            )
        }

        accumulatorSeconds += clampedDelta
        var subSteps = 0
        while (accumulatorSeconds + ENGINE_EPS >= fixedStepSeconds && subSteps < config.step.maxSubSteps) {
            world.step(fixedStepSeconds, solverIterations.velocity, solverIterations.position)
            accumulatorSeconds -= fixedStepSeconds
            subSteps++
            stepIndex++
            dispatchSleepStateChanges()
        }

        if (subSteps == config.step.maxSubSteps && accumulatorSeconds > fixedStepSeconds) {
            accumulatorSeconds = fixedStepSeconds
        }

        forwardingSink.onStep(
            StepEvent(
                deltaSeconds = clampedDelta,
                subSteps = subSteps,
                stepIndex = stepIndex,
            ),
        )

        return StepResult(
            stepped = subSteps > 0,
            bodiesCount = bodyHandlesByKey.size,
            contactsCount = world.contactCount,
        )
    }

    actual fun apply(command: PhysicsCommand) {
        when (command) {
            is PhysicsCommand.EnqueueImpulse -> applyImpulse(command)
            is PhysicsCommand.EnqueueVelocity -> applyVelocity(command)
            is PhysicsCommand.BeginDrag -> beginDrag(command)
            is PhysicsCommand.UpdateDrag -> updateDrag(command)
            is PhysicsCommand.EndDrag -> endDrag(command)
            is PhysicsCommand.CancelDrag -> cancelDrag(command)
            is PhysicsCommand.SetWorldGravity -> setWorldGravity(command.gravity)
            PhysicsCommand.ResetWorld -> resetWorld()
        }
    }

    actual fun apply(commands: List<PhysicsCommand>) {
        for (command in commands) {
            apply(command)
        }
    }

    actual fun ensureBody(reg: PhysicsBodyRegistration, measuredWidthPx: Int, measuredHeightPx: Int) {
        if (measuredWidthPx <= 0 || measuredHeightPx <= 0 || world.isLocked) return

        val existing = bodyHandlesByKey[reg.key]
        if (existing == null) {
            val created = createBodyHandle(
                reg = reg,
                widthPx = measuredWidthPx,
                heightPx = measuredHeightPx,
            ) ?: return
            bodyHandlesByKey[reg.key] = created
            return
        }

        val shouldRebuildFixture = existing.widthPx != measuredWidthPx ||
                existing.heightPx != measuredHeightPx ||
                existing.shape != reg.shape ||
                existing.config != reg.config ||
                existing.filter != reg.filter

        existing.widthPx = measuredWidthPx
        existing.heightPx = measuredHeightPx
        existing.shape = reg.shape
        existing.config = reg.config
        existing.filter = reg.filter
        existing.onCollision = reg.onCollision
        existing.onSleepChanged = reg.onSleepChanged
        existing.onDragStart = reg.onDragStart
        existing.onDragEnd = reg.onDragEnd

        applyConfigToBody(existing.body, reg.config)
        existing.body.userData = BodyKey(reg.key)

        if (shouldRebuildFixture) {
            recreateFixture(existing)
        }
    }

    actual fun updateBodySize(key: Any, widthPx: Int, heightPx: Int) {
        if (widthPx <= 0 || heightPx <= 0 || world.isLocked) return

        val handle = bodyHandlesByKey[key] ?: return
        if (handle.widthPx == widthPx && handle.heightPx == heightPx) return

        handle.widthPx = widthPx
        handle.heightPx = heightPx
        recreateFixture(handle)
    }

    actual fun updateBodyConfig(
        key: Any,
        config: PhysicsBodyConfig,
        shape: PhysicsShape,
        filter: CollisionFilter
    ) {
        if (world.isLocked) return

        val handle = bodyHandlesByKey[key] ?: return

        val shouldRebuildFixture = handle.shape != shape ||
                handle.config != config ||
                handle.filter != filter

        handle.config = config
        handle.shape = shape
        handle.filter = filter

        applyConfigToBody(handle.body, config)
        if (shouldRebuildFixture) {
            recreateFixture(handle)
        }
    }

    actual fun removeBody(key: Any) {
        if (world.isLocked) return

        destroyDragHandle(key)
        val removed = bodyHandlesByKey.remove(key) ?: return
        world.destroyBody(removed.body)
    }

    actual fun updateBoundaries(containerWidthPx: Int, containerHeightPx: Int) {
        if (containerWidthPx <= 0 || containerHeightPx <= 0) {
            this.containerWidthPx = containerWidthPx
            this.containerHeightPx = containerHeightPx
            boundariesHandle.destroy(world)
            return
        }
        this.containerWidthPx = containerWidthPx
        this.containerHeightPx = containerHeightPx

        boundariesHandle.update(
            world = world,
            widthPx = containerWidthPx,
            heightPx = containerHeightPx,
            config = boundariesConfig,
            units = units,
        )
    }

    actual fun snapshotPx(): PhysicsWorldSnapshot {
        val bodySnapshots = ArrayList<PhysicsBodySnapshot>(bodyHandlesByKey.size)
        for (handle in bodyHandlesByKey.values) {
            val positionMeters = handle.body.position
            val velocityMetersPerSecond = handle.body.linearVelocity
            bodySnapshots.add(
                PhysicsBodySnapshot(
                    key = handle.key,
                    transformPx = PhysicsTransform(
                        vector2 = PhysicsVector2(
                            x = units.metersToPx(positionMeters.x),
                            y = units.metersToPx(positionMeters.y),
                        ),
                        rotationDegrees = units.radiansToDegrees(handle.body.angle),
                    ),
                    isAwake = handle.body.isAwake,
                    linearVelocityPxPerSecond = PhysicsVector2(
                        x = units.velocityMetersToPxPerSecond(velocityMetersPerSecond.x),
                        y = units.velocityMetersToPxPerSecond(velocityMetersPerSecond.y),
                    ),
                ),
            )
        }

        val gravityMeters = world.gravity
        return PhysicsWorldSnapshot(
            isPaused = paused,
            gravity = PhysicsVector2(gravityMeters.x, gravityMeters.y),
            stepConfig = config.step,
            solverIterations = solverIterations,
            bodies = bodySnapshots,
            stepIndex = stepIndex,
        )
    }

    private fun applyImpulse(command: PhysicsCommand.EnqueueImpulse) {
        val handle = bodyHandlesByKey[command.key] ?: return
        val impulseMeters = units.impulseVecPxToPhysics(
            PhysicsVector2(
                x = command.impulseX,
                y = command.impulseY,
            ),
        )

        val wasAwake = handle.body.isAwake
        if (command.wake) {
            handle.body.isAwake = true
        } else if (wasAwake.not()) {
            return
        }

        handle.body.applyLinearImpulse(
            Vec2(impulseMeters.x, impulseMeters.y),
            handle.body.worldCenter,
        )
    }

    private fun applyVelocity(command: PhysicsCommand.EnqueueVelocity) {
        val handle = bodyHandlesByKey[command.key] ?: return
        val velocityMetersPerSecond = units.velocityVecPxToMetersPerSecond(
            PhysicsVector2(
                x = command.velocityX,
                y = command.velocityY,
            ),
        )
        handle.body.setLinearVelocity(Vec2(velocityMetersPerSecond.x, velocityMetersPerSecond.y))
    }

    private fun beginDrag(command: PhysicsCommand.BeginDrag) {
        if (world.isLocked) return

        val bodyHandle = bodyHandlesByKey[command.key] ?: return
        destroyDragHandle(command.key)

        val targetMeters = units.pxVecToMeters(command.targetPx)
        val dragHandle = if (command.dragConfig.useJointStyleDrag) {
            val joint = createMouseJoint(
                body = bodyHandle.body,
                targetMeters = targetMeters,
                dragConfig = command.dragConfig,
            ) ?: return
            DragHandle(
                key = command.key,
                body = bodyHandle.body,
                dragConfig = command.dragConfig,
                lastTargetMeters = targetMeters,
                mouseJoint = joint,
            )
        } else {
            applyDirectDragTarget(
                body = bodyHandle.body,
                targetMeters = targetMeters,
                dragConfig = command.dragConfig,
            )
            DragHandle(
                key = command.key,
                body = bodyHandle.body,
                dragConfig = command.dragConfig,
                lastTargetMeters = targetMeters,
                mouseJoint = null,
            )
        }

        bodyHandle.body.isAwake = true
        activeDragsByKey[command.key] = dragHandle
    }

    private fun updateDrag(command: PhysicsCommand.UpdateDrag) {
        val dragHandle = activeDragsByKey[command.key] ?: return
        val targetMeters = units.pxVecToMeters(command.targetPx)
        dragHandle.lastTargetMeters = targetMeters

        val mouseJoint = dragHandle.mouseJoint
        if (mouseJoint != null) {
            mouseJoint.setTarget(Vec2(targetMeters.x, targetMeters.y))
        } else {
            applyDirectDragTarget(
                body = dragHandle.body,
                targetMeters = targetMeters,
                dragConfig = dragHandle.dragConfig,
            )
        }
        dragHandle.body.isAwake = true
    }

    private fun endDrag(command: PhysicsCommand.EndDrag) {
        val dragHandle = activeDragsByKey.remove(command.key) ?: return
        destroyMouseJoint(dragHandle.mouseJoint)

        val velocityMetersPerSecond = units.velocityVecPxToMetersPerSecond(command.velocityPxPerSec)
        dragHandle.body.setLinearVelocity(Vec2(velocityMetersPerSecond.x, velocityMetersPerSecond.y))
        dragHandle.body.isAwake = true
    }

    private fun cancelDrag(command: PhysicsCommand.CancelDrag) {
        destroyDragHandle(command.key)
    }

    private fun setWorldGravity(gravity: PhysicsVector2) {
        if (world.isLocked) return
        world.gravity = Vec2(gravity.x, gravity.y)
    }

    private fun createMouseJoint(
        body: Body,
        targetMeters: PhysicsVector2,
        dragConfig: DragConfig,
    ): MouseJoint? {
        if (world.isLocked) return null
        val mouseJointDef = MouseJointDef().apply {
            bodyA = dragGroundBody
            bodyB = body
            target.set(targetMeters.x, targetMeters.y)
            maxForce = dragConfig.maxForce * max(body.mass, MIN_DRAG_MASS)
            frequencyHz = dragConfig.frequencyHz
            dampingRatio = dragConfig.dampingRatio
        }
        return world.createJoint(mouseJointDef) as? MouseJoint
    }

    private fun applyDirectDragTarget(
        body: Body,
        targetMeters: PhysicsVector2,
        dragConfig: DragConfig,
    ) {
        val current = body.position
        val stiffness = dragConfig.frequencyHz.coerceAtLeast(1f)
        val velocityX = (targetMeters.x - current.x) * stiffness
        val velocityY = (targetMeters.y - current.y) * stiffness
        body.setLinearVelocity(Vec2(velocityX, velocityY))
    }

    private fun destroyDragHandle(key: Any) {
        val handle = activeDragsByKey.remove(key) ?: return
        destroyMouseJoint(handle.mouseJoint)
    }

    private fun destroyAllDragHandles() {
        if (activeDragsByKey.isEmpty()) return
        val handles = activeDragsByKey.values.toList()
        activeDragsByKey.clear()
        for (handle in handles) {
            destroyMouseJoint(handle.mouseJoint)
        }
    }

    private fun destroyMouseJoint(mouseJoint: MouseJoint?) {
        if (mouseJoint == null || world.isLocked) return
        world.destroyJoint(mouseJoint)
    }

    private fun resetWorld() {
        if (world.isLocked) return

        destroyAllDragHandles()
        for (handle in bodyHandlesByKey.values) {
            world.destroyBody(handle.body)
        }
        bodyHandlesByKey.clear()
        boundariesHandle.destroy(world)

        accumulatorSeconds = 0f
        stepIndex = 0L

        if (containerWidthPx > 0 && containerHeightPx > 0) {
            updateBoundaries(containerWidthPx, containerHeightPx)
        }
    }

    private fun createBodyHandle(
        reg: PhysicsBodyRegistration,
        widthPx: Int,
        heightPx: Int,
    ): BodyHandle? {
        val bodyDef = createBodyDef(reg.key, reg.config)
        val body = world.createBody(bodyDef) ?: return null
        val fixtureDef = createFixtureDef(
            shape = reg.shape,
            widthPx = widthPx,
            heightPx = heightPx,
            config = reg.config,
            filter = reg.filter,
            units = units,
        ) ?: run {
            world.destroyBody(body)
            return null
        }

        body.createFixture(fixtureDef) ?: run {
            world.destroyBody(body)
            return null
        }

        return BodyHandle(
            key = reg.key,
            body = body,
            widthPx = widthPx,
            heightPx = heightPx,
            shape = reg.shape,
            config = reg.config,
            filter = reg.filter,
            onCollision = reg.onCollision,
            onSleepChanged = reg.onSleepChanged,
            onDragStart = reg.onDragStart,
            onDragEnd = reg.onDragEnd,
            wasAwake = body.isAwake,
        )
    }

    private fun createBodyDef(key: Any, config: PhysicsBodyConfig): BodyDef = BodyDef().apply {
        type = config.bodyType.toJBoxBodyType()
        userData = BodyKey(key)
        position.set(
            units.pxToMeters(config.initialTransform.vector2.x),
            units.pxToMeters(config.initialTransform.vector2.y),
        )
        angle = units.degreesToRadians(config.initialTransform.rotationDegrees)
        linearDamping = config.linearDamping
        angularDamping = config.angularDamping
        gravityScale = config.gravityScale
        fixedRotation = config.fixedRotation
        bullet = config.isBullet
        allowSleep = config.allowSleep
    }

    private fun applyConfigToBody(body: Body, config: PhysicsBodyConfig) {
        body.setType(config.bodyType.toJBoxBodyType())
        body.setBullet(config.isBullet)
        body.setFixedRotation(config.fixedRotation)
        body.setSleepingAllowed(config.allowSleep)
        body.setGravityScale(config.gravityScale)
        body.setLinearDamping(config.linearDamping)
        body.setAngularDamping(config.angularDamping)
    }

    private fun recreateFixture(handle: BodyHandle) {
        if (world.isLocked) return

        destroyAllFixtures(handle.body)
        val fixtureDef = createFixtureDef(
            shape = handle.shape,
            widthPx = handle.widthPx,
            heightPx = handle.heightPx,
            config = handle.config,
            filter = handle.filter,
            units = units,
        ) ?: return
        handle.body.createFixture(fixtureDef)
    }

    private fun destroyAllFixtures(body: Body) {
        var fixture = body.fixtureList
        while (fixture != null) {
            val next = fixture.next
            body.destroyFixture(fixture)
            fixture = next
        }
    }

    private fun dispatchSleepStateChanges() {
        for (handle in bodyHandlesByKey.values) {
            val isAwake = handle.body.isAwake
            if (isAwake == handle.wasAwake) continue
            handle.wasAwake = isAwake
            handle.onSleepChanged?.invoke(!isAwake)
        }
    }

    private fun clampDelta(deltaSeconds: Float): Float {
        if (deltaSeconds.isFinite().not() || deltaSeconds <= 0f) return 0f
        return if (deltaSeconds > config.step.maxDeltaSeconds) {
            config.step.maxDeltaSeconds
        } else {
            deltaSeconds
        }
    }

    private companion object {
        private const val ENGINE_EPS: Float = 1e-6f
        private const val MIN_DRAG_MASS: Float = 1f
    }
}
