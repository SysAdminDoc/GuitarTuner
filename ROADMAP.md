# GuitarTuner Roadmap

Project version: GuitarTuner v0.0.1

This roadmap contains incomplete work only. GuitarTuner is an offline, open-source Android acoustic guitar tuner. The first implementation target is standard six-string acoustic guitar tuning by microphone.

## Product Constraints

- Android-first, native Kotlin and Jetpack Compose.
- Minimum SDK 26.
- No network permission.
- Microphone permission only when the tuner is active.
- AMOLED dark theme by default, plus light theme when practical.
- Beginner guidance must use tune-up / tune-down as the source of truth; left/right peg guidance must be configurable because headstock layout and string winding direction vary.

## Target Standard Tuning

| String | Note | Frequency |
|---|---:|---:|
| 6 | E2 | 82.41 Hz |
| 5 | A2 | 110.00 Hz |
| 4 | D3 | 146.83 Hz |
| 3 | G3 | 196.00 Hz |
| 2 | B3 | 246.94 Hz |
| 1 | E4 | 329.63 Hz |

## Scaffold Milestones

### Phase 1 - Android Project Skeleton

- [ ] Generate Gradle Android application project.
- [ ] Configure Kotlin, Compose, Material 3, and minSdk 26.
- [ ] Add release build settings with R8 and resource shrinking.
- [ ] Add deterministic package name and app version constants.
- [ ] Add launcher icon placeholders.
- [ ] Verify clean debug and release builds.

### Phase 2 - Audio Capture Foundation

- [ ] Add `RECORD_AUDIO` permission and runtime permission flow.
- [ ] Implement an `AudioRecord` capture controller that starts only while tuning.
- [ ] Add microphone state, permission denial, device-wide mic-toggle/silent-input, clipping, and high-noise states.
- [ ] Keep audio processing off the UI thread.
- [ ] Verify capture on an emulator-safe path and at least one physical Android device.

### Phase 3 - Pitch Detection Engine

- [ ] Evaluate TarsosDSP versus a small in-project YIN or McLeod Pitch Method implementation.
- [ ] Implement pitch frame processing with confidence scoring.
- [ ] Add smoothing across stable frames after pluck attack.
- [ ] Handle low-E octave errors and weak fundamentals.
- [ ] Expose pitch result as frequency, nearest note, cents offset, confidence, and signal state.

### Phase 4 - Guitar Tuning Model

- [ ] Add standard tuning data model.
- [ ] Add nearest-string detection for open guitar strings.
- [ ] Add guided mode that locks onto one target string at a time.
- [ ] Add auto mode that selects the most likely strummed string.
- [ ] Add flat / sharp / in-tune thresholds.
- [ ] Add A4 calibration setting with 440 Hz default.

### Phase 5 - Guided Tuning UI

- [ ] Build main tuner screen with large note, string, cents, and confidence readout.
- [ ] Build needle or strobe-style visual meter.
- [ ] Add tune-up / tune-down instruction states.
- [ ] Add step-by-step acoustic guitar walkthrough from low E to high E.
- [ ] Add peg-direction calibration for left/right guidance.
- [ ] Add empty, permission-denied, high-noise, clipped-input, and no-string-detected states.

### Phase 6 - Settings and Tunings

- [ ] Add standard, half-step down, drop D, open G, DADGAD, and custom tuning support.
- [ ] Add cents tolerance setting.
- [ ] Add sensitivity / noise gate setting.
- [ ] Add theme setting.
- [ ] Add privacy note explaining that audio never leaves the device.

### Phase 7 - Verification and Release Prep

- [ ] Add pitch-engine validation with synthetic sine, harmonic-rich, and noisy samples.
- [ ] Add a small local recorded-guitar fixture set.
- [ ] Compare tuning results against a known-good tuner.
- [ ] Capture screenshots for README after UI exists.
- [ ] Add signed release build documentation.
- [ ] Prepare GitHub release workflow after a remote exists.

## Research-Driven Additions

- [ ] P0 — Lock the DSP dependency and license strategy before Gradle scaffold
  Why: TarsosDSP is GPL and its v2.5 Android microphone support is disputed, which conflicts with the repo's MIT baseline unless intentionally changed.
  Evidence: RESEARCH.md; JorenSix/TarsosDSP README; JorenSix/TarsosDSP#213; LICENSE.
  Touches: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `LICENSE`, `README.md`.
  Acceptance: The scaffold either contains an in-project MIT-compatible YIN/MPM pitch engine or explicitly changes project licensing before any GPL dependency is added.
  Complexity: M

- [ ] P0 — Add a merged-manifest permission gate
  Why: The repo promises no network permission and microphone-only access, but no build check can enforce that yet.
  Evidence: RESEARCH.md; README.md; ROADMAP.md; Android sensitive-permission docs.
  Touches: `app/src/main/AndroidManifest.xml`, Gradle verification task, CI workflow.
  Acceptance: A build task fails if the merged manifest contains `INTERNET`, background microphone service declarations, or any dangerous permission other than `RECORD_AUDIO`.
  Complexity: S

- [ ] P0 — Keep microphone capture lifecycle-bound to visible tuning screens
  Why: Android microphone foreground services are while-in-use restricted and background capture is unnecessary for this product.
  Evidence: RESEARCH.md; Android foreground-service microphone docs; Android privacy indicator docs.
  Touches: `audio/AudioCaptureController`, `ui/tuner`, `AndroidManifest.xml`.
  Acceptance: Leaving the tuner screen or backgrounding the app stops capture, releases `AudioRecord`, and updates UI state without a background microphone notification.
  Complexity: M

- [ ] P1 — Split pitch, tuning, audio, settings, and UI boundaries at scaffold time
  Why: Competitors with Wear/custom tunings share business logic across modules; this repo has no source yet, so boundaries are cheapest before UI code exists.
  Evidence: RESEARCH.md; Choona PR #61/#68/#70; current repo has no source tree.
  Touches: `app/src/main/java/.../audio`, `pitch`, `tuning`, `settings`, `ui`.
  Acceptance: Pitch and tuning logic run in JVM unit tests without Android UI or microphone dependencies.
  Complexity: M

- [ ] P1 — Add octave-error regression fixtures for low E and G3/G4 jumps
  Why: Guitar tuners commonly misread weak fundamentals and octave harmonics; thetwom/Tuner has a live G3/G4 jump report.
  Evidence: RESEARCH.md; thetwom/Tuner#88; 29a.ch low-E FFT analysis; YIN and MPM papers.
  Touches: `pitch`, `testFixtures/audio`, JVM tests.
  Acceptance: Tests cover E2, A2, D3/G3/B3/E4, harmonic-rich low E, and a G3/G4 octave-jump fixture with stable target-string output.
  Complexity: M

- [ ] P1 — Add accessible semantics for the live tuning meter
  Why: A live visual meter is central UI, but color/needle-only feedback excludes TalkBack and color-blind users.
  Evidence: RESEARCH.md; Android Compose accessibility docs; existing Phase 5 meter goal.
  Touches: `ui/tuner`, Compose semantics tests, string resources.
  Acceptance: TalkBack announces string, note, cents bucket, and tune-up/tune-down state without repeating on every audio frame.
  Complexity: M

- [ ] P1 — Add last-used and favorite tuning startup behavior
  Why: Choona users requested custom/default startup tuning, and competitors reduce friction by reopening the expected tuning.
  Evidence: RESEARCH.md; Choona README; Choona issue #34; Choona PR #49/#70.
  Touches: `settings`, `tuning`, `ui/tuning-selector`.
  Acceptance: User can choose standard default, last-used tuning, or a favorite tuning to open at app start.
  Complexity: M

- [ ] P1 — Add in-app privacy rationale and Play Data Safety draft
  Why: Google Play requires a privacy policy even for apps that collect no data, and microphone access needs clear in-app explanation.
  Evidence: RESEARCH.md; Google Play User Data policy; Android sensitive-permission rationale docs.
  Touches: `ui/permission`, `README.md`, `app/src/main/res/values/strings.xml`.
  Acceptance: App contains a privacy screen stating local-only audio processing, no network, no storage of audio, retention policy, and contact placeholder.
  Complexity: S

- [ ] P2 — Add custom tuning import/export
  Why: Community discussion around common tunings points to import/export as a lighter alternative to endless built-in presets.
  Evidence: RESEARCH.md; thetwom/Tuner#77; Choona tuning editor work; Bill Farmer custom temperament file import.
  Touches: `tuning`, `settings`, `ui/tuning-editor`, storage access flow.
  Acceptance: A user can export custom tunings to a JSON file and import the same file on another install with validation errors shown inline.
  Complexity: L

- [ ] P2 — Add localization-ready strings and first translation pass
  Why: Tunerly supports multiple languages and F-Droid users expect localizable OSS apps; this repo currently has no resource plan.
  Evidence: RESEARCH.md; Tunerly README; Android resource conventions.
  Touches: `app/src/main/res/values/strings.xml`, `values-es`, `values-de`, Compose UI text.
  Acceptance: No hardcoded user-facing strings remain in Compose code, and at least Spanish and German resource files exist for core tuner states.
  Complexity: M

- [ ] P2 — Add clean tagged release and reproducible-build checklist
  Why: Choona's IzzyOnDroid report shows dirty or unreproducible APK artifacts damage trust for OSS Android tuners.
  Evidence: RESEARCH.md; Choona issue #51; F-Droid reproducibility pages.
  Touches: `.github/workflows/release.yml`, Gradle release config, `README.md`.
  Acceptance: Release workflow builds from a clean checkout, publishes signed APK/AAB checksums, and documents exact JDK/Gradle/AGP versions.
  Complexity: L

- [ ] P2 — Add adaptive layout support for tablets and foldables
  Why: Choona supports multi-window/large screens, and Android's adaptive guidance says apps should handle resizing and larger displays.
  Evidence: RESEARCH.md; Choona README; Android adaptive Compose docs.
  Touches: `ui/tuner`, `ui/settings`, Compose previews/tests.
  Acceptance: Main tuner and settings screens render without clipping in phone portrait, phone landscape, split-screen, tablet, and foldable-width previews.
  Complexity: M

- [ ] P3 — Add optional measurement freeze after note decay
  Why: Bill Farmer users requested automatic display lock after a tone ends, and CarlTune-style workflows help users read cents/frequency after plucking.
  Evidence: RESEARCH.md; billthefarmer/tuner#65; CarlTune Play listing.
  Touches: `pitch`, `ui/tuner`, `settings`.
  Acceptance: When enabled, the last stable pitch remains visible after decay until a new confident note is detected.
  Complexity: M

- [ ] P3 — Keep Wear OS as architecture-ready, not MVP UI
  Why: Choona proves Wear value, but the current repo has no phone app; shared core should make a later watch module possible without shipping watch UI now.
  Evidence: RESEARCH.md; Choona README; Choona PR #68/#70.
  Touches: `pitch`, `tuning`, `settings`, Gradle module boundaries.
  Acceptance: Core pitch/tuning/settings packages have no phone-only UI dependencies; no Wear module is added before phone MVP acceptance passes.
  Complexity: S

## MVP Acceptance Criteria

- The app can tune standard acoustic guitar strings from E2 through E4.
- The app can automatically identify the strummed open string when reasonably close to target.
- Guided mode avoids dangerous or ambiguous string misidentification.
- The UI clearly tells the user whether to tune up, tune down, or stop.
- Left/right peg instructions are not shown until the user completes peg-direction setup.
- The app works fully offline and requests no permission except microphone access.
