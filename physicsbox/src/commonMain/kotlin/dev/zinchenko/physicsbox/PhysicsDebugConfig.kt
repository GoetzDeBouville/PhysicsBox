package dev.zinchenko.physicsbox

import androidx.compose.runtime.Immutable

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
