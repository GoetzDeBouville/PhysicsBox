package dev.zinchenko.physicsbox.physicsbody

/**
 * Physics body type.
 *
 * Determines how the physics engine treats the body during simulation.
 *
 * ## Types
 * - [Static] — immovable body. Not affected by forces, gravity, impulses, or collisions in terms of
 *   movement. Other bodies collide with it. Use for boundaries, floors, obstacles.
 *
 * - [Dynamic] — fully simulated body. Affected by forces and gravity, collides, and responds to
 *   impulses. This is the default for typical “falling/stacking” UI elements.
 *
 * - [Kinematic] — body with infinite mass that is moved by setting its velocity (or transform) by
 *   the user/engine. It is not affected by forces/gravity, but it can push dynamic bodies.
 *   Use for moving platforms or scripted motion.
 *
 * ## Notes
 * - Only [Dynamic] bodies use [PhysicsBodyConfig.density] for mass computation.
 * - Drag interactions usually target [Dynamic] bodies; dragging [Static]/[Kinematic] is typically
 *   either ignored or treated as direct repositioning (engine-specific).
 */
enum class BodyType {
    Static,
    Dynamic,
    Kinematic,
}
