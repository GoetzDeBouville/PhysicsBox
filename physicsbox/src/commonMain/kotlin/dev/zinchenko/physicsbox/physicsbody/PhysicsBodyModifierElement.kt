package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.ui.Modifier

internal data class PhysicsBodyModifierElement(
    val registration: PhysicsBodyRegistration,
) : Modifier.Element
