# Research — GuitarTuner

Updated 2026-06-19 against GuitarTuner v0.1.0 (Kotlin/Compose, YIN + autocorrelation + phase refinement, Auto/Guided/Chromatic modes, guitar/bass/ukulele presets, 1-12 string custom tunings, strobe meter, pitch history timeline, spoken TTS, fullscreen stage mode, stretch-and-settle, session summary, 8 locales, 22 test files, release workflow). Replaces the v0.0.4 research pass.

## Executive Summary

GuitarTuner v0.1.0 holds the strongest position in Android OSS tuners: free, accurate, private, maintained, beginner-safe, and feature-competitive with commercial apps. GuitarTuna paywalls alternate tunings; Fender Tune requires accounts; BOSS Tuner is abandoned (2017); Moekadu targets DSP enthusiasts over beginners; Choona lacks stretch mode, session summary, and spoken feedback. The project has shipped nearly every feature from the previous research pass and now faces a distribution and polish inflection: getting the app into users' hands (F-Droid/IzzyOnDroid), hardening architecture for v1.0 (ViewModel extraction, test gaps), and adding the competitive features that remain unique opportunities.

Top 10 opportunities in priority order:

1. **F-Droid / IzzyOnDroid submission** — the privacy-first audience can't find the app; fastlane metadata is nearly complete.
2. **ViewModel extraction for TunerRoute** — `remember`-based controller creation loses state on process death; prerequisite for v1.0 stability.
3. **FFT-accelerated YIN difference function** — O(N^2) hot path is the primary CPU bottleneck on low-end devices.
4. **StrobeMeter accessibility semantics** — strobe mode is completely invisible to TalkBack users.
5. **`ignoreUnknownKeys = true` in CustomTuningJsonCodec** — current setting breaks forward-compatible tuning file imports.
6. **Session summary string translations** — 3 new strings (`session_complete`, `session_cents_value`, `session_dismiss`) missing from all 7 non-English locales.
7. **HapticFeedbackConstants.CONFIRM API 30 fallback** — haptic confirmation silently does nothing on API 26-29 devices.
8. **Solfege / international note naming** — non-English markets expect Do/Re/Mi; no tuner app on Android offers this.
9. **In-app tuning editor** — JSON import/export is a power-user flow; a simple in-app editor would serve intermediate users.
10. **Quick-tune widget or shortcut** — app shortcut to start listening immediately from the home screen.

## Product Map

- **Core workflows**: grant mic permission -> select tuning/mode -> listen -> auto-detect or guided walkthrough -> flat/sharp + tune-up/down guidance -> haptic confirmation at in-tune -> optional stretch-and-settle multi-pass -> session summary card -> stop or fullscreen stage mode. Settings: A4 calibration (stepper + live measurement), cents tolerance, noise gate, freeze-last-note, peg direction, meter style (Normal/Strobe), spoken feedback, auto-advance, theme, startup tuning, mic device picker, custom JSON import/export.
- **Personas**: (1) beginner acoustic player needing safe guidance and overshoot protection; (2) intermediate player with alternate tunings, chromatic, and custom tuning needs; (3) privacy-conscious F-Droid/IzzyOnDroid user; (4) gigging musician needing fullscreen stage readability; (5) blind/low-vision guitarist using TalkBack + spoken feedback.
- **Platforms**: Android 8.0+ (minSdk 26, target 36). Distribution: GitHub Releases (signing secrets pending). F-Droid/IzzyOnDroid not yet submitted. `CoreBoundaryTest` keeps pitch/tuning/settings free of UI deps for Wear reuse.
- **Data flow**: `AudioRecord` (48/44.1 kHz, 4096 frames, 2048 hop) -> `YinPitchDetector` (time-domain O(N^2) + autocorrelation fallback) -> `PhaseRefiner` (Goertzel single-bin DFT) -> `TuningAnalyzer` (second-harmonic octave correction with inter-frame hysteresis) -> `StableMeasurementSmoother` (attack skip, octave-flip counter, holdover) -> `MeasurementFreeze` -> `TunerSessionState` StateFlow -> Compose UI. All local; manifest declares `RECORD_AUDIO` only.

## Competitive Landscape

### Moekadu Tuner (thetwom, Codeberg, ~v9.2+)
Best OSS detection stack: autocorrelation + FFT with phase/polynomial sub-bin refinement, pitch history, custom instruments/temperaments, stretch tuning. **Learn from:** its issue tracker as demand map — G3/G4 octave jumps (#88, 17 comments), noise jitter (#72, 34 comments), wrong-string latch (#114), analog needle request (#102), distraction-free (#103), calibration in cents (#100), auto-freeze (#65). **Avoid:** settings behind DSP jargon, overwhelming first-screen density.

### Choona (rohankhayech, ~v1.6+)
Closest architectural sibling: Kotlin/Compose + Wear OS via shared `lib/` module. Chromatic mode, tuning editor with note/octave entry, reference tone, dual Play/FOSS flavors, IzzyOnDroid distribution, SECURITY.md. **Learn from:** Wear OS module structure, IzzyOnDroid submission path. **Advantage over:** GuitarTuner has stretch mode, session summary, spoken TTS, strobe meter, pitch history — features Choona lacks.

### billthefarmer/tuner
Pro-density reference: strobe display, 32+ custom temperaments, note/octave filters, display lock, transposition. **Learn from:** strobe motion-proportional-to-cents UX (GuitarTuner's strobe already implements this), filter and freeze behaviors. **Avoid:** first-screen density that overwhelms beginners.

### GuitarTuna (Yousician, ~$2M/month)
Alternate tunings, metronome, chord library all paywalled. **Learn from:** instant cold-start UX, beginner-guided visuals, auto-advance between strings (GuitarTuner has this). **Avoid:** monetization model, account requirement, ads before tuning.

### Peterson iStroboSoft / TC PolyTune
Strobe = motion-proportional-to-cents with satisfying lock at zero. PolyTune's strum-all polyphonic check is the most-praised hardware innovation. **Learn from:** full-screen big-glyph readability at arm's length. **Avoid:** claiming 0.1-cent accuracy without validation infrastructure.

### HotPaw Talking Tuner (iOS, $0.99)
Only tuner blind musicians recommend — speaks "Note G is 15 cents flat". No Android equivalent. GuitarTuner's spoken TTS feedback fills this gap and is a genuine market first on Android.

### Pano Tuner
Most-recommended free Android tuner on r/guitar — praised for simplicity, criticized for occasional octave errors. Validates that simplicity + accuracy > features.

## Security, Privacy, and Reliability

### Already addressed (v0.0.2-v0.1.0)
- Import/export: bounded reads, `TuningFileTooLargeException`, SAF URI error handling.
- DataStore corruption recovery: catch `IOException`/`CorruptionException`, fall back to defaults.
- Permission flow: `shouldShowRequestPermissionRationale` handling and settings deep-link.
- Per-frame allocation: eliminated via pre-allocated buffers in both `YinPitchDetector` and `AudioCaptureController`.
- SECURITY.md: APK verification instructions.
- ProGuard: kotlinx-serialization keep rules present and correct in `proguard-rules.pro`.
- TalkBack: accessibility descriptions moved to string resources for all 8 locales.
- Thread safety: `SpokenFeedbackController` synchronized, `TonePlayer` volatile.
- Manifest gate: build task rejects any permission besides `RECORD_AUDIO` and any microphone FGS.

### Remaining findings
- **`ignoreUnknownKeys = false` in CustomTuningJsonCodec** — any future schema addition breaks import for users on older app versions. Forward-compatible JSON parsing requires `true`. File: `tuning/CustomTuningJsonCodec.kt`.
- **`CustomTuningRepository` silent data loss** — `.takeIf { it.errors.isEmpty() }?.tunings ?: emptyList()` drops ALL custom tunings if any single validation error exists. No user-visible error. File: `settings/CustomTuningRepository.kt`.
- **`HapticFeedbackConstants.CONFIRM` requires API 30** — silently does nothing on API 26-29. `minSdk` is 26. File: `ui/TunerScreen.kt:87,128`.
- **`PhaseRefiner` dead constructor parameter** — `sampleRate` field is declared but never used; `refine()` takes `sampleRate` as a parameter instead. File: `pitch/PhaseRefiner.kt`.
- **`deviceTypeLabel()` and `audioSourceLabel()` return English-only strings** — not in string resources, visible in UI. File: `audio/AudioCaptureController.kt:420-428,454-461`.
- **StrobeMeter: zero accessibility semantics** — no `contentDescription`, `stateDescription`, `liveRegion`, or `progressBarRangeInfo`. TalkBack users see nothing in strobe mode. File: `ui/TunerMeterPanel.kt:270-345`.
- **PitchHistoryTimeline: no accessibility annotation** — Canvas has no semantics. File: `ui/TunerMeterPanel.kt:350-410`.
- **FullscreenTunerView: no `liveRegion`** — TalkBack won't announce note/direction updates. File: `ui/FullscreenTunerView.kt`.
- **Session summary card: 3 untranslated strings** — `session_complete`, `session_cents_value`, `session_dismiss` missing from all 7 non-English locales.
- **`TunerScreen` 28-parameter lambda drill-through** — `TunerSettingsPanel` receives 28 callback parameters threaded through from `TunerRoute`. `onStretchModeToggle` logic duplicated between wide and narrow layout branches. File: `ui/TunerScreen.kt:240-252,316-328`.
- **No ViewModel** — `AudioCaptureController` + `TunerStateHolder` created via `remember` in `TunerRoute`. Not lifecycle-safe across process death or configuration changes. File: `MainActivity.kt`.
- **YIN difference function is O(N^2)** — `differenceFunction()` in `YinPitchDetector.kt:141-156` performs ~2.5M multiplications per 4096-sample frame. No FFT acceleration. Primary CPU bottleneck on low-end devices.

## Architecture Assessment

- **Module boundaries are clean and test-enforced**: `CoreBoundaryTest.kt` guards that `pitch/`, `tuning/`, `settings/` have zero Android-UI deps. This enables Wear OS reuse.
- **UI decomposition is well-structured**: TunerScreen, TunerChrome, TunerMeterPanel, TunerReadouts, TunerSettingsPanel, TunerModeSettings, TunerPreferenceSettings, TunerComponents, FullscreenTunerView, PrivacyScreen, SessionSummaryCard, TuningAccessibility. Main screen is ~445 lines — large but not unwieldy.
- **State management needs upgrade for v1.0**: `TunerRoute` in `MainActivity.kt` is ~340 lines doing DI, permission flow, SAF I/O, and preference plumbing. No ViewModel. `AudioCaptureController` created in `remember` loses state on process death. ViewModel extraction is the #1 architecture prerequisite for production quality.
- **`TunerScreen` God-composable**: owns stretch mode state (6+ `remember` vars), session results, fullscreen mode, haptic triggering, and auto-advance logic. Refactor target.
- **Test coverage (22 files, ~2,306 lines)**: Good breadth. Key gaps: no alternate-tuning WAV fixtures (Drop D, Open G, bass, ukulele), no instrumented UI tests, `TunerStateHolder.measureA4FromLive` untested edge cases, `GuidedTuningWalkthrough` stretch-and-settle drift tracking paths.
- **i18n (8 locales, ~164/167 strings translated)**: en/de/es/pt/fr/ja/ko/zh. 3 session summary strings untranslated. `deviceTypeLabel()`/`audioSourceLabel()` hardcoded English. Scientific pitch notation not localizable (no solfege path).
- **Build stack is current**: Gradle 9.5.1, AGP 9.2.1, Kotlin 2.3.21, Compose BOM 2026.05.01, DataStore 1.2.1, kotlinx-serialization-json 1.11.0. Dependency verification metadata present.

## Rejected Ideas

- **Metronome/chords/lessons/recorder** — retention bloat contradicting narrow-tuner philosophy. Source: Choona #26.
- **Polyphonic strum-check** — YIN is single-f0; multi-f0 needs a different engine. Source: TC Electronic PolyTune.
- **Neural pitch detection (CREPE/PESTO)** — adds ONNX/TFLite 2-5 MB to a zero-dependency DSP app; gains only in noisy rooms. Reconsider if classical hardening proves insufficient. Source: CREPE paper, SwiftF0 repo.
- **Oboe/AAudio native capture** — latency dominated by 85-186 ms analysis window, not capture path. Source: Oboe #1006.
- **Temperaments/stretch-tuning suites** — Moekadu and billthefarmer own this niche; cognitive load contradicts beginner-first. Source: billthefarmer #68.
- **Background/FGS microphone capture** — Android 14+ while-in-use restrictions plus privacy promise. Source: Tunerly #38.
- **Home-screen widget tuner** — Glance renders to RemoteViews with no mic access. Source: Android Glance docs.
- **Cloud sync/accounts** — breaks the no-network contract. Source: product philosophy.
- **Dual Play/FOSS flavors** — nothing proprietary bundled; single FOSS build stays simpler. Source: Choona v1.5.1.
- **Animated needle/gauge meter** — adds Compose animation complexity for aesthetic preference; current tick-based meter is cleaner and more accessible. Source: Moekadu #102 mixed demand.
- **In-app pitch pipe** — reference tone playback already covers this use case with per-string tones. Source: internal analysis.
- **Auto-rotate lock** — Android system handles this; forcing orientation is a UX anti-pattern for tuners used at varying angles. Source: r/guitar complaints about GuitarTuna.

## Sources

OSS competitors and issues:
- https://codeberg.org/thetwom/Tuner (Moekadu issues 88, 72, 114, 102, 103, 100, 65)
- https://github.com/rohankhayech/Choona (issues 78, 74, 72, 26; SECURITY.md)
- https://github.com/billthefarmer/tuner (issues 65, 68)
- https://github.com/brianhorn/Tunerly/issues (38, 36, 41)
- https://github.com/gstraube/cythara
- https://github.com/DonBraulio/tuneo
- https://f-droid.org/en/packages/de.moekadu.tuner/

Commercial / community:
- https://apps.apple.com/us/app/guitartuna-guitar-bass-tuner/id527588389
- https://www.petersontuners.com/products/istrobosoft/
- https://www.tcelectronic.com/product.html?modelCode=0800-00010
- https://www.reddit.com/r/guitar/search?q=tuner+app
- https://community.justinguitar.com/t/app-vs-clip-on-tuner/412558
- https://news.ycombinator.com/item?id=28802306
- https://applevis.com/forum (blind musician tuner threads)

DSP / platform:
- https://29a.ch/2020/04/15/guitar-tuner (web YIN)
- https://github.com/dsego/strobe-tuner (strobe phase-comparator)
- https://qmro.qmul.ac.uk/xmlui/bitstream/handle/123456789/6040/MAUCHpYINFundamental2014Accepted.pdf (pYIN)
- https://developer.android.com/guide/topics/media/sharing-audio-input
- https://developer.android.com/develop/ui/views/haptics/haptics-apis
- https://developer.android.com/topic/performance/baselineprofiles
- https://developer.android.com/reference/android/speech/tts/TextToSpeech

Distribution / security:
- https://f-droid.org/docs/Reproducible_Builds/
- https://izzyondroid.org/docs/general/Fastlane/
- https://gitlab.com/AuroraOSS/aurorastore (privacy-first distribution)

## Open Questions

- Real-device behavior of `HapticFeedbackConstants.CONFIRM` on API 26-29 — does it throw, return false, or silently no-op? Determines whether a fallback is needed or just documentation.
- Whether Compose recomposition of the live meter drops frames on mid-range hardware at ~23 fps update rate — gates draw-phase optimization priority.
- R8 behavior with kotlinx-serialization under `ignoreUnknownKeys = true` — needs a minified release test with a custom tuning import containing unknown fields.
- F-Droid reproducible build feasibility with the current signing setup — their build server needs to produce identical APKs.
