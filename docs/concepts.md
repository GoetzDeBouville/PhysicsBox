# Concepts

PhysicsBox is built around a small set of concepts: a simulation container, a state controller, fixed‑step stepping, and a consistent coordinate system.

## PhysicsBox container
`PhysicsBox` is the Compose container that:
- registers physics bodies declared by `Modifier.physicsBody`
- steps the simulation using a fixed timestep
- renders the current physics snapshot by applying translation/rotation during layout

## PhysicsBoxState
`PhysicsBoxState` is the mutable controller for the world. It exposes:
- pause/resume (`pause()`, `resume()`, `isPaused`)
- gravity (`setWorldGravity(...)`, `gravity`, `updateGravity(...)`)
- step configuration (`stepConfig`, `updateStepConfig(...)`)
- command helpers (`enqueueImpulse`, `enqueueVelocity`)
- step callback (`setOnStepListener`)
- apply an impulse or set velocity for bodies by their keys (`enqueueImpulse(...)`, `enqueueVelocity(...)`).
- etc. (see API)

## Fixed‑step stepping
PhysicsBox uses a fixed timestep to keep simulation stable across frame rates. See `StepConfig`:

- `hz`: target simulation frequency (e.g., 60)

- `maxSubSteps`: how many fixed steps are allowed per rendered frame

- `maxDeltaSeconds`: clamp for large frame deltas

- solver iterations (`velocityIterations`, `positionIterations`)

## World gravity
Gravity is configured in **physics units** (m/s²). The default is:
```kotlin
PhysicsDefaults.Gravity // (0f, 9.8f)
```
Update it using `PhysicsBoxState.updateGravity(...)`.

## Boundaries
`BoundariesConfig` creates static walls around the container. This keeps bodies inside the visible area and lets you control restitution/friction at the edges.

## Units and coordinate system
- Screen‑oriented axes: **+X right**, **+Y down**

- UI positions are in **pixels**

- Physics positions are in **meters**

- `PxPerMeter` controls the mapping:

  - `meters = pixels / pxPerMeter`

  - `pixels = meters * pxPerMeter`

`PhysicsBoxConfig.worldScale` defines the `PxPerMeter` ratio used by the runtime.

## Common pitfalls
!!! warning "Common pitfalls"
    - Very large `PxPerMeter` makes bodies tiny in meters, which can destabilize the solver.
    - Extremely high `hz` or `maxSubSteps` can spike CPU usage.
    - Do not create new `key` objects every recomposition; keys must be stable.
