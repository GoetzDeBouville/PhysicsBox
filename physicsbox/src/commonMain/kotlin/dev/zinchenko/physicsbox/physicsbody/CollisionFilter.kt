package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable

/**
 * Collision filtering data for broad-phase and contact filtering.
 */
@Immutable
data class CollisionFilter(
    val categoryBits: Int = 0x0001,
    val maskBits: Int = 0xFFFF,
    val groupIndex: Int = 0,
) {
    companion object {
        val Default: CollisionFilter = CollisionFilter()
    }
}
