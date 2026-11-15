# GuitarTuner Roadmap

Actionable work only. Historical and completed roadmap material is archived in CHANGELOG.md; blocked work is kept in Roadmap_Blocked.md.

- [ ] (P2) Baseline profile for cold start
  Why: a tuner is a 30-second utility, so cold start is most of the experience. Baseline profiles cut startup 15-40% in published case studies.
  Touches: new baselineprofile module (Macrobenchmark generator), app/build.gradle.kts.
  Acceptance: profile generated on a physical phone and bundled in the release; a macrobenchmark shows a measurable cold-start improvement.
  Complexity: S

- [ ] (P3) Fullscreen sizing on small phones and tablets
  Why: v0.2.0 caps fullscreen text by height as well as width, but it hasn't been looked at on a small phone or a tablet.
  Where: ui/FullscreenTunerView.kt.
  Acceptance: no clipping or overflow on a 360dp phone and a tablet-sized window.
  Complexity: S

- [ ] (P3) Plain-voice pass on in-app strings
  Why: the v0.2.0 device check showed status and hint strings stitched with semicolons and a spaced hyphen ("Mic open; no sound reaching it", "Strum close to the phone; if Mic stays 0, check Android", "Auto detects the nearest string; Guided walks one string at a time; Chromatic shows any note.", "Mic source - 48000 Hz").
  Touches: app/src/main/res/values*/strings.xml (all 8 locales), any string tests.
  Acceptance: no semicolon-stitched clauses or spaced hyphens in user-visible strings in any locale; tests still pass.
  Complexity: S

- [ ] (P3) Font scaling to 200%
  Why: Android lets users scale fonts system-wide up to 200%. The fixed `fontSize = 92.sp` in fullscreen and the compact readout metrics may clip at large scales.
  Touches: `FullscreenTunerView.kt`, `TunerReadouts.kt`, `TunerMeterPanel.kt`.
  Acceptance: all text readable and unclipped at font scale 200% on a 360dp phone.
  Complexity: S
