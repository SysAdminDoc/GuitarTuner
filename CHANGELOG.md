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
- Added localization-ready tuner strings with initial Spanish and German resources.
- Added env-driven release signing config and a tagged GitHub Actions release workflow with SHA-256 checksums.
- Added adaptive single-column and wide two-column tuner layouts with phone, split-screen, tablet, and foldable Compose previews.
- Added an optional freeze-last-note setting that keeps the last stable measurement visible after note decay.
- Added a JVM boundary test that keeps core pitch, tuning, and settings packages free of phone UI dependencies for later Wear reuse.
- Added an explicit high-noise audio state alongside silence, clipping, permission denial, and no-string feedback.
- Verified debug microphone capture startup on a connected Samsung SM-S938B with adb install, `RECORD_AUDIO` grant, foreground launch, active Pause/Stop UI, and an empty crash buffer.
- Added stable-frame smoothing after pluck attack to reduce jumpy cents/frequency feedback without smearing target-string changes.
- Added second-harmonic octave correction so weak-fundamental guitar strings map back to the intended open string instead of a nearby higher string.
- Added an explicit `PitchResult` contract exposing frequency, nearest note, cents offset, confidence, and signal state.
- Added Guided and Auto tuning modes, including a guided string selector that locks analysis to one target string and an auto path that keeps nearest-string detection.
- Added a persisted A4 calibration setting with a 440 Hz default and per-Hz stepper controls that scale built-in and custom tuning targets.
- Added confidence to the main tuner readout alongside note, string, frequency, and cents feedback.
- Added a step-by-step Guided walkthrough from low E to high E with Previous/Next controls and highlighted target strings.
- Added per-string peg-direction calibration so left/right turn guidance appears only after the selected string has a tune-up direction.
- Added explicit empty, permission-required, high-noise, clipped-input, and no-string-detected guidance states.
- Added built-in Half-step down, Drop D, Open G, and DADGAD tunings alongside Standard and existing custom tuning import/export.
- Added a persisted cents tolerance setting that updates the analyzer's in-tune window.
- Added a persisted noise gate setting that updates microphone sensitivity for the pitch detector.
- Added a persisted System/Dark/Light theme setting backed by the existing AMOLED dark and light color schemes.
- Added a first-screen privacy note that states microphone audio never leaves the device.
- Added a local standard-guitar WAV fixture set and golden target regression tests for pitch and tuning results.
- Added README screenshots captured from the Android app running on a physical Samsung SM-S938B.
