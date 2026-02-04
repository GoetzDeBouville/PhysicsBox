package dev.zinchenko.physicsbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent
import dev.zinchenko.physicsbox.physicsbody.CollisionFilter
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.applyPhysicsBody

/**
 * Physics-aware Compose container contract.
 *
 * This API defines how content participates in a physics world, but does not implement world stepping,
 * solver execution, or rendering synchronization yet.
 *
 * Performance note:
 * keep high-frequency simulation data out of Compose state. The intended model is:
 * `PhysicsBoxState` controls world parameters, while body transforms are managed internally by runtime code.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun Scene() {
 *     val worldState = rememberPhysicsBoxState()
 *
 *     PhysicsBox(
 *         state = worldState,
 *         config = PhysicsBoxConfig(worldScale = PxPerMeter(90f)),
 *     ) {
 *         Box(
 *             Modifier
 *                 .physicsBody(key = "box")
 *         )
 *         Text(
 *             text = "Hi",
 *             modifier = Modifier.physicsBody(
 *                 key = "label",
 *                 config = PhysicsBodyConfig(bodyType = BodyType.Dynamic),
 *             ),
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun PhysicsBox(
    modifier: Modifier = Modifier,
    state: PhysicsBoxState = rememberPhysicsBoxState(),
    config: PhysicsBoxConfig = PhysicsBoxConfig(),
    debug: PhysicsDebugConfig = PhysicsDebugConfig(),
    content: @Composable PhysicsBoxScope.() -> Unit,
) {
    val scope: PhysicsBoxScope = remember { PhysicsBoxScopeImpl }

    CompositionLocalProvider(
        LocalPhysicsBoxModifier provides modifier,
        LocalPhysicsBoxState provides state,
        LocalPhysicsBoxConfig provides config,
        LocalPhysicsDebugConfig provides debug,
    ) {
        scope.content()
    }
}

/**
 * Receiver scope for [PhysicsBox] content.
 */
@Stable
interface PhysicsBoxScope {
    /**
     * Scope-local alias for [Modifier.physicsBody].
     */
    fun Modifier.physicsBody(
        key: Any,
        config: PhysicsBodyConfig = PhysicsBodyConfig(),
        shape: PhysicsShape = PhysicsShape.Box,
        filter: CollisionFilter = CollisionFilter.Default,
        isDraggable: Boolean = true,
        dragConfig: DragConfig = DragConfig(),
        onCollision: ((CollisionEvent) -> Unit)? = null,
        onSleepChanged: ((Boolean) -> Unit)? = null,
        onDragStart: ((DragEvent) -> Unit)? = null,
        onDragEnd: ((DragEvent) -> Unit)? = null,
    ): Modifier
}

private object PhysicsBoxScopeImpl : PhysicsBoxScope {
    override fun Modifier.physicsBody(
        key: Any,
        config: PhysicsBodyConfig,
        shape: PhysicsShape,
        filter: CollisionFilter,
        isDraggable: Boolean,
        dragConfig: DragConfig,
        onCollision: ((CollisionEvent) -> Unit)?,
        onSleepChanged: ((Boolean) -> Unit)?,
        onDragStart: ((DragEvent) -> Unit)?,
        onDragEnd: ((DragEvent) -> Unit)?,
    ): Modifier = applyPhysicsBody(
        modifier = this,
        key = key,
        config = config,
        shape = shape,
        filter = filter,
        isDraggable = isDraggable,
        dragConfig = dragConfig,
        onCollision = onCollision,
        onSleepChanged = onSleepChanged,
        onDragStart = onDragStart,
        onDragEnd = onDragEnd,
    )
}

internal val LocalPhysicsBoxModifier = staticCompositionLocalOf<Modifier> { Modifier }
internal val LocalPhysicsBoxState = staticCompositionLocalOf<PhysicsBoxState?> { null }
internal val LocalPhysicsBoxConfig = staticCompositionLocalOf { PhysicsBoxConfig() }
internal val LocalPhysicsDebugConfig = staticCompositionLocalOf { PhysicsDebugConfig() }
