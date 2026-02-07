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
 * Physics-aware Compose container.
 *
 * `PhysicsBox` measures and places its children via Compose, registers each child as a physics body
 * (when the child uses `Modifier.physicsBody(...)`), runs the physics simulation, and then renders
 * the latest world snapshot by applying layer transforms (`translationX/Y`, `rotationZ`) during
 * placement rather than forcing remeasure/relayout.
 *
 * ### Mental model
 * - **Compose** owns measurement and composition.
 * - **Physics engine** owns motion and collisions.
 * - A child becomes “physical” only if it is registered with `Modifier.physicsBody(key = ...)`.
 *
 * ### State and commands
 * [state] is the single integration point with the simulation:
 * - receives global step callbacks (`dispatchStep`)
 * - provides pending commands drained and applied to the engine (e.g., reset, impulses, etc.)
 * - controls pause/resume and solver parameters
 *
 * ### Scaling and coordinates
 * The simulation runs in physics units (meters, seconds). Rendering and input happen in pixels.
 * The mapping is controlled by [config.worldScale] (e.g., `PxPerMeter`).
 *
 * ### Lifecycle
 * The physics engine instance is created with `remember(...)` and disposed with `DisposableEffect`.
 * On dispose, the world is reset and boundaries are cleared.
 *
 * @param modifier Modifier for the container itself.
 * @param state Holder of simulation state, step configuration and a command queue.
 * @param config World configuration (scale, boundaries, stepping, etc.). The effective step config is
 * merged with `state.stepConfig`.
 * @param content Children content placed inside the physics container; use [PhysicsBoxScope.physicsBody]
 * to register elements in the physics world.
 */
@Composable
fun PhysicsBox(
    modifier: Modifier = Modifier,
    state: PhysicsBoxState = rememberPhysicsBoxState(),
    config: PhysicsBoxConfig = PhysicsBoxConfig(),
    content: @Composable PhysicsBoxScope.() -> Unit,
) {
    PhysicsBoxImpl(
        modifier = modifier,
        state = state,
        config = config,
        debug = PhysicsDebugConfig(),
        content = content,
    )
}

@Deprecated(
    message = "PhysicsBox debug is not implemented yet. Parameter is reserved and currently ignored. Do not pass it.",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith("PhysicsBox(modifier = modifier, state = state, config = config, content = content)")
)
@Composable
fun PhysicsBox(
    modifier: Modifier = Modifier,
    state: PhysicsBoxState = rememberPhysicsBoxState(),
    config: PhysicsBoxConfig = PhysicsBoxConfig(),
    debug: PhysicsDebugConfig = PhysicsDebugConfig(),
    content: @Composable PhysicsBoxScope.() -> Unit,
) {
    PhysicsBoxImpl(
        modifier = modifier,
        state = state,
        config = config,
        debug = debug,
        content = content,
    )
}

@Composable
fun PhysicsBoxImpl(
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
 *
 * Use [physicsBody] to register a composable as a physics body. The body is identified by [key],
 * measured by Compose, and synchronized with the simulation. The engine then drives the visual
 * transform of the composable based on the current world snapshot.
 *
 * The extension is intentionally scope-local so you can call it as `Modifier.physicsBody(...)`
 * inside `PhysicsBox { ... }`.
 */
@Stable
interface PhysicsBoxScope {

    /**
     * Registers the composable as a physics body and enables optional pointer-driven dragging.
     *
     * ### Keys
     * [key] must be **stable** across recompositions (do not allocate new objects each frame) and
     * **unique** within the same `PhysicsBox` container. Events use keys to map back to composables.
     *
     * ### Dragging
     * If [isDraggable] is `true`, the engine may create a drag controller on pointer input.
     * Drag behavior is defined by [dragConfig]. Drag callbacks ([onDragStart], [onDragEnd]) are
     * delivered with coordinates in **container pixels** (see [DragEvent]).
     *
     * ### Collisions
     * If [onCollision] is provided, it will be invoked for contact events involving this body.
     * Collision payload is described by [CollisionEvent].
     *
     * ### Performance notes
     * Event callbacks can be frequent. Keep handlers lightweight and avoid allocating per-event.
     *
     * @param key Stable identifier used to bind the composable to a physics body and route events.
     * @param config Physical properties (type, density, restitution, friction, etc.).
     * @param shape Collision shape used by the engine.
     * @param filter Collision filtering rules (category/mask/group or equivalent).
     * @param isDraggable Enables pointer dragging for this body.
     * @param dragConfig Drag tuning parameters (max force, spring frequency, damping, fling limits).
     * @param onCollision Optional body-level collision callback.
     * @param onSleepChanged Optional callback invoked when the body enters/leaves “sleep” state
     * (engine-specific; typically means it stopped moving and is excluded from simulation work).
     * @param onDragStart Optional callback invoked when a drag starts for this body.
     * @param onDragEnd Optional callback invoked when a drag ends (including cancel).
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
