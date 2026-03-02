package dev.zinchenko.physicsbox.layout

import androidx.compose.runtime.withFrameNanos
import dev.zinchenko.physicsbox.PhysicsBoxState
import dev.zinchenko.physicsbox.SolverIterations
import dev.zinchenko.physicsbox.StepConfig
import dev.zinchenko.physicsbox.engine.PhysicsWorldEngine
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * Frame-synchronized fixed-timestep simulation loop.
 *
 * The loop accumulates real frame time, runs up to `maxSubSteps` fixed updates per frame,
 * and emits one UI tick only when at least one physics step has been executed.
 */
internal class PhysicsSimulationLoop(
    private val engine: PhysicsWorldEngine,
    private val state: PhysicsBoxState,
    private val stepConfigProvider: () -> StepConfig,
    private val onFrameStepped: () -> Unit,
) {
    private var accumulatorSeconds: Float = 0f
    private var lastFrameNanos: Long = UNSET_FRAME_NANOS

    fun reset() {
        accumulatorSeconds = 0f
        lastFrameNanos = UNSET_FRAME_NANOS
    }

    @Suppress("D")
    suspend fun run() {
        while (currentCoroutineContext().isActive) {
            withFrameNanos { frameTimeNanos ->
                if (state.isPaused) {
                    reset()
                    return@withFrameNanos
                }

                if (lastFrameNanos == UNSET_FRAME_NANOS) {
                    lastFrameNanos = frameTimeNanos
                    return@withFrameNanos
                }

                val stepConfig = stepConfigProvider()
                val fixedDeltaSeconds = 1f / stepConfig.hz
                val solverIterations = SolverIterations(
                    velocity = stepConfig.velocityIterations,
                    position = stepConfig.positionIterations,
                )

                val frameDeltaNanos = (frameTimeNanos - lastFrameNanos).coerceAtLeast(0L)
                lastFrameNanos = frameTimeNanos

                val frameDeltaSeconds = frameDeltaNanos * NANOS_TO_SECONDS
                val clampedFrameDelta = frameDeltaSeconds.coerceIn(0f, stepConfig.maxDeltaSeconds)
                if (clampedFrameDelta <= 0f) return@withFrameNanos

                accumulatorSeconds += clampedFrameDelta

                var subSteps = 0
                while (
                    accumulatorSeconds + FIXED_STEP_EPSILON >= fixedDeltaSeconds &&
                    subSteps < stepConfig.maxSubSteps
                ) {
                    engine.step(
                        deltaSeconds = fixedDeltaSeconds,
                        stepConfig = stepConfig,
                        solverIterations = solverIterations,
                    )
                    accumulatorSeconds -= fixedDeltaSeconds
                    subSteps++
                }

                if (subSteps == stepConfig.maxSubSteps && accumulatorSeconds >= fixedDeltaSeconds) {
                    accumulatorSeconds = 0f
                }

                if (subSteps > 0) {
                    onFrameStepped()
                }
            }
        }
    }

    private companion object {
        private const val NANOS_TO_SECONDS: Float = 1f / 1_000_000_000f
        private const val FIXED_STEP_EPSILON: Float = 1e-6f
        private const val UNSET_FRAME_NANOS: Long = -1L
    }
}
