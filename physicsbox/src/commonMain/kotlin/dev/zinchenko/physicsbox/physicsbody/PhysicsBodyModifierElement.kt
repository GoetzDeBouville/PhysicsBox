package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Density

internal data class PhysicsBodyModifierElement(
    val registration: PhysicsBodyRegistration,
) : ModifierNodeElement<PhysicsBodyParentDataNode>() {
    override fun create(): PhysicsBodyParentDataNode = PhysicsBodyParentDataNode(registration)

    override fun update(node: PhysicsBodyParentDataNode) {
        node.registration = registration
    }
}

internal class PhysicsBodyParentDataNode(
    var registration: PhysicsBodyRegistration,
) : Modifier.Node(), ParentDataModifierNode {
    override fun Density.modifyParentData(parentData: Any?): Any = registration
}
