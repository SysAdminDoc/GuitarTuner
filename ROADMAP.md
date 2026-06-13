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

## Research-Driven Additions

### P1

- [ ] P1 — Chromatic mode
  Why: table stakes — every maintained competitor has it (Choona shipped v1.5.0, Moekadu, billthefarmer); detects any note rather than nearest string.
  Evidence: Choona v1.5.0 release notes; Moekadu F-Droid description; JusTune HN thread requests.
  Touches: TuningAnalyzer.kt / new ChromaticAnalyzer, TuningMode enum, TunerScreen.kt mode selector, strings.xml (+de/es), tests.
  Acceptance: chromatic mode shows nearest chromatic note + cents for any pitch in 70-450 Hz (range widened as needed); Auto/Guided behavior unchanged.
  Complexity: M

- [ ] P1 — Reference tone playback
  Why: lets users tune by ear and verify the target; Choona ships it; Moekadu issue #55 shows open demand; needs no new permission.
  Evidence: Choona README feature list; thetwom/Tuner issue 55.
  Touches: new audio/TonePlayer.kt (AudioTrack sine/Karplus-strong), TunerScreen.kt per-string play affordance, capture pause-during-playback logic.
  Acceptance: tapping a string target plays its frequency (A4-calibrated); capture suspends during playback and resumes after; works offline.
  Complexity: M

- [ ] P1 — Non-visual tuning: TalkBack live region + spoken state mode
  Why: no first-class accessible tuner exists on Android (blind guitarists are pointed to a $0.99 iOS app); meter semantics exist but nothing announces state transitions.
  Evidence: AFB AccessWorld on HotPaw Talking Tuner; recurring AppleVis request threads; ui/TuningAccessibility.kt already buckets cents.
  Touches: TunerScreen.kt (liveRegion = Polite on the stable readout), TuningAccessibility.kt, optional TextToSpeech mode behind a setting, TunerPreferencesRepository.kt.
  Acceptance: TalkBack announces debounced transitions ("E2, 15 cents flat" → "E2, in tune") without per-frame chatter; optional spoken mode works with TalkBack off; haptic channel distinguishes sharp/flat.
  Complexity: M


- [ ] P1 — Fastlane metadata and IzzyOnDroid submission, then F-Droid
  Why: distribution beyond GitHub is how OSS tuners reach users; IzzyOnDroid auto-ingests fastlane from GitHub releases; F-Droid needs a fdroiddata MR with tag-based auto-updates.
  Evidence: izzyondroid.org fastlane docs; f-droid.org Inclusion How-To; Choona and Moekadu metadata layouts.
  Touches: new fastlane/metadata/android/en-US/ (short_description ≤80 chars, full_description, icon, phoneScreenshots, changelogs/<versionCode>.txt), README badges.
  Acceptance: repo carries valid fastlane metadata; IzzyOnDroid listing live after first tagged signed release; fdroiddata MR submitted with UpdateCheckMode: Tags.
  Complexity: M

### P2

- [ ] P2 — Microphone input device picker
  Why: Bluetooth earbud mics ruin tuning; requested independently in two competitor trackers and shipped by no one — cheap differentiator.
  Evidence: Choona issue 78; thetwom/Tuner issue 65; AudioRecord.setPreferredDevice API.
  Touches: AudioCaptureController.kt (AudioDeviceInfo enumeration + setPreferredDevice), TunerScreen.kt settings section, TunerPreferencesRepository.kt.
  Acceptance: settings list available input devices; selection persists; diagnostics row shows the active device; default remains auto.
  Complexity: M

- [ ] P2 — Bass and ukulele presets with per-instrument detection range
  Why: most-requested preset expansion across competitor trackers; requires widening the fixed 70-450 Hz detection window per instrument (bass E1 = 41.2 Hz).
  Evidence: Choona issues 74/72; Tunerly issue 43; PitchDetectorConfig min/maxFrequencyHz.
  Touches: StandardGuitarTuning.kt/GuitarTunings.kt (instrument field), PitchDetectorConfig wiring per tuning, TunerScreen.kt selector, fixtures for E1/A1.
  Acceptance: bass standard and ukulele standard presets tune correctly end-to-end; guitar detection unaffected; fixture tests cover the new ranges.
  Complexity: M

- [ ] P2 — Detect A4 calibration from a live reference tone
  Why: matches Moekadu v8.2.0; tuning to a piano/band reference is the real calibration workflow, not typing Hz.
  Evidence: Codeberg thetwom/Tuner v8.2.0 release notes; Moekadu issue 100 (cents-based calibration demand).
  Touches: TunerScreen.kt A4 settings row ("measure" action), reuse of live PitchEstimate, TunerPreferencesRepository.kt.
  Acceptance: "Measure A4" listens, locks on a stable tone near A4, and offers to apply the measured Hz; manual stepper still works.
  Complexity: M

- [ ] P2 — Sub-cent refinement via single-bin-DFT phase tracking
  Why: phase difference across hops at the detected fundamental yields sub-cent precision on E2 without longer windows; the technique behind software strobe tuners.
  Evidence: dsego/strobe-tuner design notes; 29a.ch/2020 guitar tuner write-up; billthefarmer/ctuner FFT-phase approach.
  Touches: pitch/ new PhaseRefiner.kt fed by YIN coarse estimate, AudioCaptureController.kt pipeline, fixture accuracy assertions tightened.
  Acceptance: fixture suite shows ≤1-cent error on E2-E4 steady tones (current tolerance documented as baseline); latency unchanged.
  Complexity: M

- [ ] P2 — Optional strobe precision view
  Why: precision-view differentiation with a real demand signal (analog/needle alternatives requested in Moekadu #102); phase-accumulator Canvas is cheap once cents offset exists.
  Evidence: dsego/strobe-tuner phase-comparator design; Peterson strobe UX reputation; Moekadu issue 102.
  Touches: TunerScreen.kt (new meter style + setting), theme tokens; draw-phase-only state reads.
  Acceptance: strobe band drifts direction/speed proportional to cents and freezes when in tune; toggleable; default meter unchanged.
  Complexity: L

- [ ] P2 — Split TunerScreen.kt and extract a testable state holder
  Why: 1300-line screen file plus a 200-line route composable doing permission flow, SAF I/O, and preference plumbing blocks UI testing and invites regressions as P1 features land.
  Evidence: ui/TunerScreen.kt (~50 KB); MainActivity.kt:52-257.
  Touches: ui/ (meter, guided panel, settings sections, diagnostics files), new TunerStateHolder/ViewModel, MainActivity.kt.
  Acceptance: no file over ~400 lines in ui/; permission/import/export logic unit-tested; behavior identical (screenshots re-captured).
  Complexity: M

- [ ] P2 — Capture-loop, alternate-tuning, and noise fixture tests
  Why: AudioCaptureController logic (source fallback, zero-frame handling, frame assembly) and non-standard tunings have zero automated coverage; noise robustness is asserted only by one synthetic case.
  Evidence: app/src/test/ inventory (no controller tests; guitar-fixtures/ holds standard tuning only).
  Touches: test/ new AudioCaptureController tests (recorder abstraction), WAV fixtures for Drop D/DADGAD and chatter/white-noise/impulse profiles.
  Acceptance: gradlew check exercises controller frame assembly and fallback paths; alternate-tuning and noise fixtures gate regressions.
  Complexity: M

- [ ] P2 — Gradle dependency verification and repository lockdown
  Why: MavenGate-class build-time attacks are the realistic attack surface for a no-network app; verification metadata + FAIL_ON_PROJECT_REPOS is cheap insurance that matches the privacy positioning.
  Evidence: Oversecured MavenGate write-up; Gradle dependency-verification docs; settings.gradle.kts currently lacks both.
  Touches: settings.gradle.kts, gradle/verification-metadata.xml.
  Acceptance: builds fail on unverified artifacts or project-declared repos; CI green.
  Complexity: S

- [ ] P2 — Reproducible release build configuration
  Why: F-Droid reproducible verification lets the app ship developer-signed; also strengthens the GitHub SHA256SUMS story.
  Evidence: f-droid.org/docs/Reproducible_Builds (PNG crunching, generatedDensities, vcsInfo, R8 core-count nondeterminism).
  Touches: app/build.gradle.kts (cruncherEnabled=false, vectorDrawables.generatedDensities=[], vcsInfo off), .github/workflows/release.yml (pinned toolchain).
  Acceptance: two clean builds of the same tag on different machines produce byte-identical APKs apart from signature.
  Complexity: M

- [ ] P2 — Baseline profile for cold start
  Why: a tuner is a 30-second utility — cold start is the UX; baseline profiles cut startup 15-40% in published case studies.
  Evidence: developer.android.com baseline profiles overview + Todoist/Duolingo case studies.
  Touches: new baselineprofile module (Macrobenchmark generator), app/build.gradle.kts.
  Acceptance: profile generated and bundled in release; macrobenchmark shows measurable cold-start improvement on a physical device.
  Complexity: S

- [ ] P2 — Per-app language picker
  Why: de/es translations exist but users can't select them independently of system language; Moekadu shipped this (v9.1.0) for the same reason.
  Evidence: Codeberg thetwom/Tuner v9.1.0; androidx per-app language (LocaleConfig) docs.
  Touches: AndroidManifest.xml (localeConfig), res/xml/locales_config.xml, settings section in TunerScreen.kt.
  Acceptance: in-app language choice among en/de/es persists and survives restart on API 26+ (AppCompatDelegate fallback below 33).
  Complexity: S

### P3

- [ ] P3 — New-string stretch-and-settle mode
  Why: new strings drift flat for days; the tune→stretch→retune cycle is standard advice no app operationalizes; extends Guided mode naturally.
  Evidence: Guitar World / Haze Guitars string-stretching guidance; JustinGuitar beginner confusion threads.
  Touches: GuidedTuningWalkthrough.kt (stretch cycle variant), TunerScreen.kt, strings.xml (+de/es).
  Acceptance: optional "new strings" walkthrough runs ≥2 full passes, tracks per-string drift between passes, and reports when drift falls inside tolerance.
  Complexity: M

- [ ] P3 — Distraction-free playing mode
  Why: minimal full-screen sharp/flat indication for intonation practice; open demand in Moekadu #103; cheap once chromatic mode exists.
  Evidence: thetwom/Tuner issue 103.
  Touches: TunerScreen.kt (fullscreen meter variant), depends on chromatic mode.
  Acceptance: a fullscreen mode shows only note + direction arrow + in-tune color state with screen kept on.
  Complexity: S

- [ ] P3 — Wear OS tuner app
  Why: Choona proves demand and the CoreBoundaryTest already keeps pitch/tuning/settings UI-free for reuse; watch mic tuning is a genuine leapfrog surface.
  Evidence: Choona v1.6.1 Wear beta (shared lib/ + wear/ modules); androidx.wear.compose material3 1.5.0 stable.
  Touches: new wear/ module, shared core extraction into a library module, release workflow artifacts.
  Acceptance: watch app detects strings and shows tune-up/down with EdgeButton/ArcProgressIndicator UI; phone app unaffected.
  Complexity: XL

## MVP Acceptance Criteria

- The app can tune standard acoustic guitar strings from E2 through E4.
- The app can automatically identify the strummed open string when reasonably close to target.
- Guided mode avoids dangerous or ambiguous string misidentification.
- The UI clearly tells the user whether to tune up, tune down, or stop.
- Left/right peg instructions are not shown until the user completes peg-direction setup.
- The app works fully offline and requests no permission except microphone access.
