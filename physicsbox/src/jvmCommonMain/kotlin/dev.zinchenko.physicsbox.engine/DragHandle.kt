package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.events.DragConfig
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.joints.MouseJoint

internal data class DragHandle(
    val key: Any,
    val body: Body,
    val dragConfig: DragConfig,
    var lastTargetMeters: PhysicsVector2,
    val mouseJoint: MouseJoint?,
)

