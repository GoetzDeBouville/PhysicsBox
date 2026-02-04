package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

/**
 * Debug drawing switches for runtime visual diagnostics.
 *
 * This is API-only for now; render implementation is intentionally out of scope.
 */
@Immutable
data class PhysicsDebugConfig(
    val enabled: Boolean = false,
    val drawBodies: Boolean = false,
    val drawBounds: Boolean = false,
    val drawContacts: Boolean = false,
)
