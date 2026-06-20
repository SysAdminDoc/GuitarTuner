package com.sysadmindoc.guitartuner.settings

import com.sysadmindoc.guitartuner.tuning.GuitarTunings

data class TunerSettings(
    val calibration: PitchCalibration = PitchCalibration(),
    val centsTolerance: Double = 5.0,
    val noiseGateRms: Double = 0.0015,
    val themeMode: ThemeMode = ThemeMode.System,
) {
    init {
        require(centsTolerance in 1.0..25.0) { "Cents tolerance must stay within 1 and 25 cents." }
        require(noiseGateRms in 0.001..0.030) { "Noise gate must stay within 0.001 and 0.030 RMS." }
    }
}

data class PitchCalibration(
    val a4Hz: Double = 440.0,
) {
    init {
        require(a4Hz in 400.0..480.0) { "A4 calibration must stay within a practical tuner range." }
    }
}

enum class ThemeMode {
    System,
    Dark,
    Light,
}

data class StoredTunerPreferences(
    val startupMode: StartupTuningMode = StartupTuningMode.StandardDefault,
    val lastUsedTuningId: String = GuitarTunings.StandardId,
    val favoriteTuningId: String = GuitarTunings.StandardId,
    val themeMode: ThemeMode = ThemeMode.System,
    val freezeAfterDecay: Boolean = false,
    val hapticEnabled: Boolean = false,
    val autoAdvanceGuided: Boolean = true,
    val spokenFeedback: Boolean = false,
    val meterStyle: MeterStyle = MeterStyle.Normal,
    val a4Hz: Double = 440.0,
    val centsTolerance: Double = 5.0,
    val noiseGateRms: Double = 0.0015,
    val pegTurnDirections: Map<Int, PegTurnDirection> = emptyMap(),
    val leftHanded: Boolean = false,
) {
    init {
        PitchCalibration(a4Hz)
        require(centsTolerance in 1.0..25.0) { "Cents tolerance must stay within 1 and 25 cents." }
        require(noiseGateRms in 0.001..0.030) { "Noise gate must stay within 0.001 and 0.030 RMS." }
    }

    fun startupTuningId(): String = when (startupMode) {
        StartupTuningMode.StandardDefault -> GuitarTunings.StandardId
        StartupTuningMode.LastUsed -> lastUsedTuningId
        StartupTuningMode.Favorite -> favoriteTuningId
    }
}

enum class StartupTuningMode {
    StandardDefault,
    LastUsed,
    Favorite,
}

enum class MeterStyle {
    Normal,
    Strobe,
}
