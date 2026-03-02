package dev.zinchenko.physicsbox.engine

import dev.zinchenko.physicsbox.BoundariesConfig
import dev.zinchenko.physicsbox.PhysicsBoxConfig
import dev.zinchenko.physicsbox.PhysicsCommand
import dev.zinchenko.physicsbox.PhysicsWorldSnapshot
import dev.zinchenko.physicsbox.SolverIterations
import dev.zinchenko.physicsbox.StepConfig
import dev.zinchenko.physicsbox.physicsbody.CollisionFilter
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyRegistration
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.units.PhysicsUnits

internal expect class PhysicsWorldEngine(
    config: PhysicsBoxConfig,
    solverIterations: SolverIterations,
    boundariesConfig: BoundariesConfig,
    units: PhysicsUnits,
    eventSink: PhysicsEventSink,
) {
    fun setPaused(paused: Boolean)

    fun step(
        deltaSeconds: Float,
        stepConfig: StepConfig,
        solverIterations: SolverIterations,
    ): StepResult

    fun apply(command: PhysicsCommand)

    fun apply(commands: List<PhysicsCommand>)

    fun ensureBody(reg: PhysicsBodyRegistration, measuredWidthPx: Int, measuredHeightPx: Int)

    fun updateBodySize(key: Any, widthPx: Int, heightPx: Int)

    fun updateBodyConfig(
        key: Any,
        config: PhysicsBodyConfig,
        shape: PhysicsShape,
        filter: CollisionFilter,
    )

    fun removeBody(key: Any)

    fun updateBoundaries(containerWidthPx: Int, containerHeightPx: Int)

    fun snapshotPx(): PhysicsWorldSnapshot
}
