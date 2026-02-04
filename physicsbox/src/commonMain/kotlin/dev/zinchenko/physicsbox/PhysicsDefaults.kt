package dev.zinchenko.physicsbox

/**
 * Shared default values used by API contracts.
 */
object PhysicsDefaults {
    /**
     * Default world length scale: `100 px = 1 meter`.
     *
     * This keeps typical UI object sizes in a numerically stable range for
     * 2D rigid-body solvers (roughly fractions of a meter to single-digit meters).
     */
    val WorldScale: PxPerMeter = PxPerMeter(100f)

    /**
     * Default gravity vector in meter units (`m/s^2`) with screen-oriented axes:
     * `+X` right, `+Y` downward.
     */
    val Gravity: PhysicsVector2 = PhysicsVector2(0f, 9.8f)
}
