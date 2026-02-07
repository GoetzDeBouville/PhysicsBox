# Debugging

`PhysicsDebugConfig` is a UI‑facing configuration object that allows runtime integrations to render debug overlays (wireframes, bounds, contacts).

```kotlin
PhysicsBox(
    modifier = Modifier.fillMaxSize(),
    state = state,
    debug = PhysicsDebugConfig(
        enabled = true,
        drawBodies = true,
        drawBounds = true,
        drawContacts = true,
    ),
) { /* content */ }
```

## What it does
The core library does not render debug visuals itself. The flags are consumed by runtime integrations (for example, a Compose layer that draws outlines or contact points).

## Visualization tips
- Show body outlines to validate shapes.
- Render boundary walls to verify thickness and position.
- Visualize contact normals when tuning restitution and friction.

## Common pitfalls
!!! note "No built‑in renderer"
    Passing `PhysicsDebugConfig` won’t show anything unless your runtime or app renders it.
