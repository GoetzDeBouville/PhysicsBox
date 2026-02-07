# Recipe: Dragging bodies

PhysicsBox exposes pointer dragging via `Modifier.physicsBody(...)` using `DragConfig`.

## Joint‑style drag (default)
```kotlin
.physicsBody(
    key = "ball",
    dragConfig = DragConfig(
        useJointStyleDrag = true,
        maxForce = 1200f,
        frequencyHz = 6f,
        dampingRatio = 0.7f,
    )
)
```

Joint‑style drag uses a spring‑like constraint (similar to a MouseJoint). This feels stable in the presence of collisions.

## Direct drag
```kotlin
.physicsBody(
    key = "card",
    dragConfig = DragConfig(
        useJointStyleDrag = false,
        maxForce = 800f,
        frequencyHz = 5f,
        dampingRatio = 0.5f,
    )
)
```

Direct drag drives velocity toward the pointer without creating a joint. It can be useful for light UI elements.

## Tips
- Increase `maxForce` if dragging feels sluggish.
- Reduce `frequencyHz` or increase `dampingRatio` if drag feels jittery.
