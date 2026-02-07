# Bodies

Bodies are registered via `Modifier.physicsBody(...)`. The modifier binds a Composable to a physics body and allows the runtime to drive its translation and rotation.

## Basic usage
```kotlin
Box(
    Modifier
        .size(80.dp)
        .physicsBody(key = "box")
)
```

## Keys
- Keys must be **stable across recompositions** and **unique** in a `PhysicsBox`.
- Use immutable types (`String`, `Long`, enums) when possible.

## PhysicsBodyConfig
`PhysicsBodyConfig` defines material and motion parameters:
- `bodyType`: `Dynamic`, `Static`, `Kinematic`
- `density`, `friction`, `restitution`
- `linearDamping`, `angularDamping`
- `fixedRotation`, `allowSleep`, `isBullet`, `gravityScale`
- `initialTransform` (position in px, rotation in degrees)

Example:
```kotlin
.physicsBody(
    key = "floor",
    config = PhysicsBodyConfig(
        bodyType = BodyType.Static,
        friction = 0.6f,
        restitution = 0.1f,
        initialTransform = PhysicsTransform(
            vector2 = PhysicsVector2(160f, 360f)
        )
    )
)
```

## Transforms
`PhysicsTransform` uses **pixels** and **degrees**:
- `vector2`: position in container px
- `rotationDegrees`: rotation in degrees (clockwise in Y‑down screen space)

## Common pitfalls
!!! tip "Avoiding body re-creation"
    PhysicsBox tracks bodies by key. Reusing the same key for a different Composable can cause callbacks
    to be delivered to the wrong element. Make keys stable and unique.

!!! warning "Config changes"
    Changing shape or size triggers fixture rebuilds. This is supported but can be expensive if done every frame.
