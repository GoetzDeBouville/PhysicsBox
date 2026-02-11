# Events

PhysicsBox exposes events at two levels:
- **Body events** via `Modifier.physicsBody(...)`
- **World step events** via `PhysicsBoxState.setOnStepListener(...)`

## Body events
```kotlin
.physicsBody(
    key = "box",
    onCollision = { event: CollisionEvent ->
        // collision between this body and event.otherKey
    },
    onDragStart = { event: DragEvent ->
        // drag started
    },
    onDragEnd = { event: DragEvent ->
        // drag ended (including cancel)
    },
)
```

### CollisionEvent
- `selfKey`, `otherKey`

- `impulse`: scalar magnitude (engine‑defined units)

- `normalX`, `normalY`: contact normal in world/container space

In the current backend the normal points from **self → other**.

### DragEvent
- `phase`: `Start`, `Move`, `End`, `Cancel`

- pointer/target positions in **pixels**

- velocity in **px/s**

## StepEvent
```kotlin
DisposableEffect(state) {
    state.setOnStepListener { event: StepEvent ->
        // per-step hook
    }
    onDispose { state.setOnStepListener(null) }
}
```

### Handling tips
!!! tip "Keep handlers lightweight"
    Events can be frequent. Avoid allocating large objects or triggering heavy recompositions in callbacks.

!!! tip "Rate-limit effects"
    For haptics/sound/particles, consider rate‑limiting collision events to avoid spamming when bodies stack.
