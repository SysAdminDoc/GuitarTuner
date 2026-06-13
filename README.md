# GuitarTuner

![Version](https://img.shields.io/badge/version-v0.0.1-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-lightgrey)

GuitarTuner is an open-source Android app for tuning an acoustic guitar by sound. The app listens through the microphone, identifies the strummed open string, shows whether it is flat or sharp, and guides the player through small tuning adjustments.

## Current Direction

- Native Android app with Kotlin, Jetpack Compose, and Material 3.
- Offline-first design with no network permission.
- Microphone-only pitch detection using lifecycle-bound Android `AudioRecord` capture.
- Guided standard-tuning workflow for acoustic guitar.
- Automatic standard-string detection with confidence and signal feedback.
- Clear tune-up / tune-down guidance with optional peg-direction calibration.
- AMOLED dark theme by default with an accessible light theme.

## Build

Requirements:

- JDK 21.
- Android SDK platform 36.
- Gradle 9.5.1 wrapper.
- Android Gradle Plugin 9.2.1.
- Kotlin/Compose plugin 2.3.21.

PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat check assembleDebug assembleRelease
```

## Release Checklist

- Build from a clean tag named `v*`.
- Set signing secrets before pushing a release tag: `ANDROID_SIGNING_KEYSTORE_BASE64`, `GUITARTUNER_KEYSTORE_PASSWORD`, `GUITARTUNER_KEY_ALIAS`, and `GUITARTUNER_KEY_PASSWORD`.
- Run `.\gradlew.bat check assembleRelease bundleRelease --no-daemon --no-configuration-cache`.
- Push the tag to GitHub after a remote and signing secrets are configured; `.github/workflows/release.yml` uploads the APK/AAB plus `SHA256SUMS.txt` and publishes them to a GitHub Release.
- Do not publish artifacts from a dirty worktree or from an untagged local build.

## Signed Release Builds

Generate a release keystore once and keep it outside the repository. The `.gitignore` blocks common keystore extensions, but the keystore still belongs in a password manager or secrets vault.

```powershell
keytool -genkeypair -v `
  -keystore "$env:USERPROFILE\guitartuner-release.jks" `
  -alias guitartuner `
  -keyalg RSA `
  -keysize 4096 `
  -validity 10000
```

For a local signed build, export the signing environment variables before running Gradle:

```powershell
$env:GUITARTUNER_KEYSTORE_PATH = "$env:USERPROFILE\guitartuner-release.jks"
$env:GUITARTUNER_KEYSTORE_PASSWORD = "<keystore-password>"
$env:GUITARTUNER_KEY_ALIAS = "guitartuner"
$env:GUITARTUNER_KEY_PASSWORD = "<key-password>"
.\gradlew.bat check assembleRelease bundleRelease --no-daemon --no-configuration-cache
Get-FileHash app\build\outputs\apk\release\app-release.apk -Algorithm SHA256
```

For GitHub release builds, store the same passwords plus a base64-encoded keystore in repository secrets:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:USERPROFILE\guitartuner-release.jks")) | Set-Clipboard
```

Paste that value into `ANDROID_SIGNING_KEYSTORE_BASE64`. The release workflow decodes it on the runner, builds `assembleRelease` and `bundleRelease`, and uploads APK/AAB artifacts with `SHA256SUMS.txt`.

The release workflow is ready in this repository, but it only runs after the project is pushed to a GitHub remote and the four signing secrets exist. A `v*` tag push publishes or updates the matching GitHub Release; manual workflow runs build and upload artifacts without creating a release.

## Repository Status

GuitarTuner v0.0.1 has a native Android scaffold with an in-project YIN pitch detector, stable-frame smoothing, second-harmonic octave correction, an explicit `PitchResult` contract, Guided and Auto tuning modes, a low-E-to-high-E walkthrough, per-string peg-direction calibration, persisted A4 calibration, persisted cents tolerance, persisted noise gate control, persisted System/Dark/Light theme selection, built-in and custom tunings, a polished first-screen action flow, sectioned tuning controls, a first-screen local-audio privacy note, a confidence-aware main tuner readout, explicit signal and permission states, a standard six-string tuning model, Compose tuner screen, merged-manifest permission gate, JVM tests for standard-string, octave-error, and local WAV fixture regression coverage, and a physical-device debug capture smoke test on a Samsung SM-S938B.

## Screenshots

![Main tuner screen](docs/screenshots/tuner-main.png)

![Settings and tuning controls](docs/screenshots/tuner-settings.png)

## Privacy Baseline

The Android manifest declares `RECORD_AUDIO` only. The app has no network permission, no background microphone service, and no audio upload path.

## Custom Tuning JSON

The Import button accepts a JSON file shaped like this:

```json
{
  "schemaVersion": 1,
  "tunings": [
    {
      "id": "open_g",
      "name": "Open G",
      "strings": [
        { "stringNumber": 6, "name": "D", "note": "D2", "frequencyHz": 73.42 },
        { "stringNumber": 5, "name": "G", "note": "G2", "frequencyHz": 98.0 },
        { "stringNumber": 4, "name": "D", "note": "D3", "frequencyHz": 146.83 },
        { "stringNumber": 3, "name": "G", "note": "G3", "frequencyHz": 196.0 },
        { "stringNumber": 2, "name": "B", "note": "B3", "frequencyHz": 246.94 },
        { "stringNumber": 1, "name": "D", "note": "D4", "frequencyHz": 293.66 }
      ]
    }
  ]
}
```

## Play Data Safety Draft

- Data collected: none transmitted off device.
- Data shared: none.
- Audio: microphone samples are processed ephemerally on device for tuning, not stored, exported, or uploaded.
- App activity and settings: startup tuning preferences are stored locally only and are deleted when app data is cleared or the app is uninstalled.
- Network: the app declares no network permission.

## License

MIT. See [LICENSE](LICENSE).
