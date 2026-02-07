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

## Minimal example
```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.physicsbody.physicsBody
import dev.zinchenko.physicsbox.rememberPhysicsBoxState

@Composable
fun SimplePhysicsScene() {
    val state = rememberPhysicsBoxState()
    val density = LocalDensity.current

    val start = with(density) { PhysicsVector2(120.dp.toPx(), 40.dp.toPx()) }

    PhysicsBox(modifier = Modifier.fillMaxSize(), state = state) {
        Box(
            Modifier
                .size(80.dp)
                .background(Color.Red)
                .physicsBody(
                    key = "box",
                    config = PhysicsBodyConfig(
                        initialTransform = PhysicsTransform(vector2 = start),
                    ),
                )
        )
    }
}
```
