# Demos

This repository includes demo applications for Android and Desktop. 
Find it releases page:
`https://github.com/GoetzDeBouville/PhysicsBox/releases/tag/v1.0.0`

Or clone repository and build it on your local machine.

## Android demo
- Run in Android Studio using the imported run configuration.
- Or build an APK:

```bash
./gradlew :androidApp:assembleDebug
# APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

## Desktop demo
```bash
./gradlew :desktopApp:run
```

Hot reload:
```bash
./gradlew :desktopApp:hotRun --auto
```

## What the demos show
- Physics‑driven layout in Compose.
- Drag interactions.
- Basic boundaries and collisions.
