package com.sysadmindoc.guitartuner.settings

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
