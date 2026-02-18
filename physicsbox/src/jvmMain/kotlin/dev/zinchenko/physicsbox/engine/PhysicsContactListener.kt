package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.units.PhysicsUnits
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.collision.WorldManifold
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.contacts.Contact
import kotlin.math.abs

internal class PhysicsContactListener(
    private val bodyByJBoxBody: (Body) -> Any?,
    private val units: PhysicsUnits,
    private val sink: PhysicsEventSink,
) : ContactListener {
    private val worldManifold = WorldManifold()

    override fun beginContact(contact: Contact) = Unit

    override fun endContact(contact: Contact) = Unit

    override fun preSolve(contact: Contact, oldManifold: Manifold) = Unit

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
        var normalImpulse = 0f
        val pointsCount = impulse.count.coerceAtMost(impulse.normalImpulses.size)
        for (i in 0 until pointsCount) {
            normalImpulse += abs(impulse.normalImpulses[i])
        }
        emitCollisionPair(contact = contact, impulsePhysics = normalImpulse)
    }

    private fun emitCollisionPair(contact: Contact, impulsePhysics: Float) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        val keyA = bodyByJBoxBody(bodyA) ?: return
        val keyB = bodyByJBoxBody(bodyB) ?: return

        contact.getWorldManifold(worldManifold)
        val normal = worldManifold.normal
        val impulsePx = units.impulsePhysicsToPx(impulsePhysics)

        sink.onCollision(
            CollisionEvent(
                selfKey = keyA,
                otherKey = keyB,
                impulse = impulsePx,
                normalX = normal.x,
                normalY = normal.y,
            ),
        )
        sink.onCollision(
            CollisionEvent(
                selfKey = keyB,
                otherKey = keyA,
                impulse = impulsePx,
                normalX = -normal.x,
                normalY = -normal.y,
            ),
        )
    }
}
