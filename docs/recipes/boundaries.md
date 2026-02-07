# Recipe: Boundaries

Use `BoundariesConfig` to keep bodies inside the container and tune how they bounce against edges.

```kotlin
PhysicsBox(
    config = PhysicsBoxConfig(
        boundaries = BoundariesConfig(
            enabled = true,
            thicknessPx = 64f,
            restitution = 0.2f,
            friction = 0.3f,
        )
    )
) { /* ... */ }
```

## Disabling boundaries
```kotlin
PhysicsBox(
    config = PhysicsBoxConfig(
        boundaries = BoundariesConfig(enabled = false)
    )
) { /* ... */ }
```

## Tips
- Increase `thicknessPx` if fast bodies tunnel through edges.
- Boundaries are rebuilt when container size changes.
