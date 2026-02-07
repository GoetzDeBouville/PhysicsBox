# Shapes

Shapes are provided by `PhysicsShape` and define collision geometry. The engine derives actual dimensions from the Composable's measured size unless specified.

## Box
`PhysicsShape.Box` uses the Composable bounds as a rectangle.

```kotlin
.physicsBody(key = "box", shape = PhysicsShape.Box)
```

## Circle
`PhysicsShape.Circle(radiusPx)` optionally specifies a radius in pixels. If `radiusPx` is null, the runtime derives it from the Composable size.

```kotlin
.physicsBody(
    key = "ball",
    shape = PhysicsShape.Circle(radiusPx = 40f)
)
```

## Polygon
`PhysicsShape.Polygon` uses a list of vertices in either normalized or pixel space.

```kotlin
val verts = listOf(
    PhysicsVector2(-0.5f, -0.4f),
    PhysicsVector2(0.5f, -0.4f),
    PhysicsVector2(0.2f, 0.5f),
    PhysicsVector2(-0.2f, 0.5f),
)

.physicsBody(
    key = "poly",
    shape = PhysicsShape.Polygon(
        vertices = verts,
        space = PhysicsShape.Polygon.VertexSpace.Normalized,
    )
)
```

### Constraints
- **Convex only** (no concave or self‑intersecting shapes).
- **3..8 vertices** recommended (backend limit is typically 8).
- Vertices should not be degenerate (zero area).
- Counter‑clockwise winding is recommended. The current backend auto‑corrects winding if needed.

## Common pitfalls
!!! warning "Polygon validity"
    If the polygon is invalid (concave, too many vertices, zero area), the fixture creation fails and
    the body will not collide as expected.
