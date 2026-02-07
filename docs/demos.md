# Demos

This repository includes demo applications for Android and Desktop.

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
