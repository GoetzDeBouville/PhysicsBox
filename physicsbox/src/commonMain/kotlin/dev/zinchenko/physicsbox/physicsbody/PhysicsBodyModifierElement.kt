package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Modifier

internal data class PhysicsBodyModifierElement(
    val registration: PhysicsBodyRegistration,
) : Modifier.Element, ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = registration
}
