# Bodies

Bodies (childs) are registered via `Modifier.physicsBody(...)` as a first childs of the PhysicsBox. The modifier binds a Composable to a physics body and allows the runtime to drive its translation and rotation.

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
`PhysicsBodyConfig` defines material and motion parameters for a physics body:

- `bodyType`: Dynamic, Static, Kinematic

- `density`: body density (applied only to Dynamic bodies; must be >= 0)

- `friction`: surface friction coefficient (must be >= 0)

- `restitution`: bounciness coefficient (must be >= 0)

- `linearDamping`: linear velocity damping (must be >= 0)

- `angularDamping`: angular velocity damping (must be >= 0)

- `fixedRotation`: prevents rotation when true

- `allowSleep`: enables sleeping/auto-deactivation (default true)

- `isBullet`: enables continuous collision detection for fast-moving bodies

- `gravityScale`: gravity multiplier for this body

- `initialTransform`: initial position (px) and rotation (degrees)


Example:
```kotlin
modifier = Modifier.physicsBody(
    key = "floor",
    config = PhysicsBodyConfig(
        bodyType = BodyType.Static,
        density = 1f,
        friction = 0.6f,
        restitution = 0.5f,
        initialTransform = PhysicsTransform(
            vector2 = PhysicsVector2(160f, 360f)
        ), // ...
    )
)
```

## Transforms

`PhysicsTransform` uses **pixels** and **degrees**:

- `vector2`: position in container px

- `rotationDegrees`: rotation in degrees (clockwise in Y‑down screen space)

## Shapes
Collision shape (Physical shape) used by the engine.

## CollisionFilter
Collision filtering rules (category/mask/group or equivalent).

## Draggability
`isDraggable` - enables pointer dragging for this body.

## DragConfig
This configuration controls how a body follows the pointer (finger/mouse) during dragging and what happens when the drag ends (fling).

## CallBacks
- `onCollision()`

- `onSleepChanged()`

- `onDragStart()`

- `onDragEnd()`

## Common pitfalls
!!! tip "Avoiding body re-creation"
    PhysicsBox tracks bodies by key. Reusing the same key for a different Composable can cause callbacks
    to be delivered to the wrong element. Make keys stable and unique.

!!! warning "Config changes"
    Changing shape or size triggers fixture rebuilds. This is supported but can be expensive if done every frame.

## Note
PhysicsBox can also be a child of another PhysicsBox and contains its own rules and laws.