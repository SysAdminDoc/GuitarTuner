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

PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat check assembleDebug assembleRelease
```

## Repository Status

GuitarTuner v0.0.1 has a native Android scaffold with an in-project YIN pitch detector, standard six-string tuning model, Compose tuner screen, merged-manifest permission gate, and JVM tests for standard-string and octave-error coverage. Emulator visual QA is still pending because local `adb devices` did not return a usable target.

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
