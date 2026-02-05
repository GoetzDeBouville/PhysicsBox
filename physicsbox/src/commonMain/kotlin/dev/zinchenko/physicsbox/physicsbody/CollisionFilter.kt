package dev.zinchenko.physicsbox.physicsbody

import androidx.compose.runtime.Immutable

/**
 * Collision filtering parameters used during broad-phase and contact filtering.
 *
 * This DTO follows the common (Box2D-like) collision filtering model:
 * - [categoryBits] defines which *category* this body belongs to (bitmask).
 * - [maskBits] defines which categories this body can collide with (bitmask).
 * - [groupIndex] provides an explicit override: bodies in the same non-zero group can be forced
 *   to always collide or never collide, regardless of category/mask.
 *
 * Typical rules (engine-dependent but widely adopted):
 * - If `groupIndex != 0` for both bodies and the values are equal:
 *   - positive value => always collide
 *   - negative value => never collide
 * - Otherwise collision is allowed when:
 *   `(a.maskBits & b.categoryBits) != 0 && (b.maskBits & a.categoryBits) != 0`
 *
 * Note: although fields are `Int`, most physics backends treat masks as 16-bit values; only the
 * lower bits may be used.
 *
 * @property categoryBits Bitmask representing the category of this body (usually a single bit).
 * @property maskBits Bitmask representing categories this body is allowed to collide with.
 * @property groupIndex Optional override group. `0` means "no override".
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
