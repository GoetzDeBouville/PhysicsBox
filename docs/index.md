# PhysicsBox

PhysicsBox is a Compose Multiplatform physics layout container for Android and Desktop (JVM). It turns Composables into 2D physics bodies and renders their motion by applying translation and rotation during layout.

The runtime uses JBox2D on JVM platforms, providing stable rigid‑body simulation with fixed‑timestep stepping, boundaries, dragging, and collision callbacks.

## Quick links
- [Getting Started](getting-started.md)
- [Concepts](concepts.md)
- [Bodies](bodies.md)
- [Shapes](shapes.md)
- [Events](events.md)
- [API Reference](api.md)

## What you get
- `PhysicsBox` container that drives simulation and layout.
- `Modifier.physicsBody` to register Composables as physics bodies.
- Shapes: box, circle, polygon.
- Dragging with joint‑style or direct control.
- Collision, gravity simulation and step events.

## Minimal snippet
```kotlin
val state = rememberPhysicsBoxState()

PhysicsBox(modifier = Modifier.fillMaxSize(), state = state) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CutCornerShape(0.dp))
            .physicsBody(key = "default_shape")
            .background(Color.Red),
    )
}
```

## Demo videos

### Android
<video controls playsinline muted preload="metadata" style="max-width: 50%; height: auto;">
  <source src="demoVideo/android_demo.mp4" type="video/mp4">
  Your browser does not support HTML video. Download:
  <a href="demoVideo/android_demo.mp4">android_demo.mp4</a>
</video>

### Desktop (demo 0)
<video controls playsinline muted preload="metadata" style="max-width: 100%; height: auto;">
  <source src="demoVideo/desktop_demo0.mp4" type="video/mp4">
  Your browser does not support HTML video. Download:
  <a href="demoVideo/desktop_demo0.mp4">desktop_demo0.mp4</a>
</video>

### Desktop (demo 2)
<video controls playsinline muted preload="metadata" style="max-width: 100%; height: auto;">
  <source src="demoVideo/desktop_demo2.mp4" type="video/mp4">
  Your browser does not support HTML video. Download:
  <a href="demoVideo/desktop_demo2.mp4">desktop_demo2.mp4</a>
</video>

