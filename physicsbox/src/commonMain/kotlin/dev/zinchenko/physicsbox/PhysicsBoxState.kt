package dev.zinchenko.physicsbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zinchenko.physicsbox.events.CollisionEvent
import dev.zinchenko.physicsbox.events.DragEvent
import dev.zinchenko.physicsbox.events.DragPhase
import dev.zinchenko.physicsbox.events.StepEvent
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyCallbacks

/**
 * Mutable controller for a physics world hosted by [dev.zinchenko.physicsbox.layout.PhysicsBox].
 *
 * This state is intended to be the primary API surface for:
 * - pausing/resuming simulation ([pause], [resume], [isPaused])
 * - updating world gravity ([setWorldGravity], [gravity])
 * - configuring stepping/solver parameters ([stepConfig], [solverIterations])
 * - queuing one-off actions for the runtime ([enqueueCommand] and helpers)
 *
 * The state keeps a queue of [PhysicsCommand] objects. The runtime drains it and applies changes
 * to the underlying physics engine. UI code should treat command helpers as *fire-and-forget*.
 *
 * Notes:
 * - This class is [Stable] to avoid unnecessary recompositions when only internal bookkeeping changes.
 * - Values such as [gravity] and [stepConfig] are exposed as Compose state (mutable) and will trigger
 *   recomposition when changed. Per-body transforms are intentionally not mirrored here.
 */
@Stable
class PhysicsBoxState internal constructor(
    private val initialIsPaused: Boolean,
    private val initialGravity: PhysicsVector2,
    private val initialStepConfig: StepConfig,
) {
    /**
     * Whether the simulation is paused.
     *
     * When paused, the runtime should stop advancing the simulation clock. Rendering may still
     * happen using the last known world snapshot.
     */
    var isPaused: Boolean by mutableStateOf(initialIsPaused)
        private set

    /**
     * Current world gravity vector.
     *
     * Units are interpreted by the runtime (typically physics units, e.g. m/s²).
     * Use [setWorldGravity] to also enqueue the update for the simulation backend.
     */
    var gravity: PhysicsVector2 by mutableStateOf(initialGravity)
        private set

    /**
     * Current step configuration (timestep/iterations/etc.).
     *
     * This value is used by the runtime while stepping the world. Updating it affects subsequent
     * simulation steps.
     */
    var stepConfig: StepConfig by mutableStateOf(initialStepConfig)
        private set

    /**
     * Convenience view over solver iteration counts derived from [stepConfig].
     *
     * Runtimes commonly expose two solver iteration counts:
     * - velocity iterations (constraint impulse solver)
     * - position iterations (penetration correction)
     */
    val solverIterations: SolverIterations
        get() = SolverIterations(
            velocity = stepConfig.velocityIterations,
            position = stepConfig.positionIterations,
        )

    private val pendingCommands: ArrayDeque<PhysicsCommand> = ArrayDeque()
    private val callbacksByKey: MutableMap<Any, PhysicsBodyCallbacks> = LinkedHashMap()

    private var commandVersion: Long by mutableLongStateOf(0L)
    private var onStepListener: ((StepEvent) -> Unit)? = null

    /**
     * Monotonic marker that changes when the command queue receives new commands.
     *
     * Runtime implementations may use this as a cheap invalidation key to detect whether there is
     * work to consume from the UI side.
     */
    val pendingCommandVersion: Long
        get() = commandVersion

    /** Pauses simulation stepping. */
    fun pause() {
        isPaused = true
    }

    /** Resumes simulation stepping. */
    fun resume() {
        isPaused = false
    }

    /**
     * Updates gravity and enqueues the change for the runtime.
     *
     * Equivalent to calling [setWorldGravity].
     */
    fun updateGravity(gravity: PhysicsVector2) {
        setWorldGravity(gravity)
    }

    /**
     * Updates gravity and enqueues the change for the runtime.
     *
     * Equivalent to calling `setWorldGravity(PhysicsVector2(x, y))`.
     */
    fun updateGravity(x: Float, y: Float) {
        setWorldGravity(PhysicsVector2(x, y))
    }

    /**
     * Sets world gravity and enqueues [PhysicsCommand.SetWorldGravity].
     *
     * Use this method when you need gravity to affect the runtime simulation immediately.
     */
    fun setWorldGravity(gravity: PhysicsVector2) {
        this.gravity = gravity
        enqueueCommand(PhysicsCommand.SetWorldGravity(gravity))
    }

    /**
     * Replaces current [stepConfig].
     *
     * This updates Compose state only; runtimes should read [stepConfig] from [snapshot] or
     * directly from state during stepping.
     */
    fun updateStepConfig(stepConfig: StepConfig) {
        this.stepConfig = stepConfig
    }

    /**
     * Convenience helper for updating solver iteration counts.
     *
     * @param velocityIterations Velocity solver iterations.
     * @param positionIterations Position solver iterations.
     */
    fun setSolverIterations(velocityIterations: Int, positionIterations: Int) {
        updateStepConfig(
            stepConfig.copy(
                velocityIterations = velocityIterations,
                positionIterations = positionIterations,
            )
        )
    }

    /**
     * Resets world-level settings and requests runtime world reset.
     *
     * Resets:
     * - [isPaused] to initial value
     * - [stepConfig] to initial value
     * - [gravity] to initial value (also enqueued to runtime)
     *
     * Also enqueues [PhysicsCommand.ResetWorld] to request a full backend reset.
     */
    fun reset() {
        isPaused = initialIsPaused
        stepConfig = initialStepConfig
        enqueueCommand(PhysicsCommand.ResetWorld)
        setWorldGravity(initialGravity)
    }

    /**
     * Queues a linear impulse for the body associated with [key].
     *
     * Impulse components are expressed in container pixels (Px) and converted internally
     * to physics impulse units by the runtime [dev.zinchenko.physicsbox.units.PhysicsUnits].
     *
     * @param key Body key used in `Modifier.physicsBody(key = ...)`.
     * @param impulseX Impulse X component in container pixels (Px).
     * @param impulseY Impulse Y component in container pixels (Px).
     * @param wake Whether to wake the body if the backend supports sleeping.
     */
    fun enqueueImpulse(
        key: Any,
        impulseX: Float,
        impulseY: Float,
        wake: Boolean = true,
    ) {
        enqueueCommand(
            PhysicsCommand.EnqueueImpulse(
                key = key,
                impulseXPx = impulseX,
                impulseYPx = impulseY,
                wake = wake,
            ),
        )
    }

    /**
     * Queues a linear velocity update for the body associated with [key].
     *
     * Velocity components are expressed in container pixels per second (Px/s) and converted
     * internally to meters per second by the runtime
     * [dev.zinchenko.physicsbox.units.PhysicsUnits].
     *
     * @param key Body key used in `Modifier.physicsBody(key = ...)`.
     * @param velocityX Velocity X component in container pixels per second (Px/s).
     * @param velocityY Velocity Y component in container pixels per second (Px/s).
     */
    fun enqueueVelocity(
        key: Any,
        velocityX: Float,
        velocityY: Float,
    ) {
        enqueueCommand(
            PhysicsCommand.EnqueueVelocity(
                key = key,
                velocityXPxPerSec = velocityX,
                velocityYPxPerSec = velocityY,
            ),
        )
    }

    /**
     * Creates an immutable snapshot of the current world-level configuration.
     *
     * This is typically used by runtime/layout integration as a single read of all relevant values.
     */
    fun snapshot(): PhysicsWorldSnapshot = PhysicsWorldSnapshot(
        isPaused = isPaused,
        gravity = gravity,
        stepConfig = stepConfig,
        solverIterations = solverIterations,
    )

    /**
     * Registers a world step callback.
     *
     * The runtime may invoke it after each simulated step (see [StepEvent]).
     */
    fun setOnStepListener(listener: ((StepEvent) -> Unit)?) {
        onStepListener = listener
    }

    /**
     * Generic command extension point for advanced integrations.
     *
     * Adds [command] to an internal queue. The runtime drains this queue (see `drainPendingCommands`)
     * and applies commands to the physics backend.
     */
    fun enqueueCommand(command: PhysicsCommand) {
        pendingCommands.addLast(command)
        commandVersion++
    }

    internal fun drainPendingCommands(): List<PhysicsCommand> {
        if (pendingCommands.isEmpty()) return emptyList()
        val drained = pendingCommands.toList()
        pendingCommands.clear()
        return drained
    }

    internal fun dispatchStep(event: StepEvent) {
        onStepListener?.invoke(event)
    }

    /**
     * Registers callbacks for a body [key].
     *
     * Keys must match those used in `Modifier.physicsBody(key = ...)`.
     * Duplicate keys are treated as last-writer-wins to keep runtime resilient.
     */
    internal fun registerBodyCallbacks(key: Any, callbacks: PhysicsBodyCallbacks) {
        callbacksByKey[key] = callbacks
    }

    internal fun unregisterBodyCallbacks(key: Any) {
        callbacksByKey.remove(key)
    }

    internal fun dispatchCollisionToBody(event: CollisionEvent) {
        callbacksByKey[event.selfKey]?.onCollision?.invoke(event)
    }

    internal fun dispatchDragToBody(event: DragEvent) {
        val callbacks = callbacksByKey[event.key] ?: return
        when (event.phase) {
            DragPhase.Start -> callbacks.onDragStart?.invoke(event)
            DragPhase.Move -> Unit
            DragPhase.End, DragPhase.Cancel -> callbacks.onDragEnd?.invoke(event)
        }
    }

    internal fun dispatchSleepToBody(key: Any, isSleeping: Boolean) {
        callbacksByKey[key]?.onSleepChanged?.invoke(isSleeping)
    }
}

/**
 * Remembers a [PhysicsBoxState] instance for controlling world-level behavior.
 *
 * This state intentionally exposes only coarse-grained mutable values (pause, gravity, step config).
 * Per-body transforms should remain in the runtime snapshot to avoid broad recompositions on every
 * simulation tick.
 *
 * @param initialIsPaused Initial pause state.
 * @param initialGravity Initial world gravity vector.
 * @param initialStepConfig Initial step configuration.
 */
@Composable
fun rememberPhysicsBoxState(
    initialIsPaused: Boolean = false,
    initialGravity: PhysicsVector2 = PhysicsDefaults.Gravity,
    initialStepConfig: StepConfig = StepConfig(),
): PhysicsBoxState = remember(initialIsPaused, initialGravity, initialStepConfig) {
    PhysicsBoxState(
        initialIsPaused = initialIsPaused,
        initialGravity = initialGravity,
        initialStepConfig = initialStepConfig,
    )
}
