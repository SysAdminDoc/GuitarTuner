# Research — GuitarTuner

Updated 2026-06-15 against GuitarTuner v0.0.4 (full Android app: Kotlin/Compose, in-project YIN + autocorrelation fallback + phase refinement, Auto/Guided/Chromatic modes, guitar/bass/ukulele presets, custom JSON tunings, DataStore settings, fullscreen stage mode, reference tone playback, haptic confirmation, fixture regression tests, release workflow). This replaces the v0.0.1 research pass.

## Executive Summary

GuitarTuner occupies the strongest unclaimed position in Android tuners: free + accurate + maintained + private + beginner-safe. BOSS Tuner is abandoned (2017), GuitarTuna/Fender gate features behind subscriptions, and OSS competitors (Moekadu, Choona, billthefarmer) optimize for DSP enthusiasts rather than beginners. Since v0.0.1, the project has shipped chromatic mode, bass/ukulele presets, phase refinement, mic-stolen detection, fullscreen stage mode, reference tone playback, haptic confirmation, mic device picker, and a substantial hardening pass. The remaining highest-value opportunities:

1. **Pitch history timeline** — the single most-requested visual feature across competitor trackers (Moekadu #102, Choona community); shows pitch drift over time and gives confidence that tuning is converging.
2. **Auto-advance in Guided mode** — when a string hits in-tune, automatically move to the next string (Rocksmith UX, GuitarTuna guided flow); eliminates manual taps for beginners.
3. **Octave-jump hysteresis hardening** — the #1 accuracy complaint class (Moekadu #88, 17 comments); current heuristic uses a fixed 80-cent improvement threshold without inter-frame hysteresis.
4. **F-Droid / IzzyOnDroid distribution** — fastlane metadata tree is present but incomplete; submission unlocks the privacy-first audience.
5. **Spoken tuning feedback for TalkBack** — no Android tuner serves blind musicians; HotPaw Talking Tuner (iOS $0.99) is the only option; this would be a genuine market first.
6. **Tuning history / session log** — show how long each string took, how many passes were needed; gamification-lite that builds confidence.
7. **SECURITY.md with APK verification** — Choona precedent; required for trust with privacy-conscious F-Droid users.
8. **Per-frame allocation elimination** — `frameBuffer.copyOf()` in the audio path creates ~86 KB/s of garbage; trivial double-buffer fix.
9. **Pitch detection noise profile fixtures** — no test coverage for chatter, white noise, or impulse signals; these are the environments where tuners fail.
10. **New-string stretch-and-settle mode** — operationalizes the standard tune-stretch-retune advice; no app does this.

## Product Map

- Core workflows: grant mic permission -> select tuning/mode -> listen -> auto-detect or guided walkthrough -> flat/sharp + tune-up/down guidance -> haptic confirmation at in-tune -> stop or fullscreen stage mode. Settings: A4 calibration (stepper + live measurement), cents tolerance, noise gate, freeze-last-note, peg direction, theme, startup tuning, custom JSON import/export.
- Personas: (1) beginner acoustic player needing safe guidance and overshoot protection; (2) intermediate player with alternate tunings and chromatic needs; (3) privacy-conscious F-Droid user; (4) gigging musician needing fullscreen stage readability; (5) blind/low-vision guitarist (unserved on Android).
- Platforms: Android 8.0+ (minSdk 26, target 36). Distribution: GitHub Releases workflow ready (signing secrets pending first push). IzzyOnDroid/F-Droid not yet submitted. `CoreBoundaryTest` keeps pitch/tuning/settings free of UI deps for later Wear reuse.
- Data flow: `AudioRecord` (48/44.1 kHz, 4096-sample frames, 2048 hop) -> `YinPitchDetector` -> `PhaseRefiner` (Goertzel single-bin DFT) -> `TuningAnalyzer` (second-harmonic octave correction) -> `StableMeasurementSmoother` (attack skip, octave-flip hysteresis, holdover) -> `MeasurementFreeze` -> `TunerSessionState` StateFlow -> Compose UI. All local; manifest declares `RECORD_AUDIO` only.

## Competitive Landscape

### Moekadu Tuner (thetwom, Codeberg, v9.1.1)
Best OSS detection stack: autocorrelation + FFT with phase/polynomial sub-bin refinement, pitch history display, custom instruments/temperaments, stretch tuning (v9.0.0), measure-A4-from-live (v8.2.0). **Learn from:** its issue tracker is a demand map — #88 G3/G4 octave jumps (17 comments), #72 noise jitter (34 comments), #114 wrong-string latch, #102 analog needle option, #103 distraction-free mode, #100 calibration in cents, #65 auto-freeze. **Avoid:** settings behind DSP jargon (#9), tiny touch targets (#35), overwhelming first-screen density.

### Choona (rohankhayech, v1.6.1)
Closest architectural sibling (Kotlin/Compose + Wear OS via shared `lib/` module). Chromatic mode, tuning editor with direct note/octave entry, reference tone playback, semitone/cents toggle, dual Play/FOSS flavors, fastlane + IzzyOnDroid distribution, SECURITY.md APK verification doc. **Learn from:** Wear OS module structure (shared core), IzzyOnDroid submission path, security doc pattern. **Open demand:** #78 mic device picker (shipped by GuitarTuner), #74 per-instrument detection range (shipped), #72 custom-tuning note ceiling. **Avoid:** hardcoded AudioRecord buffer sizes (crash fix in v1.4.2), no octave correction.

### billthefarmer/tuner
Pro-density reference: strobe display, 32+ custom temperaments, note/octave filters, display lock, transposition. **Learn from:** strobe UX (motion proportional to cents, freezes at lock), filter and freeze behaviors. **Open demand:** #65 auto-freeze last reading (shipped by GuitarTuner), #68 stretch tuning. **Avoid:** first-screen density that overwhelms beginners, no guided mode.

### GuitarTuna (Yousician)
~$2M/month paywall; alternate tunings, metronome, chord library paywalled. Community complaint corpus: ads interrupt tuning, tunings locked behind subscription, privacy concerns (requires account). **Learn from:** instant cold-start, beginner-guided visuals, auto-advance between strings. **Avoid:** monetization model, account requirement, ads before tuning.

### Peterson iStroboSoft / TC PolyTune
Strobe = motion-proportional-to-cents with a satisfying lock at zero; PolyTune's strum-all-strings polyphonic check is the most praised hardware innovation. **Learn from:** full-screen big-glyph readability at arm's length (clip-on ergonomics), strobe precision view, accu-pitch confirmation beep/haptic. **Avoid:** claiming 0.1-cent accuracy without validation infrastructure.

### HotPaw Talking Tuner (iOS, $0.99)
The only tuner blind musicians recommend (speaks "Note G is 15 cents flat"). No Android equivalent exists; AppleVis has recurring unsolved threads. **Learn from:** spoken-state mode works without screen reader — TTS announces note, direction, and cents magnitude after each detection. **Avoid:** depends on iOS Speech API; Android equivalent needs `TextToSpeech` with debouncing to avoid overlapping announcements.

### Pano Tuner / Cleartune / Simply Tune
Pano Tuner is the most-recommended free Android tuner on r/guitar — praised for simplicity, criticized for occasional octave errors. Cleartune ($3.99) = reference-grade chromatic with note wheel. Simply Tune (Fender lead-gen) = polished but requires account. All validate that simplicity + accuracy > features.

### Tunerly / cythara (orphaned)
Cautionary tales: single-maintainer abandonment. Tunerly's bugs (#38 mic stays on in background, #36 loose tolerance, #41 wrong DADGAD preset) are trust failures. Release cadence is itself a competitive feature. cythara (gstraube) abandoned 2019, no Compose, no Material 3.

## Security, Privacy, and Reliability

### Verified — already addressed in v0.0.2-v0.0.4
- Import/export crash: bounded reads, `TuningFileTooLargeException`, SAF URI error handling (v0.0.3).
- DataStore corruption recovery: catch `IOException`/`CorruptionException`, fall back to defaults (v0.0.3).
- Permission flow: `shouldShowRequestPermissionRationale` handling and settings deep-link for permanent denial (v0.0.3).
- Hardcoded English strings in `AudioCaptureController`: error codes mapped to resources (v0.0.3).

### Remaining findings
- **Per-frame heap allocation**: `AudioCaptureController.kt:289` — `frameBuffer.copyOf()` creates a 4096-element `FloatArray` (16 KB) per analysis frame at ~21 frames/sec = ~336 KB/s of short-lived garbage. Reusable double-buffer is trivial and eliminates GC pressure on the audio path.
- **Silence-definition mismatch**: `TunerSessionState.kt` (`SilentRms = 0.0002`) vs `YinPitchDetector` default `silenceRms = 0.0015`. The UI can claim mic is silent while the detector still gates (or vice versa). Aligning these to a single source constant prevents contradictory feedback.
- **Octave/harmonic robustness is heuristic**: `TuningAnalyzer.kt:95-111` uses a fixed 80-cent improvement threshold for second-harmonic halving with no inter-frame hysteresis. Competitor evidence (Moekadu #88, 17 comments) shows G3/G4 flips persist under real signals. The `StableMeasurementSmoother` octave-flip counter (threshold=3) mitigates but doesn't prevent short flip bursts visible to the user.
- **Accessibility: hardcoded English in TuningAccessibility.kt**: lines 22-43 use English strings ("Waiting for a detected guitar string", "In tune", "tune up", etc.) instead of string resources. TalkBack users on de/es locales hear English accessibility descriptions.
- **No SECURITY.md**: Choona and billthefarmer provide APK verification instructions. Missing for a privacy-first app.
- **Fastlane metadata incomplete**: `fastlane/metadata/android/en-US/` exists with changelogs and screenshots but missing `title.txt` (required for F-Droid/IzzyOnDroid submission). `featureGraphic.png` also missing.
- **No proguard-dictionary or class-name randomization**: R8 default obfuscation is sufficient for a no-secrets app, but custom rules in `proguard-rules.pro` are empty (no `-keep` rules for serialization). If `kotlinx-serialization-json` is used for custom tunings, ProGuard may strip serialization metadata.
- **`setNoiseGateRms` discards config**: `AudioCaptureController.kt:132-135` rebuilds the detector with only `silenceRms` copied, silently discarding any other config changes made via `setTuning()`. If `setTuning()` changed `minFrequencyHz`/`maxFrequencyHz` and then `setNoiseGateRms()` is called, the frequency range resets to defaults. Fix: copy the full current config.
- **Reference tone sample rate mismatch**: `TonePlayer.kt` generates at 44100 Hz while capture prefers 48000 Hz. Not a bug (playback and capture are independent AudioTrack/AudioRecord), but a `Measure A4` reading from the speaker output may be slightly off if the platform resamples oddly.

## Architecture Assessment

- **Boundaries are clean and test-enforced** (`CoreBoundaryTest.kt`): pitch/tuning/settings have zero Android-UI deps. This is the Wear OS reuse path — keep it.
- **UI split is well-structured** since v0.0.2: TunerScreen, TunerChrome, TunerMeterPanel, TunerReadouts, TunerSettingsPanel, TunerModeSettings, TunerPreferenceSettings, TunerComponents, FullscreenTunerView, PrivacyScreen, TuningAccessibility. The main screen is ~280 lines. Good.
- **`TunerRoute` in `MainActivity.kt`** is ~250 lines doing DI, permission flow, SAF I/O, and preference plumbing. Not yet a ViewModel; the `AudioCaptureController` + `TunerStateHolder` + compose-state pattern works but the controller creation in `remember` means it's not lifecycle-safe across configuration changes (process death loses state). For v0.x this is fine; ViewModel extraction is a v1.0 prerequisite.
- **`setNoiseGateRms` fragile constructor pattern**: rebuilds detector with only `silenceRms`, losing other config. Should copy full current config.
- **Test coverage**: 22 test files covering pitch detection, tuning analysis, smoother, freeze, chromatic, JSON codec, accessibility, architecture boundaries, and WAV fixtures. Gaps: no `AudioCaptureController` tests (race conditions in start/stop/rebuild), no alternate-tuning WAV fixtures (Drop D/Open G analyzers untested against audio), no noise-profile fixtures, no DataStore repository tests, no UI interaction/screenshot tests.
- **i18n**: en/de/es complete for all 135 string resources. Missing: pt, fr, ja, ko, zh (the top F-Droid locales after en/de/es). Accessibility strings in `TuningAccessibility.kt` are hardcoded English — must be moved to resources.
- **Build**: Gradle 9.5.1, AGP 9.2.1, Kotlin/Compose 2.3.21, Compose BOM 2026.05.01, DataStore 1.2.1, kotlinx-serialization-json 1.11.0. All current. Dependency verification metadata exists in `gradle/verification-metadata.xml`. No SBOM generation.

## Rejected Ideas

- **Metronome/chords/lessons/recorder** (Choona #26, Soundcorset model): retention bloat contradicting narrow-tuner philosophy. Source: Choona issue #26.
- **Polyphonic strum-check** (PolyTune model): YIN is single-f0; multi-f0 needs a different engine. Source: TC Electronic PolyTune.
- **Neural pitch detection** (CREPE/PESTO/SwiftF0): adds ONNX/TFLite runtime (2-5 MB) to a zero-dependency DSP app; gains only matter in noisy rooms. Reconsider if classical hardening proves insufficient. Source: CREPE paper, SwiftF0 MIT repo.
- **Oboe/AAudio native capture**: latency dominated by 85-186 ms analysis window, not capture path; NDK complexity buys nothing. Source: Oboe GitHub #1006.
- **Temperaments/stretch-tuning suites**: Moekadu and billthefarmer own this niche; cognitive load contradicts beginner-first. Source: billthefarmer #68.
- **Background/FGS microphone capture**: Android 14+ while-in-use restrictions plus privacy promise. Source: Tunerly #38, Android docs.
- **Home-screen widget tuner**: Glance renders to RemoteViews with no mic access. Source: Android Glance docs.
- **Cloud sync/accounts**: breaks the no-network contract. Source: product philosophy.
- **Dual Play/FOSS flavors**: nothing proprietary bundled; single FOSS build stays simpler. Source: Choona v1.5.1 precedent.
- **In-app tuning editor with note/octave pickers** (Choona model): JSON import/export covers the power-user case; an editor adds UI complexity for a rare workflow. Reconsider if custom tuning adoption data shows high usage.
- **Animated needle/gauge meter**: adds Compose animation complexity for aesthetic preference, not accuracy. The current tick-based meter is cleaner and more accessible. Source: Moekadu #102 shows mixed demand.

## Sources

OSS competitors and issues:
- https://codeberg.org/thetwom/Tuner (Moekadu, + issues 88, 72, 114, 102, 103, 100, 65, 9, 35)
- https://github.com/rohankhayech/Choona (+ issues 78, 74, 72, 26; SECURITY.md)
- https://github.com/billthefarmer/tuner (+ issues 65, 68)
- https://github.com/brianhorn/Tunerly/issues (38, 36, 41)
- https://github.com/gstraube/cythara
- https://github.com/DonBraulio/tuneo
- https://f-droid.org/en/packages/de.moekadu.tuner/

Commercial / community:
- https://apps.apple.com/us/app/guitartuna-guitar-bass-tuner/id527588389
- https://www.petersontuners.com/products/istrobosoft/
- https://www.tcelectronic.com/product.html?modelCode=0800-00010
- https://americansongwriter.com/best-guitar-tuner-apps/
- https://www.reddit.com/r/guitar/search?q=tuner+app
- https://community.justinguitar.com/t/app-vs-clip-on-tuner/412558
- https://news.ycombinator.com/item?id=28802306

DSP / platform:
- https://29a.ch/2020/04/15/guitar-tuner (web YIN implementation)
- https://github.com/dsego/strobe-tuner (strobe phase-comparator design)
- https://qmro.qmul.ac.uk/xmlui/bitstream/handle/123456789/6040/MAUCHpYINFundamental2014Accepted.pdf (pYIN)
- https://developer.android.com/guide/topics/media/sharing-audio-input
- https://developer.android.com/develop/ui/views/haptics/haptics-apis
- https://developer.android.com/topic/performance/baselineprofiles
- https://developer.android.com/reference/android/speech/tts/TextToSpeech

Distribution / security:
- https://f-droid.org/docs/Reproducible_Builds/
- https://izzyondroid.org/docs/general/Fastlane/
- https://oversecured.com/blog/introducing-mavengate-a-supply-chain-attack-method-for-java-and-android-applications
- https://applevis.com/forum (blind musician tuner threads)

## Open Questions

- Real-device behavior of the mic-stolen scenario (second capture app foregrounding) on Android 14/15 — needed to tune the zeros-while-listening detector threshold/duration.
- Whether Compose recomposition of the live meter actually drops frames on mid-range hardware (Layout Inspector / macrobenchmark check) — gates the draw-phase optimization item.
- ProGuard behavior with kotlinx-serialization: does R8 strip metadata needed for `CustomTuningJsonCodec` decoding? Needs a minified release test with a custom tuning import.
