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

## MVP Acceptance Criteria

- The app can tune standard acoustic guitar strings from E2 through E4.
- The app can automatically identify the strummed open string when reasonably close to target.
- Guided mode avoids dangerous or ambiguous string misidentification.
- The UI clearly tells the user whether to tune up, tune down, or stop.
- Left/right peg instructions are not shown until the user completes peg-direction setup.
- The app works fully offline and requests no permission except microphone access.
