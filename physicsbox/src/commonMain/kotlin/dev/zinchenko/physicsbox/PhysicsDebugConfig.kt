package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Debug drawing switches for runtime visual diagnostics.
 *
 * This is a UI-facing configuration object used by `PhysicsBox` integrations to render overlays
 * (wireframes, bounds, contacts). The library core does not mandate how the debug rendering is done;
 * each runtime/backend can interpret these flags.
 *
 * @property enabled Master switch: when `false`, debug rendering should be disabled regardless of
 * other flags.
 * @property drawBodies Draw body shapes/fixtures (wireframe).
 * @property drawBounds Draw world/container boundaries used for collisions.
 * @property drawContacts Draw contact points/normals (if the backend exposes them).
 */
@Deprecated(
    message = "PhysicsBox debug is not implemented yet",
    level = DeprecationLevel.WARNING
)
@Immutable
data class PhysicsDebugConfig(
    val enabled: Boolean = false,
    val drawBodies: Boolean = false,
    val drawBounds: Boolean = false,
    val drawContacts: Boolean = false,
)
