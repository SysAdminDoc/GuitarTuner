package com.sysadmindoc.guitartuner.settings

import com.sysadmindoc.guitartuner.tuning.GuitarTunings

data class TunerSettings(
    val calibration: PitchCalibration = PitchCalibration(),
    val centsTolerance: Double = 5.0,
    val noiseGateRms: Double = 0.008,
    val themeMode: ThemeMode = ThemeMode.System,
)

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
) {
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
