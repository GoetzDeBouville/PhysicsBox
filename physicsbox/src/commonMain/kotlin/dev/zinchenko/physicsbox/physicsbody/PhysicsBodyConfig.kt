package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable

/**
 * Physical parameters for a composable body registered via [androidx.compose.ui.Modifier.physicsBody].
 *
 * This configuration describes how the underlying physics engine should model the body:
 * its mass-related properties, surface interaction, damping, sleeping behavior, and initial pose.
 *
 * ## Units and world scale
 * Most values here are **dimensionless coefficients** (e.g., [friction], [restitution]) and do not
 * directly depend on `PxPerMeter`. However, parameters that interact with motion (forces/velocities)
 * can *feel* different when `PxPerMeter` changes because the same `m/s` corresponds to a different
 * `px/s` on screen.
 *
 * PhysicsBox uses a pixels-to-meters conversion defined by `PhysicsBoxConfig.worldScale`:
 * - size in meters = size in pixels / `pxPerMeter`
 *
 * Keeping typical body sizes around `0.1..2.0 m` improves stability for stacking and collisions.
 *
 * ## Surface interaction
 * ### [friction]
 * Sliding friction coefficient used for contacts. Higher values reduce slipping when the body
 * touches other bodies or boundaries.
 * - `0.0` → frictionless sliding
 * - typical UI values: `0.2..0.8`
 *
 * ### [restitution]
 * Bounciness coefficient used for contacts.
 * - `0.0` → no bounce
 * - `1.0` → highly elastic bounce
 *
 * Values above `1.0` can inject energy and destabilize scenes; prefer `[0..1]` unless you
 * intentionally want exaggerated bounce.
 *
 * ## Mass and density
 * ### [density]
 * Density affects mass computation for dynamic bodies (mass is derived from density * area).
 *
 * Notes:
 * - Density is only relevant for [BodyType.Dynamic]. For [BodyType.Static] and
 *   [BodyType.Kinematic], engines typically treat mass as infinite / not simulated.
 * - `0.0` is allowed (massless-like), but may produce unintuitive results. Prefer a positive value
 *   for dynamic bodies.
 *
 * ## Damping
 * ### [linearDamping]
 * Per-step velocity decay that simulates air resistance. Helps bodies settle.
 * - `0.0` → no damping
 * - small values like `0.1..2.0` can reduce jitter in busy scenes
 *
 * ### [angularDamping]
 * Rotational velocity decay. Useful for preventing excessive spinning.
 *
 * Damping is solver-dependent; very large values can make motion feel sticky.
 *
 * ## Rotation and sleeping
 * ### [fixedRotation]
 * If `true`, the body will not rotate (angular velocity is constrained). This is useful for UI
 * elements like labels/buttons that should move but stay upright.
 *
 * ### [allowSleep]
 * If `true`, the engine may put the body to sleep when it is at rest, improving performance.
 * If you need continuous tiny motion/updates, set this to `false` (at higher CPU cost).
 *
 * ## Continuous collision detection
 * ### [isBullet]
 * If `true`, enables a higher-quality collision mode for fast-moving dynamic bodies to reduce
 * tunneling through thin objects/boundaries. This is more expensive.
 *
 * Consider enabling for small, fast bodies or when you observe bodies escaping the container.
 *
 * ## Gravity scaling
 * ### [gravityScale]
 * Multiplier applied to world gravity for this body.
 * - `1.0` → normal gravity
 * - `0.0` → no gravity (body floats)
 * - negative values invert gravity direction for that body
 *
 * This affects only gravitational acceleration, not other forces.
 *
 * ## Initial transform
 * ### [initialTransform]
 * Initial pose of the body in the PhysicsBox container.
 *
 * By convention in PhysicsBox:
 * - position is in **pixels** in the container coordinate space
 * - rotation is in **degrees** (screen/Compose convention: positive rotates clockwise in screen Y-down space)
 *
 * Engines typically store angles in radians internally; conversion is handled by the units layer.
 *
 * Note: if a body with the same key is already present, the engine may ignore the initial transform,
 * depending on implementation. Treat this as a “spawn pose”, not an always-enforced constraint.
 */
@Immutable
data class PhysicsBodyConfig(
    val bodyType: BodyType = BodyType.Dynamic,
    val density: Float = 1f,
    val friction: Float = 0.3f,
    val restitution: Float = 0.2f,
    val linearDamping: Float = 0f,
    val angularDamping: Float = 0f,
    val fixedRotation: Boolean = false,
    val allowSleep: Boolean = true,
    val isBullet: Boolean = false,
    val gravityScale: Float = 1f,
    val initialTransform: PhysicsTransform = PhysicsTransform(),
) {
    init {
        require(density >= 0f) { "PhysicsBodyConfig.density must be >= 0." }
        require(friction >= 0f) { "PhysicsBodyConfig.friction must be >= 0." }
        require(restitution >= 0f) { "PhysicsBodyConfig.restitution must be >= 0." }
        require(linearDamping >= 0f) { "PhysicsBodyConfig.linearDamping must be >= 0." }
        require(angularDamping >= 0f) { "PhysicsBodyConfig.angularDamping must be >= 0." }
    }
}
