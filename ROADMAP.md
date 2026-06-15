# GuitarTuner Roadmap

Project version: GuitarTuner v0.0.4

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

## Research-Driven Additions (2026-06-15)

### P0

- [ ] P0 — Fix setNoiseGateRms config loss
  Why: `setNoiseGateRms()` rebuilds the pitch detector with only `silenceRms` copied, silently discarding `minFrequencyHz`/`maxFrequencyHz` set by `setTuning()`. Changing noise gate resets detection range to defaults, breaking bass/ukulele presets.
  Evidence: `AudioCaptureController.kt:132-135` — constructs `PitchDetectorConfig` with only `silenceRms`.
  Touches: `AudioCaptureController.kt` — copy full current `pitchDetector.config` via `.copy(silenceRms = rms)`.
  Acceptance: changing noise gate while bass tuning is active does not reset frequency range to 70-450 Hz.
  Complexity: S

- [ ] P0 — Align silence-definition constants
  Why: `TunerSessionState.SilentRms` (0.0002) vs `YinPitchDetector.config.silenceRms` (0.0015) can give contradictory feedback — UI says "silent" while detector is still gating, or vice versa.
  Evidence: `TunerSessionState.kt:63` vs `PitchDetectorConfig.silenceRms`.
  Touches: `TunerSessionState.kt`, `AudioCaptureController.kt` — derive UI silence threshold from the detector's configured gate.
  Acceptance: UI "no sound reaching mic" state and detector silence gate agree at all noise gate settings.
  Complexity: S

- [ ] P0 — Move accessibility strings to resources
  Why: `TuningAccessibility.kt` hardcodes English strings ("Waiting for a detected guitar string", "In tune", etc.) while de/es translations exist for all UI strings. TalkBack users on non-English locales hear English accessibility descriptions.
  Evidence: `TuningAccessibility.kt:22-43` — all state descriptions are English literals.
  Touches: `TuningAccessibility.kt`, `values/strings.xml`, `values-de/strings.xml`, `values-es/strings.xml`.
  Acceptance: TalkBack announces tuning state in the device language for en/de/es.
  Complexity: S

### P1

- [ ] P1 — Eliminate per-frame heap allocation on audio path
  Why: `frameBuffer.copyOf()` creates a 4096-element FloatArray (16 KB) per analysis frame at ~21 frames/sec = ~336 KB/s of garbage. Causes GC pressure visible on mid-range devices.
  Evidence: `AudioCaptureController.kt:289`.
  Touches: `AudioCaptureController.kt` — use a reusable double-buffer (two pre-allocated arrays, swap on each frame).
  Acceptance: no per-frame allocation in the `captureReadLoop` hot path; verified with Allocation Tracker or code inspection.
  Complexity: S

- [ ] P1 — Auto-advance in Guided mode
  Why: when a string reaches in-tune, the user must manually tap Next. GuitarTuna and Rocksmith auto-advance after a brief hold, which is the expected beginner UX. This is the single highest-value UX improvement for the guided flow.
  Evidence: GuitarTuna guided mode, Rocksmith Tuner, JustinGuitar beginner confusion threads.
  Touches: `TunerScreen.kt` or `MainActivity.kt` (LaunchedEffect watching `TuningStatus.InTune` + hold timer), `GuidedTuningWalkthrough.kt`, `strings.xml` (+de/es).
  Acceptance: in Guided mode, after a string is in-tune for 1.5 seconds, the guided step auto-advances to the next string with a brief visual/haptic confirmation. Manual Previous/Next still work. Auto-advance is optional (preference toggle, default on).
  Complexity: M

- [ ] P1 — Add SECURITY.md with APK verification
  Why: privacy-first positioning requires verifiable release artifacts. Choona has this; GuitarTuner does not.
  Evidence: Choona SECURITY.md, F-Droid reproducible builds documentation.
  Touches: new `SECURITY.md` at repo root. Content: SHA-256 checksum verification against GitHub Release `SHA256SUMS.txt`, keystore fingerprint for signed APKs.
  Acceptance: SECURITY.md exists with working verification instructions.
  Complexity: S

- [ ] P1 — Complete fastlane metadata for IzzyOnDroid submission
  Why: IzzyOnDroid is the fastest F-Droid distribution path; requires `title.txt` and `featureGraphic.png` in `fastlane/metadata/android/en-US/`.
  Evidence: https://izzyondroid.org/docs/general/Fastlane/
  Touches: `fastlane/metadata/android/en-US/title.txt`, `featureGraphic.png`; verify existing `full_description.txt`, `short_description.txt`, `changelogs/`, `images/phoneScreenshots/`.
  Acceptance: IzzyOnDroid metadata validation passes; submission can proceed.
  Complexity: S

### P2

- [ ] P2 — Pitch history timeline
  Why: the most-requested visual feature across competitor trackers (Moekadu #102, billthefarmer feature threads, Reddit r/guitar). Shows pitch drift over time, gives visual confidence that tuning is converging, and is the natural complement to the current instant-reading meter.
  Evidence: Moekadu pitch history display, billthefarmer strobe + history, Peterson iStroboSoft sweep display.
  Touches: new `ui/PitchHistoryView.kt` (Canvas composable), `AudioCaptureController.kt` or `TunerSessionState.kt` (ring buffer of recent cents readings), `TunerMeterPanel.kt` (embed below cents meter).
  Acceptance: scrolling timeline of cents-over-time visible during tuning; at least 5 seconds of history; updates at frame rate without jank.
  Complexity: M

- [ ] P2 — Spoken tuning feedback (TalkBack + optional TTS)
  Why: no Android tuner serves blind musicians. HotPaw Talking Tuner (iOS $0.99) is the only option; AppleVis forums have recurring unsolved requests for an Android equivalent. This would be a genuine market first.
  Evidence: HotPaw Talking Tuner, AppleVis forum threads, afb.org/aw accessibility resources.
  Touches: new `audio/SpokenFeedbackController.kt` (Android `TextToSpeech`), `TunerScreen.kt` (enable when TalkBack active OR user opts in), `StoredTunerPreferences.kt` (toggle), `strings.xml` (+de/es).
  Acceptance: when enabled, announces "<Note> <direction> <cents>" after each stable detection with debouncing (no overlapping speech). Works with TalkBack off for sighted users who want audio feedback.
  Complexity: M

- [ ] P2 — Octave-jump hysteresis in TuningAnalyzer
  Why: G3/G4 flips are the #1 accuracy complaint across all tuner issue trackers (Moekadu #88 with 17 comments, #114). Current second-harmonic correction uses a fixed 80-cent threshold without inter-frame state. StableMeasurementSmoother's octave-flip counter (threshold=3) mitigates but short flip bursts are still visible.
  Evidence: Moekadu #88, #114; Pano Tuner Reddit complaints; pYIN probabilistic approach (Mauch & Dixon 2014).
  Touches: `TuningAnalyzer.kt` (add inter-frame pitch candidate weighting or Viterbi-style octave state), `StableMeasurementSmoother.kt` (tighten octave-flip threshold).
  Acceptance: G3/G4 and low-E octave flips reduced below 1-in-20 frames in WAV fixture tests; no regression in standard fixtures.
  Complexity: L

- [ ] P2 — Noise-profile test fixtures
  Why: no test coverage for chatter, white noise, or impulse signals. These are the real-world environments where tuners fail and users give 1-star reviews.
  Evidence: Moekadu #72 (34 comments on noise jitter), Reddit tuner complaints about "jumpy readings in noisy rooms".
  Touches: `test/resources/guitar-fixtures/` (new WAV files: white noise, room ambience, guitar+chatter overlay), new test class.
  Acceptance: detector correctly returns Unstable/HighNoise for pure noise; does not produce false string detections from chatter.
  Complexity: M

- [ ] P2 — ProGuard keep rules for kotlinx-serialization
  Why: R8 may strip serialization metadata needed for `CustomTuningJsonCodec`. No keep rules exist in `proguard-rules.pro`. A minified release build has not been tested with custom tuning import.
  Evidence: empty `proguard-rules.pro`, kotlinx-serialization-json 1.11.0 dependency.
  Touches: `proguard-rules.pro` — add `-keepattributes` and `-keep class` rules for serialization, or verify `@Serializable` annotation processing generates sufficient keep rules via R8 full mode.
  Acceptance: minified release APK successfully imports a custom tuning JSON file.
  Complexity: S

- [ ] P2 — Portuguese and French localization
  Why: pt and fr are the #4 and #5 most common Android languages globally and the next highest-demand F-Droid locales after en/de/es.
  Evidence: F-Droid download statistics by locale, Android global language distribution.
  Touches: new `values-pt/strings.xml`, `values-fr/strings.xml` (135 strings each), `TuningAccessibility.kt` string resources (after P0 fix).
  Acceptance: all UI strings render correctly in pt and fr; no untranslated fallbacks.
  Complexity: M

### P3

- [ ] P3 — Tuning session summary
  Why: after completing a guided walkthrough, show a summary of how each string ended (in-tune, how many cents off at lock, time per string). Builds beginner confidence and encourages re-tuning. No competitor does this.
  Evidence: gamification research in music education apps, GuitarTuna session tracking (paywalled).
  Touches: `TunerSessionState.kt` (track per-string results during guided session), new `ui/SessionSummaryView.kt`, `strings.xml` (+de/es).
  Acceptance: after the last guided string hits in-tune, a summary card shows each string's final cents offset and total session time.
  Complexity: M

- [ ] P3 — Keep screen on during active tuning
  Why: fullscreen mode already uses `keepScreenOn`, but the main tuner screen does not. Phone screen dims/locks mid-tuning, requiring unlock + re-grant. Every competitor keeps screen on while listening.
  Evidence: GuitarTuna, Pano Tuner, Moekadu all keep screen on during active listening.
  Touches: `TunerScreen.kt` or `MainActivity.kt` — set `keepScreenOn` flag when `state.isListening` is true.
  Acceptance: screen stays on while tuner is actively listening; turns off when stopped.
  Complexity: S

- [ ] P3 — Dynamic Type / font scaling support
  Why: Android accessibility settings allow system-wide font scaling up to 200%. Compose `sp` units should respect this, but the fixed `fontSize = 92.sp` in fullscreen mode and compact readout metrics may overflow or clip at large scales.
  Evidence: Android accessibility font scaling docs, Material 3 dynamic type guidelines.
  Touches: `FullscreenTunerView.kt` (test at 200% scaling), `TunerReadouts.kt`, `TunerMeterPanel.kt`.
  Acceptance: all text readable and non-clipped at Android font scale 200% on a 360dp phone.
  Complexity: S

- [ ] P3 — Japanese, Korean, Chinese (Simplified) localization
  Why: ja/ko/zh-Hans are the top 3 Asian Android markets and have active guitar communities. Unlocks significant F-Droid and Play Store reach.
  Evidence: F-Droid download statistics, Android market share by region.
  Touches: new `values-ja/strings.xml`, `values-ko/strings.xml`, `values-zh-rCN/strings.xml`.
  Acceptance: all UI strings render correctly with correct CJK line breaking.
  Complexity: M

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
