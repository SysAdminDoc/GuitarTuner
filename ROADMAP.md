# GuitarTuner Roadmap

Project version: GuitarTuner v0.0.2

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

- [ ] P1 — Version bump to v0.1.0 and first tagged release
  Why: all P0/P1 features are shipped; the release workflow is configured but no signed release exists.
  Where: app/build.gradle.kts, README.md badges, CHANGELOG.md.
  Acceptance: v0.1.0 tag pushed, GitHub Release created with signed APK/AAB + SHA256SUMS.
  Complexity: S

- [ ] P1 — Re-capture screenshots on physical device
  Why: UI has changed significantly (chromatic mode, fullscreen, haptic toggle, mic picker, overshoot state); stale screenshots mislead users.
  Where: docs/screenshots/, fastlane/metadata/android/en-US/images/phoneScreenshots/, README.md.
  Complexity: S

### P2

- [ ] P2 — Optional strobe precision view
  Why: precision-view differentiation with a real demand signal (analog/needle alternatives requested in Moekadu #102); phase-accumulator Canvas is cheap once cents offset exists.
  Evidence: dsego/strobe-tuner phase-comparator design; Peterson strobe UX reputation; Moekadu issue 102.
  Touches: TunerScreen.kt (new meter style + setting), theme tokens; draw-phase-only state reads.
  Acceptance: strobe band drifts direction/speed proportional to cents and freezes when in tune; toggleable; default meter unchanged.
  Complexity: L

- [ ] P2 — Baseline profile for cold start
  Why: a tuner is a 30-second utility — cold start is the UX; baseline profiles cut startup 15-40% in published case studies.
  Evidence: developer.android.com baseline profiles overview + Todoist/Duolingo case studies.
  Touches: new baselineprofile module (Macrobenchmark generator), app/build.gradle.kts.
  Acceptance: profile generated and bundled in release; macrobenchmark shows measurable cold-start improvement on a physical device.
  Complexity: S

- [ ] P2 — New-string stretch-and-settle mode
  Why: new strings drift flat for days; the tune-stretch-retune cycle is standard advice no app operationalizes; extends Guided mode naturally.
  Evidence: Guitar World / Haze Guitars string-stretching guidance; JustinGuitar beginner confusion threads.
  Touches: GuidedTuningWalkthrough.kt (stretch cycle variant), TunerScreen.kt, strings.xml (+de/es).
  Acceptance: optional "new strings" walkthrough runs >=2 full passes, tracks per-string drift between passes, and reports when drift falls inside tolerance.
  Complexity: M

### P3

- [ ] P3 — Wear OS tuner app
  Why: Choona proves demand and the CoreBoundaryTest already keeps pitch/tuning/settings UI-free for reuse; watch mic tuning is a genuine leapfrog surface.
  Evidence: Choona v1.6.1 Wear beta (shared lib/ + wear/ modules); androidx.wear.compose material3 1.5.0 stable.
  Touches: new wear/ module, shared core extraction into a library module, release workflow artifacts.
  Acceptance: watch app detects strings and shows tune-up/down with EdgeButton/ArcProgressIndicator UI; phone app unaffected.
  Complexity: XL

### Audit Findings (deferred)

- [ ] P2 — Add AudioCaptureController lifecycle and concurrency tests
  Why: complex start/stop/close state machine and @Volatile field interactions have zero test coverage; audit found race windows in stop() and rebuildAnalyzer().
  Where: test/ new AudioCaptureControllerTest.kt (requires recorder abstraction or Robolectric).

- [ ] P3 — Responsive font sizing in FullscreenTunerView
  Why: fullscreen copy is cleaner now, but it still needs live small-phone/tablet visual proof before closing.
  Where: ui/FullscreenTunerView.kt.

## MVP Acceptance Criteria

- The app can tune standard acoustic guitar strings from E2 through E4.
- The app can automatically identify the strummed open string when reasonably close to target.
- Guided mode avoids dangerous or ambiguous string misidentification.
- The UI clearly tells the user whether to tune up, tune down, or stop.
- Left/right peg instructions are not shown until the user completes peg-direction setup.
- The app works fully offline and requests no permission except microphone access.
