package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable
import dev.zinchenko.physicsbox.units.PhysicsUnits

/**
 * Global configuration for a [dev.zinchenko.physicsbox.layout.PhysicsBox] world.
 *
 * This config defines how UI space (pixels) maps to physics space (meters) and how the world
 * is stepped over time.
 *
 * ## World scale (`worldScale`) and how it affects physics
 * [worldScale] is the **pixels-per-meter** ratio (`px / m`). It controls conversion between
 * the UI coordinate system (px) and the physics world (m).
 *
 * - `meters = pixels / worldScale`
 * - `pixels = meters * worldScale`
 *
 * This has several practical consequences:
 *
 * ### 1) Gravity and acceleration
 * Gravity is defined in **meters per second squared** (`m/s²`) in the physics world.
 * When you think in UI pixels, the perceived acceleration in `px/s²` becomes:
 *
 * `gravityPx = gravityMps2 * worldScale`
 *
 * So with the same gravity in `m/s²`, increasing [worldScale] makes gravity *look stronger*
 * in pixels (objects fall faster across the screen).
 *
 * ### 2) Velocities and impulses
 * - Velocity in physics is `m/s`. In UI terms:
 *   `velocityPxPerSec = velocityMps * worldScale`
 * - If you submit velocities/targets in pixels (e.g., drag target), they are converted to meters.
 *
 * Larger [worldScale] means the same physical velocity corresponds to more pixels per second.
 *
 * ### 3) Collision tolerances / stability (important)
 * Box2D-style solvers are most stable when typical object sizes are in a “reasonable” meter range.
 * If your UI objects are small but [worldScale] is huge, their physics sizes (in meters) become
 * extremely tiny, which can make the simulation feel jittery or unstable (penetration, stacking issues).
 *
 * Rule of thumb for UI scenes:
 * - Choose [worldScale] so that common object sizes end up around **0.1–2.0 meters** in physics.
 *   Example: a 100 px object:
 *   - with `PxPerMeter(100f)` → 1.0 m
 *   - with `PxPerMeter(200f)` → 0.5 m
 *
 * ### 4) Perceived “bounciness” and friction
 * Coefficients like restitution and friction are dimensionless and do not directly depend on scale.
 * However, because scale affects *speeds and effective sizes* in meters, you may observe that
 * tuning restitution/friction “feels” different when you change [worldScale] significantly.
 *
 * @property boundaries world edge settings. When enabled, bodies collide with container bounds.
 * @property worldScale conversion ratio from UI pixels to physics meters (`px / meter`).
 * @property step fixed-step simulation settings.
 */
@Immutable
data class PhysicsBoxConfig(
    val boundaries: BoundariesConfig = BoundariesConfig(),
    val worldScale: PxPerMeter = PhysicsDefaults.WorldScale,
    val step: StepConfig = StepConfig(),
)

/**
 * Creates a unit-conversion helper bound to [PhysicsBoxConfig.worldScale].
 *
 * This allocates a new [PhysicsUnits] instance on each call. Runtime code should cache it
 * per world/state if used in a hot loop.
 */
fun PhysicsBoxConfig.units(): PhysicsUnits = PhysicsUnits(worldScale)
