# Recipe: Tuning bounciness and friction

`restitution` controls bounce; `friction` controls sliding. Both are available on `PhysicsBodyConfig` and `BoundariesConfig`.

## Body tuning
```kotlin
Modifier.physicsBody(
    key = "ball",
    config = PhysicsBodyConfig(
        restitution = 0.8f,
        friction = 0.2f,
    )
)
```

## Boundary tuning
```kotlin
PhysicsBox(
    config = PhysicsBoxConfig(
        boundaries = BoundariesConfig(
            restitution = 0.3f,
            friction = 0.4f,
        )
    )
) { /* ... */ }
```

## Tips
- Keep restitution in `[0..1]` for stable UI scenes.
- Increase friction to reduce sliding and stacking jitter.
