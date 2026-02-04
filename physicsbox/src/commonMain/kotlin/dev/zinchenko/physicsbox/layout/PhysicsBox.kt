package dev.zinchenko.physicsbox.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.zinchenko.physicsbox.LocalPhysicsBoxState
import dev.zinchenko.physicsbox.PhysicsBoxConfig
import dev.zinchenko.physicsbox.PhysicsBoxState
import dev.zinchenko.physicsbox.PhysicsCommand
import dev.zinchenko.physicsbox.PhysicsDebugConfig
import dev.zinchenko.physicsbox.engine.PhysicsEventSink
import dev.zinchenko.physicsbox.engine.PhysicsWorldEngine
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragConfig
import dev.zinchenko.physicsbox.events.DragEvent
import dev.zinchenko.physicsbox.events.StepEvent
import dev.zinchenko.physicsbox.physicsbody.CollisionFilter
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.applyPhysicsBody
import dev.zinchenko.physicsbox.rememberPhysicsBoxState
import dev.zinchenko.physicsbox.units

/**
 * Physics-aware Compose container contract.
 *
 * Children are measured by Compose, registered in the physics engine with their measured size, and
 * then rendered using layer transforms from world snapshot (`translationX/Y`, `rotationZ`).
 *
 * Performance contract:
 * body transforms are applied in layer placement, not through relayout/remeasure.
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
    val frameTick = remember { mutableLongStateOf(0L) }
    val runtimeConfig = remember(config, state.stepConfig) {
        config.copy(step = state.stepConfig)
    }
    val solverIterations = state.solverIterations
    val units = remember(runtimeConfig.worldScale) { runtimeConfig.units() }

    val eventSink = remember(state) {
        object : PhysicsEventSink {
            override fun onCollision(event: CollisionEvent) = Unit

            override fun onStep(event: StepEvent) {
                state.dispatchStep(event)
            }

            override fun onDrag(event: DragEvent) = Unit
        }
    }

    val engine = remember(runtimeConfig, solverIterations, units, eventSink) {
        PhysicsWorldEngine(
            config = runtimeConfig,
            solverIterations = solverIterations,
            boundariesConfig = runtimeConfig.boundaries,
            units = units,
            eventSink = eventSink,
        )
    }
    val pendingCommandVersion = state.pendingCommandVersion
    val isPaused = state.isPaused
    SideEffect {
        engine.setPaused(isPaused)
        if (pendingCommandVersion >= 0L) {
            val commands = state.drainPendingCommands()
            if (commands.isNotEmpty()) {
                engine.apply(commands)
            }
        }
    }

    LaunchedEffect(engine, state) {
        val simulationLoop = PhysicsSimulationLoop(
            engine = engine,
            state = state,
            stepConfigProvider = { state.stepConfig },
            onFrameStepped = {
                frameTick.longValue = frameTick.longValue + 1L
            },
        )
        simulationLoop.run()
    }

    DisposableEffect(engine) {
        onDispose {
            engine.apply(PhysicsCommand.ResetWorld)
            engine.updateBoundaries(containerWidthPx = 0, containerHeightPx = 0)
        }
    }

    CompositionLocalProvider(
        LocalPhysicsBoxModifier provides modifier,
        LocalPhysicsBoxState provides state,
        LocalPhysicsBoxConfig provides runtimeConfig,
        LocalPhysicsDebugConfig provides debug,
    ) {
        PhysicsBoxLayout(
            modifier = modifier,
            engine = engine,
            frameTick = frameTick,
        ) {
            scope.content()
        }
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
internal val LocalPhysicsBoxConfig = staticCompositionLocalOf { PhysicsBoxConfig() }
internal val LocalPhysicsDebugConfig = staticCompositionLocalOf { PhysicsDebugConfig() }
