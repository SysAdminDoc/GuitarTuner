# Changelog

## GuitarTuner v0.0.5 - 2026-06-15

- Moved TalkBack accessibility descriptions to string resources so tuning state is announced in the device language (en/de/es) instead of hardcoded English.
- Derived the UI silence threshold from the detector's configured noise gate so the "no sound reaching mic" state and pitch detector silence gate agree at all user-chosen noise gate settings.
- Added auto-advance in Guided mode: after a string holds in-tune for 1.5 seconds, the walkthrough moves to the next string with optional haptic confirmation. Togglable in settings (default on).
- Eliminated per-frame heap allocation in the audio capture loop by using a pre-allocated analysis buffer instead of `copyOf()`.
- Added SECURITY.md with APK verification instructions and release artifact integrity guidance.
- Added fastlane `title.txt` for IzzyOnDroid metadata completeness.
- Enabled keep-screen-on during active listening so the phone does not dim or lock mid-tuning.
- Added Portuguese (Brazilian) and French localizations covering all UI and accessibility strings.
- Added synthetic noise-profile tests covering white noise, quiet ambient, impulse clicks, loud noise, and guitar-in-noise scenarios to verify the detector rejects non-musical signals.
- Added a scrolling pitch history timeline below the cents meter showing recent cents-over-time during active tuning.
- Added spoken tuning feedback using Android TextToSpeech: announces note, direction, and cents offset after each stable detection with 2-second debouncing. Localized for en/de/es/pt/fr. Works independently of TalkBack.

## GuitarTuner v0.0.4 - 2026-06-15

- Stabilized the listening controls so Stop listening and Full screen no longer jump as live pitch detection updates the tuner panel.
- Preserved string-break overshoot warnings through stable-frame smoothing so dangerous sharp readings cannot degrade into ordinary tune-down guidance.
- Respected analyzer-classified in-tune states during smoothing so relaxed cents tolerance settings remain authoritative.
- Reset freeze-last-note state when listening stops or tuning/analyzer context changes, preventing stale measurements from reappearing after mode, tuning, or calibration changes.
- Hardened reference tone playback against invalid frequencies, failed `AudioTrack` initialization, and incomplete static-buffer writes.
- Recovered custom tunings and tuner preferences to empty/default state after DataStore read failures or corruption instead of breaking startup flows.
- Made settings toggle rows fully tappable with checkbox semantics and removed dead session/error and string resources from earlier UI revisions.

## GuitarTuner v0.0.3 - 2026-06-15

- Hardened microphone capture start/stop handling so canceled capture loops cannot attach to a newer job, stale recorders are stopped promptly, and cancellation does not surface as a false microphone failure.
- Fixed phase refinement to use the recorder's actual sample rate, with fallback 44.1 kHz regression coverage for devices that do not capture at 48 kHz.
- Rejected custom tuning imports that collide with any built-in tuning id, not only Standard, and kept the README custom tuning example aligned with that rule.
- Added defensive tuning-analysis guards for invalid, non-finite, and empty-string-set pitch paths so malformed estimates settle into a no-string state instead of reaching logarithm/nearest-string assumptions.
- Sanitized persisted peg-turn direction data by dropping malformed entries and impossible string numbers before settings are re-encoded.
- Bounded custom tuning import reads while streaming from Android document URIs and added a specific file-too-large feedback state instead of a generic read error.
- Aligned microphone permission button labels and tuning guidance with the actual permanent-denial state, so retryable denials do not misleadingly say to open Android settings.
- Added Back handling for Privacy and Full screen modes plus screen-reader progress semantics for the microphone input meter.
- Hardened release signing behavior by ignoring blank signing env vars in Gradle and avoiding direct secret conditionals in the GitHub Actions decode step.

## GuitarTuner v0.0.2 - 2026-06-15

- Split the tuner UI into focused Compose modules for screen chrome, meter panel, live readouts, settings sections, fullscreen mode, privacy, and state-holder logic.
- Added a premium AMOLED tuner surface with a tick-based cents meter, compact frequency/cents/confidence/input readouts, clearer target hierarchy, and an always-visible local-audio trust signal.
- Refined settings into independent scan-friendly sections with calmer selected states, consistent rectangular 8 dp controls, safer long-label wrapping, and stronger touch-target consistency.
- Removed the duplicate active listening stop action by replacing the old Pause/Stop pair with one clear Stop listening action plus Full screen.
- Replaced corrupted fullscreen tuning symbols with localized text states, safer overshoot wording, and a tap-to-return hint.
- Improved privacy screen spacing and screen-reader grouping while keeping the app's microphone-only, no-network privacy model explicit.
- Added `.kotlin/` to `.gitignore` so Gradle/Kotlin build caches stay out of source control.

## GuitarTuner v0.0.1 - 2026-06-12

- Created local repository planning baseline.
- Added scaffold roadmap for an offline, open-source acoustic guitar tuner for Android.
- Captured initial product direction: microphone pitch detection, guided acoustic guitar tuning, privacy-first permissions, and configurable peg-direction guidance.
- Scaffolded the Android app with Gradle 9.5.1, AGP 9.2.1, Kotlin/Compose, Material 3, minSdk 26, release shrinking, and launcher placeholders.
- Added an in-project YIN pitch detector, standard six-string tuning model, lifecycle-bound `AudioRecord` capture controller, and Compose tuner screen.
- Added a merged-manifest gate that fails the build if any Android permission other than `RECORD_AUDIO` or any microphone foreground service is declared.
- Added JVM tests for standard strings, harmonic-rich low E, noisy low E, and G3/G4 octave-regression coverage.
- Added live tuning meter accessibility descriptions with stable cents buckets for TalkBack.
- Added persisted startup tuning behavior for standard, last-used, and favorite tuning modes.
- Added an in-app privacy screen and README Play Data Safety draft for local-only microphone processing.
- Added custom tuning JSON import/export with validation and persisted custom tuning selection.
- Added localization-ready tuner strings with initial Spanish and German resources.
- Added env-driven release signing config and a tagged GitHub Actions release workflow with SHA-256 checksums.
- Added adaptive single-column and wide two-column tuner layouts with phone, split-screen, tablet, and foldable Compose previews.
- Added an optional freeze-last-note setting that keeps the last stable measurement visible after note decay.
- Added a JVM boundary test that keeps core pitch, tuning, and settings packages free of phone UI dependencies for later Wear reuse.
- Added an explicit high-noise audio state alongside silence, clipping, permission denial, and no-string feedback.
- Verified debug microphone capture startup on a connected Samsung SM-S938B with adb install, `RECORD_AUDIO` grant, foreground launch, active Pause/Stop UI, and an empty crash buffer.
- Added stable-frame smoothing after pluck attack to reduce jumpy cents/frequency feedback without smearing target-string changes.
- Added second-harmonic octave correction so weak-fundamental guitar strings map back to the intended open string instead of a nearby higher string.
- Added an explicit `PitchResult` contract exposing frequency, nearest note, cents offset, confidence, and signal state.
- Added Guided and Auto tuning modes, including a guided string selector that locks analysis to one target string and an auto path that keeps nearest-string detection.
- Added a persisted A4 calibration setting with a 440 Hz default and per-Hz stepper controls that scale built-in and custom tuning targets.
- Added confidence to the main tuner readout alongside note, string, frequency, and cents feedback.
- Added a step-by-step Guided walkthrough from low E to high E with Previous/Next controls and highlighted target strings.
- Added per-string peg-direction calibration so left/right turn guidance appears only after the selected string has a tune-up direction.
- Added explicit empty, permission-required, high-noise, clipped-input, and no-string-detected guidance states.
- Added built-in Half-step down, Drop D, Open G, and DADGAD tunings alongside Standard and existing custom tuning import/export.
- Added a persisted cents tolerance setting that updates the analyzer's in-tune window.
- Added a persisted noise gate setting that updates microphone sensitivity for the pitch detector.
- Added a persisted System/Dark/Light theme setting backed by the existing AMOLED dark and light color schemes.
- Added a first-screen privacy note that states microphone audio never leaves the device.
- Added a local standard-guitar WAV fixture set and golden target regression tests for pitch and tuning results.
- Added README screenshots captured from the Android app running on a physical Samsung SM-S938B.
- Expanded signed release build documentation with local keystore, GitHub secret, APK/AAB, and checksum commands.
- Prepared the GitHub release workflow to publish or update a tagged release once a remote and signing secrets exist.
- Refined the tuner UI with first-screen listening actions, guided target empty states, sectioned settings, accessible selectable controls, stronger privacy copy, fuller theme tokens, and refreshed physical-device screenshots.
- Fixed real-use detection startup by making Auto mode the default, lowering the default microphone noise gate, preferring the reliable microphone source, using the recorder's actual sample rate, and adding quiet-input pitch regression coverage.
- Hardened live string detection with overlapping microphone analysis windows, a YIN autocorrelation fallback, a more sensitive default gate, explicit AudioRecord read errors, live input-level feedback, and live-sized WAV fixture regression coverage.
- Added raw microphone RMS/peak diagnostics, Android input source/sample-rate visibility, real-time performance source fallback, clearer silent-input guidance, launcher branding from the new logo, and GitHub-ready repository copy.
