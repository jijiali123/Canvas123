# Canvas

Simple Android drawing app built with Jetpack Compose.

## Project structure

- `app/src/main/java/com/example/canvas/MainActivity.kt`: app entry point and drawing UI.
- `app/src/main/res`: Android resources (icons, strings, themes).
- `app/build.gradle.kts`: app module dependencies and Android settings.
- `gradle/libs.versions.toml`: centralized dependency versions and aliases.

## Requirements

- Windows PowerShell
- Android Studio (for SDK, platform tools, and emulator)
- Java 21 (Android Studio bundled runtime works)

## Terminal build and run (PowerShell)

From any folder:

```powershell
Set-Location C:/Users/Jawad/AndroidStudioProjects/Canvas
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
$env:Path = "$env:JAVA_HOME/bin;" + $env:Path
```

Check Gradle and tasks:

```powershell
./gradlew.bat -v
./gradlew.bat tasks --all
```

Build debug APK:

```powershell
./gradlew.bat assembleDebug
```

Install on connected emulator/device:

```powershell
./gradlew.bat installDebug
```

Run lint and tests:

```powershell
./gradlew.bat lint
./gradlew.bat testDebugUnitTest
```

Clean build:

```powershell
./gradlew.bat clean
```

## Where to make common changes

- Add drawing behavior, gestures, and UI actions in `MainActivity.kt`.
- Add new screens by creating composables under `app/src/main/java/com/example/canvas`.
- Add libraries in `gradle/libs.versions.toml`, then reference them in `app/build.gradle.kts`.
- Adjust app id, SDK levels, and build types in `app/build.gradle.kts`.

## Troubleshooting

- If you see `JAVA_HOME is not set`, export JAVA_HOME as shown above.
- If installs fail, verify emulator/device availability:

```powershell
adb devices
```
