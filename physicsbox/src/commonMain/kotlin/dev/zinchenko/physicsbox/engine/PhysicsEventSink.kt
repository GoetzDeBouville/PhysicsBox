package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragEvent
import dev.zinchenko.physicsbox.events.StepEvent

internal interface PhysicsEventSink {
    fun onCollision(event: CollisionEvent)

    fun onStep(event: StepEvent)

    fun onDrag(event: DragEvent)
}
