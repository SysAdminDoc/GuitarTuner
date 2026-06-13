# Research — GuitarTuner

Updated 2026-06-12 against GuitarTuner v0.0.1 (full Android app: Kotlin/Compose, in-project YIN + autocorrelation fallback, Auto/Guided modes, custom tunings, DataStore settings, fixture regression tests, release workflow). This replaces the pre-scaffold research pass.

## Executive Summary

GuitarTuner is a working offline Android acoustic guitar tuner whose strongest shape is the empty market quadrant it already occupies: free + accurate + maintained + private. BOSS Tuner (the trusted free option) is abandoned since 2017, GuitarTuna/Fender gate tunings and precision behind subscriptions and ads, and the OSS field (Moekadu, Choona, billthefarmer) is accuracy-first but not beginner-first. The highest-value direction is to harden the capture/detection pipeline against the documented failure modes that destroy tuner trust (octave jumps, noisy-room jitter, silent capture loss), then ship the beginner-safety and accessibility features no competitor has. Top opportunities in priority order:

1. Fix verified reliability bugs: uncrashable tuning import/export, 48 kHz-first sample-rate negotiation, complete permission flow.
2. Property-gated `UNPROCESSED` audio source preference and explicit mic-stolen detection (Android 10+ concurrent capture zeros the loser silently).
3. Octave-jump hysteresis / pitch-candidate weighting — the single biggest complaint class across competitor issue trackers.
4. String-break overshoot warning — beginners snap high-E strings following tuners; no app warns; uniquely fits the guided beginner-first philosophy.
5. Chromatic mode + reference tone playback — the two table-stakes features every serious competitor has.
6. First-class non-visual tuning (TalkBack live regions, spoken state, haptics) — near-zero competition on Android; blind-musician communities have only a $0.99 iOS option.
7. Fastlane metadata → IzzyOnDroid → F-Droid distribution; reproducible release build.
8. Mic input device picker — independently requested in Choona #78 and Moekadu #65; nobody has shipped it.
9. Sub-cent precision via single-bin-DFT phase refinement, then an optional strobe view.
10. Defensive supply-chain hardening (dependency verification) befitting the no-network privacy promise.

## Product Map

- Core workflows: grant mic → Auto mode detects strummed open string → flat/sharp + tune-up/down guidance → stop in tolerance; Guided low-E-to-high-E walkthrough; custom tuning import/export (JSON); settings (A4, cents tolerance, noise gate, theme, peg direction, freeze-after-decay).
- Personas: beginner acoustic player needing safe guidance; intermediate player with alternate tunings; privacy-conscious F-Droid user; blind/low-vision guitarist (unserved on Android).
- Platforms/distribution: Android 8.0+ (minSdk 26, target 36), GitHub Releases workflow ready (signing secrets pending); IzzyOnDroid/F-Droid not yet submitted. `CoreBoundaryTest` keeps pitch/tuning/settings free of UI deps for later Wear reuse.
- Data flow: `AudioRecord` (44.1 kHz, 4096-sample frames, 2048 hop) → `YinPitchDetector` → `TuningAnalyzer` → `StableMeasurementSmoother` → `MeasurementFreeze` → `TunerSessionState` StateFlow → Compose. All local; manifest declares `RECORD_AUDIO` only, enforced by a merged-manifest Gradle gate.

## Competitive Landscape

- **Moekadu Tuner (thetwom, Codeberg, v9.1.1)** — best OSS detection stack: autocorrelation + FFT spectrum with phase/polynomial sub-bin refinement, pitch history, custom instruments/temperaments, stretch tuning (v9.0.0), measure-A4-from-live-source (v8.2.0). Learn: its issue tracker is a demand map — #88 G3/G4 octave jumps (17 comments), #72 noise jitter (34 comments), #114 wrong-string latch, #102 analog needle option, #103 distraction-free playing mode, #100 calibration in cents. Avoid: settings buried behind DSP jargon (#9), tiny touch targets (#35).
- **Choona (rohankhayech, v1.6.1)** — closest architectural sibling (Kotlin/Compose + Wear OS via shared `lib/` module). Learn: chromatic mode, tuning editor with direct note/octave entry, reference tone playback, semitone/cents toggle, dual Play/FOSS build flavors, fastlane + IzzyOnDroid distribution, SECURITY.md APK verification doc. Open demand: #78 mic device picker, #74 per-instrument detection range, #72 custom-tuning note ceiling too low. Avoid: hardcoded AudioRecord buffer sizes (its v1.4.2 crash fix).
- **billthefarmer/tuner** — pro-density reference: strobe display, 32+ custom temperaments, note/octave filters, display lock, transposition. Learn: filter and freeze behaviors; open #65 auto-freeze last reading, #68 stretch tuning. Avoid: its first-screen density for a beginner app.
- **GuitarTuna (Yousician)** — ~$2M/month paywall machine; community complaint corpus (ads before tuning, tunings paywalled) defines the trust gap GuitarTuner fills. Learn: beginner guided visuals and instant cold-start. Avoid: everything about its monetization.
- **BOSS Tuner / Simply Tune / Rocksmith Tuner** — free corporate tuners; BOSS abandoned (2017), Simply/Rocksmith are lead-gen funnels. Learn: BOSS accu-pitch beep on lock, Rocksmith string-by-string guided UX, "free with no strings attached" positioning. Avoid: account/telemetry baggage.
- **Peterson iStroboSoft / TC PolyTune (hardware UX)** — strobe = motion-proportional-to-cents legibility and a satisfying lock; PolyTune's strum-all-strings check is the most praised hardware innovation. Learn: full-screen big-glyph readability at arm's length (clip-on ergonomics), optional strobe precision view. Avoid: claiming 0.1-cent accuracy without validation.
- **Tunerly / cythara (orphaned OSS)** — cautionary tales: single-maintainer abandonment is the genre's killer; Tunerly's open bugs (#38 mic stays on in background, #36 loose in-tune tolerance, #41 wrong DADGAD preset note) are all trust failures GuitarTuner already avoids or must keep avoiding. Release cadence is itself a competitive feature.
- **HotPaw Talking Tuner (iOS, $0.99)** — the only tuner blind guitarists recommend (speaks "Note G is 15 cents flat"). No Android equivalent exists; AppleVis has recurring unsolved request threads. Learn: spoken-state mode that works without a screen reader.

## Security, Privacy, and Reliability

Verified findings (file paths in repo):

- **Import/export crash**: `MainActivity.kt:127-135` and `142-151` launch coroutines calling `readTextFromUri`/`writeTextToUri` (`MainActivity.kt:262-273`) which throw `IOException` (and can throw `SecurityException` from SAF) with no catch — an unreadable/revoked URI crashes the app.
- **Sample-rate rigidity**: `AudioCaptureController.kt:232-239` requests only 44.1 kHz and throws if unsupported; modern devices are natively 48 kHz, so capture always goes through the platform resampler, and a hypothetical 48k-only device fails outright. The detector already accepts the real rate (`analyzeFrame`, line 213-214), so negotiation is cheap.
- **Audio source order**: `AudioCaptureController.kt:242-251` tries VOICE_PERFORMANCE → VOICE_RECOGNITION → MIC → UNPROCESSED. UNPROCESSED (the documented tuner source, no AGC/NS) is last and is never gated by `AudioManager.getProperty(PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED)`; some devices return silence for unsupported UNPROCESSED, which is presumably why it was demoted — the property check plus a zero-frames fallback recovers the better source safely.
- **Mic-stolen blindness**: Android 10+ concurrent capture silences the losing app with zero-filled buffers, not an error. The app shows generic "no sound reaching the mic" guidance (`TunerScreen.kt` waiting state) but cannot distinguish another app holding the mic; users will blame the tuner.
- **Permission flow incomplete**: `MainActivity.kt:153-162,196-201` — no `shouldShowRequestPermissionRationale` handling and no settings deep-link after permanent denial ("deny twice" leaves a button that silently does nothing new); `notePermissionDenied` (`AudioCaptureController.kt:89-94`) also wipes session state and plants a hardcoded-English error that lingers after the user later grants permission in Settings.
- **Hardcoded user-facing strings**: `AudioCaptureController.kt:92,141,201,238,278,299-305` surface English error text directly into UI state while de/es resources exist (`values-de/`, `values-es/`) — i18n leak; should be error codes mapped to resources.
- **Silence-definition mismatch**: `TunerSessionState.kt:63` (`SilentRms = 0.0002`) vs `YinPitchDetector` default `silenceRms = 0.0015` — UI can claim the mic is silent while the detector still gates, and vice versa.
- **Octave/harmonic robustness is heuristic**: `TuningAnalyzer` applies a hard second-harmonic halving with a magic 0.75 floor factor; competitor evidence (Moekadu #88/#114) shows hard heuristics still produce G3/G4 flips and wrong-string latching under real signals. No hysteresis between consecutive octave-candidate frames.
- **Guardrails present and worth keeping**: mic released on ON_PAUSE (`MainActivity.kt:164-182`); merged-manifest permission gate in `app/build.gradle.kts`; DataStore reads sanitized with range coercion; no FGS, no network permission, MIT license, no GPL deps (in-project DSP avoids the TarsosDSP GPL trap).
- **Supply chain**: only `google()`/`mavenCentral()` in use (good), but no Gradle dependency verification metadata and no `FAIL_ON_PROJECT_REPOS`; MavenGate-class attacks are the realistic attack surface for a no-network app. Release is not yet reproducible (PNG crunching, R8 nondeterminism unaddressed) — relevant for F-Droid's own-signature path.

## Architecture Assessment

- Boundaries are clean and test-enforced (`CoreBoundaryTest.kt`): pitch/tuning/settings have zero Android-UI deps. Keep this; it is the Wear OS option.
- `TunerScreen.kt` is ~1300 lines / 50 KB holding layout, formatting, semantics, and previews — split into meter, guided-panel, settings-sections, and diagnostics files before more UI lands.
- `TunerRoute` in `MainActivity.kt` is a 200-line composable doing DI, permission flow, SAF I/O, and preference plumbing — extract a state holder (plain class or ViewModel) so the capture/permission logic becomes testable; today `AudioCaptureController`'s loop and `MainActivity` routing have zero tests.
- `setNoiseGateRms` (`AudioCaptureController.kt:111-113`) rebuilds the detector with *only* `silenceRms`, silently discarding any other future config — fragile constructor pattern.
- Per-frame `frameBuffer.copyOf()` allocation (`AudioCaptureController.kt:186`) churns ~86 KB/s of garbage on the audio path; reusable double-buffer is trivial.
- Live meter recomposition: cents/level values flow through `Text`/composable parameters each frame; Compose guidance is to defer high-rate reads to the draw phase (`Canvas` lambda reads) — needs live validation with Layout Inspector before optimizing.
- Test gaps: no capture-loop tests, no alternate-tuning fixtures (Drop D/Open G/DADGAD analyzers untested against audio), no noise-profile fixtures (chatter, white noise, impulse), no DataStore repository tests, no UI interaction tests.
- Docs: README is strong; missing fastlane metadata tree (required for IzzyOnDroid/F-Droid) and a SECURITY.md-style APK verification note (Choona precedent).

## Rejected Ideas

- Metronome/chords/lessons/recorder (Choona #26, Soundcorset model): retention bloat that contradicts the narrow-tuner philosophy; revisit never unless the tuner is fully mature.
- Polyphonic strum-check (PolyTune model): YIN is single-f0; multi-f0 needs a different engine with a large validation burden — keep as a far-future research note, not roadmap.
- Neural pitch detection (SwiftF0 ~96k params MIT/ONNX, PESTO 130k): viable on-device but adds an ONNX/TFLite runtime dependency (~2-5 MB) to a zero-dependency DSP app for gains that only matter in noisy rooms; reconsider only if classical noise hardening (P0 items) proves insufficient.
- Oboe/AAudio native capture: information latency is dominated by the 85-186 ms analysis window, not the capture path; NDK complexity buys nothing for a tuner.
- Temperaments/stretch-tuning suites: Moekadu and billthefarmer own this niche; cognitive load contradicts beginner-first. (Stretch tuning specifically: piano-domain, see billthefarmer #68.)
- Background/FGS microphone capture or lock-screen tuner: Android 14+ while-in-use restrictions plus the privacy promise; Tunerly #38 shows users punish background mic use.
- Live tuner home-screen widget: Glance renders to RemoteViews with no mic access and throttled updates — infeasible; a launch-shortcut widget adds little.
- Cloud sync/accounts: breaks the no-network contract that is the product's identity.
- Dual Play/FOSS flavors (Choona v1.5.1 precedent): nothing proprietary is bundled, so there is nothing to split; single FOSS build stays simpler.

## Sources

OSS competitors and issues:
- https://github.com/rohankhayech/Choona (+ issues 78, 74, 72, 26; release v1.6.1)
- https://codeberg.org/thetwom/Tuner/releases
- https://github.com/thetwom/Tuner/issues/88
- https://github.com/thetwom/Tuner/issues/72
- https://github.com/thetwom/Tuner/issues/114
- https://github.com/billthefarmer/tuner (+ issues 65, 68)
- https://github.com/brianhorn/Tunerly/issues (38, 36, 41, 45)
- https://github.com/gstraube/cythara
- https://github.com/DonBraulio/tuneo
- https://f-droid.org/en/packages/de.moekadu.tuner/

Commercial / community:
- https://apps.apple.com/us/app/guitartuna-guitar-bass-tuner/id527588389
- https://apps.apple.com/us/app/boss-tuner/id1113473319
- https://www.ubisoft.com/en-us/game/rocksmith/plus/guitar-tuner
- https://americansongwriter.com/best-guitar-tuner-apps/
- https://news.ycombinator.com/item?id=28802306
- https://news.ycombinator.com/item?id=33150260
- https://community.justinguitar.com/t/app-vs-clip-on-tuner/412558
- https://steamcommunity.com/app/221680/discussions/0/358415738192883259
- https://stringjoy.com/how-high-tune-guitar-string-breaks/
- https://afb.org/aw/21/2/16906

DSP / platform:
- https://29a.ch/2020/04/15/guitar-tuner
- https://github.com/dsego/strobe-tuner
- https://github.com/sevagh/pitch-detection/issues/63
- https://qmro.qmul.ac.uk/xmlui/bitstream/handle/123456789/6040/MAUCHpYINFundamental2014Accepted.pdf (pYIN, Mauch & Dixon 2014)
- https://developer.android.com/guide/topics/media/sharing-audio-input
- https://github.com/google/oboe/issues/1006 (UNPROCESSED device quirks)
- https://developer.android.com/develop/ui/views/haptics/haptics-apis
- https://f-droid.org/docs/Reproducible_Builds/
- https://izzyondroid.org/docs/general/Fastlane/
- https://oversecured.com/blog/introducing-mavengate-a-supply-chain-attack-method-for-java-and-android-applications

## Open Questions

- Does the Samsung SM-S938B (the only QA device) support `UNPROCESSED` per the AudioManager property, and does it deliver non-silent frames on it? Needs live validation before reordering sources (P0 item is written to be safe either way).
- Real-device behavior of the mic-stolen scenario (second capture app foregrounding) on Android 14/15 — needed to tune the zeros-while-listening detector threshold/duration.
- Whether Compose recomposition of the live meter actually drops frames on mid-range hardware (Layout Inspector / macrobenchmark check) — gates the draw-phase optimization item.
