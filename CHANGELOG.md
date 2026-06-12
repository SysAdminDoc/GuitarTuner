# Changelog

## GuitarTuner v0.0.1 - 2026-06-12

- Created local repository planning baseline.
- Added scaffold roadmap for an offline, open-source acoustic guitar tuner for Android.
- Captured initial product direction: microphone pitch detection, guided acoustic guitar tuning, privacy-first permissions, and configurable peg-direction guidance.
- Scaffolded the Android app with Gradle 9.5.1, AGP 9.2.1, Kotlin/Compose, Material 3, minSdk 26, release shrinking, and launcher placeholders.
- Added an in-project YIN pitch detector, standard six-string tuning model, lifecycle-bound `AudioRecord` capture controller, and Compose tuner screen.
- Added a merged-manifest gate that fails the build if any Android permission other than `RECORD_AUDIO` or any microphone foreground service is declared.
- Added JVM tests for standard strings, harmonic-rich low E, noisy low E, and G3/G4 octave-regression coverage.
- Added live tuning meter accessibility descriptions with stable cents buckets for TalkBack.
- Added persisted startup tuning behavior for standard, last-used, and favorite tuning modes.
- Added an in-app privacy screen and README Play Data Safety draft for local-only microphone processing.
- Added custom tuning JSON import/export with validation and persisted custom tuning selection.
