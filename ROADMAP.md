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

### Phase 2 - Audio Capture Foundation

- [ ] Add microphone state, permission denial, device-wide mic-toggle/silent-input, clipping, and high-noise states.
- [ ] Verify capture on an emulator-safe path and at least one physical Android device.

### Phase 3 - Pitch Detection Engine

- [ ] Add smoothing across stable frames after pluck attack.
- [ ] Handle low-E octave errors and weak fundamentals.
- [ ] Expose pitch result as frequency, nearest note, cents offset, confidence, and signal state.

### Phase 4 - Guitar Tuning Model

- [ ] Add guided mode that locks onto one target string at a time.
- [ ] Add auto mode that selects the most likely strummed string.
- [ ] Add A4 calibration setting with 440 Hz default.

### Phase 5 - Guided Tuning UI

- [ ] Build main tuner screen with large note, string, cents, and confidence readout.
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

- [ ] Add a small local recorded-guitar fixture set.
- [ ] Compare tuning results against a known-good tuner.
- [ ] Capture screenshots for README after UI exists.
- [ ] Add signed release build documentation.
- [ ] Prepare GitHub release workflow after a remote exists.

## Research-Driven Additions

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
