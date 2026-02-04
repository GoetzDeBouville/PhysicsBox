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
 */
@Stable
class PhysicsBoxState internal constructor(
    private val initialIsPaused: Boolean,
    private val initialGravity: PhysicsVector2,
    private val initialStepConfig: StepConfig,
) {
    var isPaused: Boolean by mutableStateOf(initialIsPaused)
        private set

    var gravity: PhysicsVector2 by mutableStateOf(initialGravity)
        private set

    var stepConfig: StepConfig by mutableStateOf(initialStepConfig)
        private set

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
     * Monotonic marker that changes when command queue receives new commands.
     * Runtime implementation may use this as a cheap invalidation key.
     */
    val pendingCommandVersion: Long
        get() = commandVersion

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun updateGravity(gravity: PhysicsVector2) {
        this.gravity = gravity
    }

    fun updateGravity(x: Float, y: Float) {
        updateGravity(PhysicsVector2(x, y))
    }

    fun updateStepConfig(stepConfig: StepConfig) {
        this.stepConfig = stepConfig
    }

    fun setSolverIterations(velocityIterations: Int, positionIterations: Int) {
        updateStepConfig(
            stepConfig.copy(
                velocityIterations = velocityIterations,
                positionIterations = positionIterations,
            )
        )
    }

    /**
     * Resets user-controlled world settings and requests runtime world reset.
     */
    fun reset() {
        isPaused = initialIsPaused
        gravity = initialGravity
        stepConfig = initialStepConfig
        enqueueCommand(PhysicsCommand.ResetWorld)
    }

    /**
     * Queues a linear impulse for the body associated with [key].
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
                impulseX = impulseX,
                impulseY = impulseY,
                wake = wake,
            ),
        )
    }

    /**
     * Queues a linear velocity update for the body associated with [key].
     */
    fun enqueueVelocity(
        key: Any,
        velocityX: Float,
        velocityY: Float,
    ) {
        enqueueCommand(
            PhysicsCommand.EnqueueVelocity(
                key = key,
                velocityX = velocityX,
                velocityY = velocityY,
            ),
        )
    }

    fun snapshot(): PhysicsWorldSnapshot = PhysicsWorldSnapshot(
        isPaused = isPaused,
        gravity = gravity,
        stepConfig = stepConfig,
        solverIterations = solverIterations,
    )

    /**
     * Registers a world step callback.
     *
     * Runtime integration may invoke it after each simulated step.
     */
    fun setOnStepListener(listener: ((StepEvent) -> Unit)?) {
        onStepListener = listener
    }

    /**
     * Generic command extension point for advanced runtime integrations.
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
 * Per-body transforms should stay in the physics runtime and not be mirrored as Compose state to avoid
 * broad recompositions every simulation tick.
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
