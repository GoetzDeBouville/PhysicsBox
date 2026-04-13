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

!!! note "Boundary tunneling"
    - Bodies can tunnel through boundaries when they cross a wall between simulation steps or when
      the solver cannot resolve a deep overlap with a wall in time.
    - Tunneling is more likely with thin walls, high velocity/impulse, strong drag or fling, large
      gravity, low step frequency, low solver iterations, or bodies marked as non-bullet.
    - Tunneling can also happen when large bodies are spawned at `(0, 0)` or near/outside the
      container edge, because they can start intersecting the wall or already be partially beyond it.
    - Spawning many bodies in the same point can create large overlap-correction impulses that push
      bodies through boundaries.
    - Increase `BoundariesConfig.thicknessPx` for larger or faster bodies, and use
      `PhysicsBodyConfig.isBullet` for bodies that can move quickly.
    - Use `PhysicsTransform` to spawn bodies away from walls and avoid initial intersections with
      boundaries or other bodies. For multiple bodies, distribute their initial transforms instead of
      placing all of them at the same position.

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
