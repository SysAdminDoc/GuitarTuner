# GuitarTuner Roadmap

Project version: GuitarTuner v0.1.0

This roadmap contains incomplete work only. GuitarTuner is an offline, open-source Android acoustic guitar tuner. The first implementation target is standard six-string acoustic guitar tuning by microphone.

Blocked items live in `Roadmap_Blocked.md` (gitignored). Move items back here when the blocker is resolved.

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

## Research-Driven Additions (2026-06-19)

### P2

- [ ] P2 — In-app custom tuning editor
  Why: JSON import/export is a power-user flow. Intermediate users wanting to create a custom tuning (e.g., Open C, NST) must edit JSON externally. A simple in-app editor with string count + note/frequency pickers would serve a much wider audience.
  Evidence: Choona offers in-app tuning editor with note/octave entry; community request frequency on r/guitar.
  Touches: new `ui/TuningEditorScreen.kt`, `tuning/CustomTuningJsonCodec.kt`, `settings/CustomTuningRepository.kt`.
  Acceptance: user can create, edit, and delete custom tunings from within the app without JSON knowledge.
  Complexity: L

## MVP Acceptance Criteria

- The app can tune standard acoustic guitar strings from E2 through E4.
- The app can automatically identify the strummed open string when reasonably close to target.
- Guided mode avoids dangerous or ambiguous string misidentification.
- The UI clearly tells the user whether to tune up, tune down, or stop.
- Left/right peg instructions are not shown until the user completes peg-direction setup.
- The app works fully offline and requests no permission except microphone access.
