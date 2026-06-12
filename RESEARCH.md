# Research - GuitarTuner

## Executive Summary
GuitarTuner is a v0.0.1 planning-only Android repository for an offline acoustic guitar tuner; the repo currently has no Android source, manifests, build files, or dependencies, and its strongest current shape is a clear privacy-first product constraint in `README.md`, `ROADMAP.md`, and ignored `CLAUDE.md`. Verified highest-value direction: build a narrow, reliable, beginner-guided standard guitar tuner before adding lesson-suite features. Top opportunities: decide the audio/DSP dependency path before scaffold; enforce no-network and microphone-only permissions; design `AudioRecord` capture for visible use only; validate low-E and octave-jump behavior with fixtures; split DSP/model/UI boundaries early; add privacy/Data Safety docs; make live meter feedback accessible; add last-used/favorite tunings; add custom tuning import/export; prepare reproducible signed APK distribution.

## Product Map
- Core workflows: grant microphone, select guided or auto mode, strum one open string, read tune-up/tune-down feedback, stop when in tolerance.
- User personas: beginner acoustic player who needs safe guidance; intermediate player using alternate tunings; privacy-conscious Android/F-Droid user; maintainer validating DSP accuracy.
- Platforms and distribution: native Kotlin/Compose Android app, minSdk 26, planned signed APK/AAB, likely Google Play plus F-Droid/IzzyOnDroid if reproducible builds are kept clean.
- Key integrations and data flows: Android microphone to `AudioRecord`, PCM frames to pitch engine, pitch result to tuning model, UI state to Compose screens, local DataStore/Room only if custom tunings need persistence.

## Competitive Landscape
- Choona: Verified Android/Wear OS OSS guitar tuner with automatic string detection, 20+ tunings, custom tunings, chromatic mode, favorites, true dark mode, large-screen support, Play/GitHub/Izzy distribution, and recent v1.6.1 release. Learn from its shared logic module, tuning editor, startup default tuning, Wear-ready separation, and release verification; avoid starting broad with Wear/12-string support before standard acoustic reliability is proven.
- Moekadu Tuner: Verified F-Droid/Play OSS tuner using autocorrelation plus FFT spectrum, phase/polynomial accuracy improvement, pitch history, custom instruments, and many temperaments. Learn from its configurable DSP and pitch-history confidence UI; avoid overwhelming beginner guitar users with temperament-heavy settings in the MVP.
- Bill Farmer Tuner: Verified mature Android tuner with strobe, scope, spectrum, multiple-note display, filters, custom temperaments, transposition, display lock, copy result, and detailed help. Learn from filter controls, hold/freeze behavior, signal status, and on-device help; avoid accordion/pro-tuner density on the first screen.
- Tunerly: Verified older Kotlin/TarsosDSP app with guitar/bass/ukulele/cuatro presets and multiple languages. Learn from broad preset coverage and localization; avoid relying on stale TarsosDSP Android microphone helpers without validating current Android support.
- Tunify: Verified small Kotlin app implementing YIN directly. Learn from keeping the DSP path in-project and auditable; avoid shipping without a test corpus and distribution polish.
- Rocksmith Tuner/BOSS/Fender/Simply Tune/GuitarTuna: Verified commercial apps prove user demand for free/ad-free guided tuning, chromatic/pro modes, 30+ presets, beginner visuals, and bundled learning tools. Learn guided string-by-string UX and clear free value; avoid subscription gates, ads, account requirements, and lesson bloat.
- Peterson/iStroboSoft/CarlTune/Pano Tuner: Verified pro/chromatic apps value precision, cents/Hz readouts, strobe/analog displays, display freeze, and calibration. Learn optional precision readouts and freeze-after-tone; avoid claiming 0.1-cent or strobe-level accuracy until validated against a known tuner.

## Security, Privacy, and Reliability
- Verified repo state risk: no Android manifest or build exists yet, so the no-network and microphone-only promise in `README.md`/`ROADMAP.md` is not enforceable until CI inspects the merged manifest.
- Verified Android privacy constraint: microphone is sensitive data, Android 12+ shows microphone indicators and can return silent audio when the global mic toggle is off; the app needs explicit silent-input and rationale states.
- Verified Android 14+ constraint: `RECORD_AUDIO` is while-in-use restricted for microphone foreground services; a simple tuner should keep capture visible/in-app instead of background microphone capture.
- Verified dependency risk: TarsosDSP 2.5 documents Java 11/core/JVM modules, while issue #213 reports Android microphone helpers removed and JVM module dependence on unavailable `javax.sound`; use TarsosDSP algorithms only after an Android proof or implement YIN/MPM in-project.
- Verified supply-chain risk: TarsosDSP 2.5 is GPL and published from a custom Maven repository, not Maven Central; MIT app licensing must either avoid GPL code/dependencies or intentionally switch project licensing before adding it.
- Verified reliability risk from competitors: thetwom/Tuner issue #88 reports G3/G4 octave jumps on guitar; low E and octave errors are known guitar tuner failure modes, so fixture tests are not optional.
- Verified release trust signal: Choona had an IzzyOnDroid reproducible-build failure caused by APK differences that looked like dirty build artifacts; GuitarTuner should build release artifacts from clean tagged commits.

## Architecture Assessment
- `README.md`, `ROADMAP.md`, and ignored `CLAUDE.md` all agree on Kotlin/Compose, minSdk 26, offline, microphone-only, standard guitar first; this is coherent and should stay narrow.
- No source modules exist; scaffold should create separate `audio`, `pitch`, `tuning`, `ui`, and `settings` packages or modules before UI code accretes around microphone callbacks.
- Pitch engine should expose deterministic data classes: frequency Hz, note, octave, target string, cents, confidence, signal level, clipping, silence, and stale-result state.
- Audio capture should use a lifecycle-bound foreground-visible controller, not a background service, unless a later feature proves a service is necessary.
- Test gaps are total: no Gradle project, no unit tests, no instrumented tests, no fixture corpus, no manifest-policy checks, no accessibility checks, and no release build verification.
- Documentation gaps: no RESEARCH prior to this file, no privacy policy text, no Data Safety answers, no dependency decision, no DSP validation method, no release signing path.

## Rejected Ideas
- Lessons, tabs, chord libraries, games, and metronomes: commercial apps use them for retention/paywalls, but they contradict the planned narrow offline tuner and should wait until the tuner is excellent.
- Background microphone tuning: Android foreground-service restrictions and privacy indicators make it unnecessary risk for a visible tuner.
- Cloud sync/accounts/multi-user: no current workflow needs identity or server storage; adding it would break the offline/no-network promise.
- Plugin ecosystem for tunings: custom tuning import/export covers the real need with less architecture and security risk.
- Polyphonic/all-strings-at-once tuning: interesting but harder to validate; the app goal is one strummed open string.
- Neural pitch models such as SwiftF0/TuneNN: promising but premature for an offline MIT Android MVP because classic YIN/MPM/autocorrelation are proven and easier to test.
- Wear OS first release: Choona shows value, but current repo has no phone app yet; keep architecture shareable and defer watch UI.
- Temperament-heavy pro mode: Moekadu/Bill Farmer cover this well; it adds cognitive load for beginner acoustic tuning.

## Sources
OSS:
- https://github.com/rohankhayech/Choona
- https://github.com/thetwom/Tuner
- https://f-droid.org/en/packages/de.moekadu.tuner/
- https://github.com/billthefarmer/tuner
- https://f-droid.org/en/packages/org.billthefarmer.tuner/
- https://github.com/brianhorn/Tunerly
- https://github.com/thestbar/tunify
- https://f-droid.org/en/packages/de.fff.ccgt/

Issues and release signals:
- https://github.com/thetwom/Tuner/issues/88
- https://github.com/thetwom/Tuner/issues/77
- https://github.com/billthefarmer/tuner/issues/65
- https://github.com/JorenSix/TarsosDSP/issues/213
- https://github.com/rohankhayech/Choona/issues/51

Commercial:
- https://play.google.com/store/apps/details?id=com.ovelin.guitartuna
- https://play.google.com/store/apps/details?id=com.fender.tuner
- https://www.petersontuners.com/shop/Mobile-App-Tuners/Popularity
- https://play.google.com/store/apps/details?id=com.joytunes.simplyguitar.tuner
- https://play.google.com/store/apps/details?id=com.ubisoft.rocksmith.connect.tuner.learn.bass.guitar.rock.lessons.tabs.chords
- https://play.google.com/store/apps/details?id=jp.co.roland.bosstuner
- https://play.google.com/store/apps/details?id=com.brainting.carltune

Platform and DSP:
- https://developer.android.com/reference/android/media/AudioRecord
- https://developer.android.com/develop/background-work/services/fgs/service-types
- https://developer.android.com/training/permissions/explaining-access
- https://support.google.com/googleplay/android-developer/answer/10144311
- https://github.com/JorenSix/TarsosDSP
- https://pubs.aip.org/asa/jasa/article/111/4/1917/547221/YIN-a-fundamental-frequency-estimator-for-speech
- https://www.cs.otago.ac.nz/graphics/Geoff/tartini/papers/A_Smarter_Way_to_Find_Pitch.pdf
- https://29a.ch/2020/04/15/guitar-tuner

Community:
- https://news.ycombinator.com/item?id=34200612
- https://www.reddit.com/r/Guitar/comments/186blxg/question_what_is_the_best_free_guitar_tuner_app/

## Open Questions
- None blocking current prioritization.
