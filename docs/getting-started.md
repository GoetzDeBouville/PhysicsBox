# Getting Started

This guide shows the minimum setup required to render physics‑driven Composables with PhysicsBox.

## Installation

Add Maven Central:
```kotlin
repositories {
    mavenCentral()
}
```

KMP (recommended):
```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.zinchenko-dev:physicsbox:<last-version>")
            }
        }
    }
}
```

Android‑only:
```kotlin
dependencies {
    implementation("io.github.zinchenko-dev:physicsbox-android: <last-version>")
}
```

Desktop‑only:
```kotlin
dependencies {
    implementation("io.github.zinchenko-dev:physicsbox-desktop: <last-version>")
}
```

## Minimal example with two childs (dynamic and static) with minimum of parameters
```kotlin
@Composable
fun SimplePhysicsScene() {

    PhysicsBox(
        modifier = Modifier
            .fillMaxSize(),
        state = rememberPhysicsBoxState()
    ) {
        Box(
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .physicsBody(
                    key = "dynamic_circle",
                    shape = PhysicsShape.Circle(),
                    config = PhysicsBodyConfig(
                        initialTransform = PhysicsTransform(
                            vector2 = PhysicsVector2(x = 781f, y = 0f)
                        ),
                        restitution = 0.8f
                    ),
                )
                .background(Color.Red)
        )

        Box(
            Modifier
                .size(80.dp)
                .physicsBody(
                    key = "static_box",
                    config = PhysicsBodyConfig(
                        bodyType = BodyType.Static,
                        initialTransform = PhysicsTransform(
                            vector2 = PhysicsVector2(780f, 860f),
                            rotationDegrees = 45f
                        ),
                    ),
                )
                .background(Color.LightGray)
        )
    }
}
```

The default shape for any .physicsBody() is Square. As you can see from the example, the Compose shape should match the PhysicsShape (but it is not required). You also do not have to define the size for PhysicsShape: it takes the size from the composable (but you can override it if you need different sizes for the physical and composable shapes).
