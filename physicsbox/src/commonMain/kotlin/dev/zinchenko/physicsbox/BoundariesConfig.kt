package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Optional static boundaries around the [dev.zinchenko.physicsbox.layout.PhysicsBox] container.
 *
 * When enabled, the engine creates four **static** “walls” (left / top / right / bottom) that
 * surround the container. These walls prevent dynamic bodies from leaving the visible area and
 * allow you to tune how objects bounce and slide when they hit the edges.
 *
 * ## Coordinate space and sizing
 * - The container uses Compose coordinates: **X to the right**, **Y down**.
 * - Boundary geometry is generated from the **current container size** (in px) as observed by
 *   `PhysicsBoxLayout`.
 * - [thicknessPx] defines the wall thickness in **pixels**. A thicker wall reduces the chance that
 *   fast bodies “tunnel” through edges (though true CCD depends on body settings like `isBullet`).
 *
 * ## Contact behavior
 * - [restitution] controls how “bouncy” the container edges are:
 *   - `0.0` → no bounce (perfectly inelastic)
 *   - `1.0` → highly elastic bounce
 *   Values above `1.0` are allowed by the underlying physics engines in many cases, but can inject
 *   energy and destabilize simulation; keep it in `[0..1]` for typical UI use.
 * - [friction] controls sliding against the walls:
 *   - `0.0` → no friction (objects slide freely)
 *   - higher values → stronger resistance to sliding
 *
 * ## Lifecycle
 * - Walls are (re)generated whenever the container size changes.
 * - Disabling boundaries removes the walls from the physics world.
 *
 * ## Recommended starting values
 * - `restitution = 0.2f` and `friction = 0.3f` give a subtle bounce with controlled sliding.
 * - `thicknessPx = 32f..96f` is usually enough for UI scenes depending on element sizes and speeds.
 *
 * @property enabled Whether the boundary walls should exist around the container.
 * @property restitution Bounciness of the walls. Must be `>= 0`.
 * @property friction Sliding friction against the walls. Must be `>= 0`.
 * @property thicknessPx Wall thickness in **pixels**. Must be `> 0`.
 */
@Immutable
data class BoundariesConfig(
    val enabled: Boolean = true,
    val restitution: Float = 0.2f,
    val friction: Float = 0.3f,
    val thicknessPx: Float = 64f,
) {
    init {
        require(restitution >= 0f) { "Boundaries restitution must be >= 0." }
        require(friction >= 0f) { "Boundaries friction must be >= 0." }
        require(thicknessPx > 0f) { "Boundaries thicknessPx must be > 0." }
    }
}
