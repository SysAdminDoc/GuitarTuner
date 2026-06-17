# GuitarTuner Roadmap

Project version: GuitarTuner v0.1.0

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

## Research-Driven Additions

### P1

- [ ] P1 — Push v0.1.0 tag and create first signed GitHub Release
  Why: version is bumped to v0.1.0 in code; the release workflow is configured but signing secrets are not yet set in the GitHub repository.
  Where: GitHub repository settings (secrets), then `git tag v0.1.0 && git push origin v0.1.0`.
  Acceptance: v0.1.0 tag pushed, GitHub Release created with signed APK/AAB + SHA256SUMS.
  Complexity: S (blocked on signing secrets)

- [ ] P1 — Re-capture screenshots on physical device
  Why: UI has changed significantly (chromatic mode, fullscreen, haptic toggle, mic picker, overshoot state); stale screenshots mislead users.
  Where: docs/screenshots/, fastlane/metadata/android/en-US/images/phoneScreenshots/, README.md.
  Complexity: S

### P2

- [ ] P2 — Baseline profile for cold start
  Why: a tuner is a 30-second utility — cold start is the UX; baseline profiles cut startup 15-40% in published case studies.
  Evidence: developer.android.com baseline profiles overview + Todoist/Duolingo case studies.
  Touches: new baselineprofile module (Macrobenchmark generator), app/build.gradle.kts.
  Acceptance: profile generated and bundled in release; macrobenchmark shows measurable cold-start improvement on a physical device.
  Complexity: S

### P3

- [ ] P3 — Wear OS tuner app
  Why: Choona proves demand and the CoreBoundaryTest already keeps pitch/tuning/settings UI-free for reuse; watch mic tuning is a genuine leapfrog surface.
  Evidence: Choona v1.6.1 Wear beta (shared lib/ + wear/ modules); androidx.wear.compose material3 1.5.0 stable.
  Touches: new wear/ module, shared core extraction into a library module, release workflow artifacts.
  Acceptance: watch app detects strings and shows tune-up/down with EdgeButton/ArcProgressIndicator UI; phone app unaffected.
  Complexity: XL

### Audit Findings (deferred)

- [ ] P3 — Responsive font sizing in FullscreenTunerView
  Why: fullscreen copy is cleaner now, but it still needs live small-phone/tablet visual proof before closing.
  Where: ui/FullscreenTunerView.kt.

## Research-Driven Additions (2026-06-15)

### P3

- [ ] P3 — Dynamic Type / font scaling support
  Why: Android accessibility settings allow system-wide font scaling up to 200%. Compose `sp` units should respect this, but the fixed `fontSize = 92.sp` in fullscreen mode and compact readout metrics may overflow or clip at large scales.
  Evidence: Android accessibility font scaling docs, Material 3 dynamic type guidelines.
  Touches: `FullscreenTunerView.kt` (test at 200% scaling), `TunerReadouts.kt`, `TunerMeterPanel.kt`.
  Acceptance: all text readable and non-clipped at Android font scale 200% on a 360dp phone.
  Complexity: S

- [ ] P3 — Alternate-tuning WAV fixture tests
  Why: Drop D, Open G, DADGAD, bass, and ukulele tunings have zero audio fixture test coverage. The analyzer is tested only against standard guitar WAVs. A wrong-string detection bug in an alternate tuning would be invisible.
  Evidence: existing test coverage in `guitar-fixtures/` covers only standard 6-string; Moekadu #114 wrong-string latch in alternate tunings.
  Touches: `test/resources/guitar-fixtures/` (new WAV files for Drop D low-D, Open G low-D, bass E1, ukulele G4), new test classes.
  Acceptance: each alternate tuning has at least one WAV fixture with golden-target regression coverage.
  Complexity: M

## MVP Acceptance Criteria

- The app can tune standard acoustic guitar strings from E2 through E4.
- The app can automatically identify the strummed open string when reasonably close to target.
- Guided mode avoids dangerous or ambiguous string misidentification.
- The UI clearly tells the user whether to tune up, tune down, or stop.
- Left/right peg instructions are not shown until the user completes peg-direction setup.
- The app works fully offline and requests no permission except microphone access.
