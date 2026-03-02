# Shapes

Shapes are provided by `PhysicsShape` and define collision geometry. The engine derives actual dimensions from the Composable's measured size unless specified.

## Box
`PhysicsShape.Box` uses the Composable bounds as a rectangle.

```kotlin
Modifier.physicsBody(key = "box", shape = PhysicsShape.Box)
```

## Circle
`PhysicsShape.Circle(radiusPx)` optionally specifies a radius in pixels. If `radiusPx` is null, the runtime derives it from the Composable size.

```kotlin
Modifier.physicsBody(
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

Modifier.physicsBody(
    key = "poly",
    shape = PhysicsShape.Polygon(
        vertices = verts,
        space = PhysicsShape.Polygon.VertexSpace.Normalized,
    )
)
```

### Polygon utilities

#### regularPolygonNormalized(...)
`regularPolygonNormalized(...)` returns a regular convex `PhysicsShape.Polygon` in
`PhysicsShape.Polygon.VertexSpace.Normalized`.

In normalized space, `x` and `y` values in `-0.5..0.5` map to the Composable's width and height.
On non-square Composables, the polygon stretches with the measured bounds in the same way as the
physics fixture.

Vertices use screen-oriented coordinates. Because of that, `rotationDegrees` is clockwise. The default `-90f` places the first vertex at the top.

- `sides` must be in `3..8` (backend limit).
- `radius` must be finite and `> 0`.
- `rotationDegrees` must be finite.

```kotlin
val hex = regularPolygonNormalized(sides = 6)

Modifier.physicsBody(key = "hex", shape = hex)
```

```kotlin
val tri = regularPolygonNormalized(
    sides = 3,
    radius = 0.45f,
    rotationDegrees = 0f,
)

Modifier.physicsBody(key = "tri", shape = tri)
```

#### polygonComposeShape(...)
`polygonComposeShape(...)` converts a `PhysicsShape.Polygon` to a Compose `Shape`
(`GenericShape`) so the visual clip can match the collision geometry.

It supports both `PhysicsShape.Polygon.VertexSpace.Normalized` and
`PhysicsShape.Polygon.VertexSpace.Px`, using the same local-origin mapping as the physics fixture
adapter.

If the polygon has fewer than 3 vertices, or if the Composable size is zero, the resulting path is
empty and the shape is effectively a no-op.

```kotlin
val poly = regularPolygonNormalized(6)

Modifier
    .clip(polygonComposeShape(poly))
    .physicsBody(key = "poly", shape = poly)
```

```kotlin
val pxPoly = PhysicsShape.Polygon(
    vertices = listOf(
        PhysicsVector2(-40f, -30f),
        PhysicsVector2(40f, -30f),
        PhysicsVector2(20f, 50f),
        PhysicsVector2(-20f, 50f),
    ),
    space = PhysicsShape.Polygon.VertexSpace.Px,
)

Modifier
    .clip(polygonComposeShape(pxPoly))
    .physicsBody(key = "pxPoly", shape = pxPoly)
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
