package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragEvent
import dev.zinchenko.physicsbox.physicsbody.CollisionFilter
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import org.jbox2d.dynamics.Body

internal data class BodyKey(val value: Any)

internal fun userDataToKey(userData: Any?): Any? = when (userData) {
    is BodyKey -> userData.value
    else -> userData
}

internal data class BodyHandle(
    val key: Any,
    val body: Body,
    var widthPx: Int,
    var heightPx: Int,
    var shape: PhysicsShape,
    var config: PhysicsBodyConfig,
    var filter: CollisionFilter,
    var onCollision: ((CollisionEvent) -> Unit)? = null,
    var onSleepChanged: ((Boolean) -> Unit)? = null,
    var onDragStart: ((DragEvent) -> Unit)? = null,
    var onDragEnd: ((DragEvent) -> Unit)? = null,
    var wasAwake: Boolean = true,
)
