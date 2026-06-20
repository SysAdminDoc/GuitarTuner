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

- [ ] P2 — FFT-accelerated YIN difference function
  Why: `differenceFunction()` in `YinPitchDetector.kt:141-156` is O(N^2) — ~2.5M multiplications per 4096-sample frame at ~23 fps. This is the primary CPU bottleneck on low-end devices and causes thermal throttling during extended use. FFT-accelerated YIN (Wiener-Khinchin theorem) reduces to O(N log N).
  Evidence: sevagh/pitch-detection C++ library demonstrates FFT-YIN; pYIN paper (Mauch 2014).
  Touches: `pitch/YinPitchDetector.kt` — replace time-domain loop with FFT-based autocorrelation.
  Acceptance: `differenceFunction` uses FFT; unit tests pass; CPU usage measurably lower on a Snapdragon 6xx device.
  Complexity: M

- [ ] P2 — Solfege / international note naming option
  Why: non-English markets (Romance languages, East Asia) expect Do/Re/Mi/Fa/Sol/La/Si notation. No Android tuner app offers this as a setting. billthefarmer/tuner offers it but with no localization-aware toggle.
  Evidence: billthefarmer/tuner solfege support; Moekadu uses scientific pitch only; community requests on r/guitar from non-English speakers.
  Touches: `tuning/GuitarString.kt` (add note name formatting), `settings/TunerSettings.kt` (new preference), `ui/TunerMeterPanel.kt`, all `values-*/strings.xml`.
  Acceptance: setting to switch between scientific pitch (C4) and solfege (Do4); default follows locale.
  Complexity: M

- [ ] P2 — In-app custom tuning editor
  Why: JSON import/export is a power-user flow. Intermediate users wanting to create a custom tuning (e.g., Open C, NST) must edit JSON externally. A simple in-app editor with string count + note/frequency pickers would serve a much wider audience.
  Evidence: Choona offers in-app tuning editor with note/octave entry; community request frequency on r/guitar.
  Touches: new `ui/TuningEditorScreen.kt`, `tuning/CustomTuningJsonCodec.kt`, `settings/CustomTuningRepository.kt`.
  Acceptance: user can create, edit, and delete custom tunings from within the app without JSON knowledge.
  Complexity: L

- [ ] P2 — Alternate-tuning WAV fixture tests
  Why: Drop D, Open G, DADGAD, bass, and ukulele tunings have zero audio fixture test coverage. The analyzer is tested only against standard guitar WAVs. A wrong-string detection bug in an alternate tuning would be invisible.
  Evidence: existing test coverage in `guitar-fixtures/` covers only standard 6-string; Moekadu #114 wrong-string latch in alternate tunings.
  Touches: `test/resources/guitar-fixtures/` (new WAV files for Drop D low-D, Open G low-D, bass E1, ukulele G4), new test classes.
  Acceptance: each alternate tuning has at least one WAV fixture with golden-target regression coverage.
  Complexity: M

### P3

- [ ] P3 — Capo / transposition support
  Why: capo usage shifts all string frequencies by N semitones. Players tuning with a capo need the tuner to show target notes relative to the capo position. Multiple commercial tuners support this; no OSS Android tuner does.
  Evidence: GuitarTuna premium feature; Fender Tune community request; r/guitar threads.
  Touches: `tuning/TuningAnalyzer.kt` (semitone offset), new capo setting in preferences, UI selector.
  Acceptance: setting capo to fret 2 shifts all target frequencies up 2 semitones; chromatic mode shows transposed note names.
  Complexity: M

- [ ] P3 — Left-handed instrument layout indicator
  Why: beginner left-handed players may be confused by string numbering in guided mode. A simple toggle to mirror the guided string display would improve onboarding.
  Evidence: Simply Tune by Fender offers left-handed diagrams; community request on beginner forums.
  Touches: `ui/TunerModeSettings.kt` (guided string display), new preference.
  Acceptance: guided mode shows strings in reverse visual order when left-handed toggle is on; tuning logic unchanged.
  Complexity: S

- [ ] P3 — SBOM generation for supply chain transparency
  Why: privacy-conscious users and F-Droid reviewers value a machine-readable bill of materials. CycloneDX Gradle plugin generates one at build time.
  Evidence: F-Droid reproducible build requirements; CycloneDX Gradle plugin.
  Touches: `app/build.gradle.kts` (add `org.cyclonedx.bom` plugin), CI workflow.
  Acceptance: `./gradlew cyclonedxBom` produces `build/reports/bom.json`.
  Complexity: S

## MVP Acceptance Criteria

- The app can tune standard acoustic guitar strings from E2 through E4.
- The app can automatically identify the strummed open string when reasonably close to target.
- Guided mode avoids dangerous or ambiguous string misidentification.
- The UI clearly tells the user whether to tune up, tune down, or stop.
- Left/right peg instructions are not shown until the user completes peg-direction setup.
- The app works fully offline and requests no permission except microphone access.
