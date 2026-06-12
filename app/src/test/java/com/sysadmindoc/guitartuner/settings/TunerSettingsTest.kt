package com.sysadmindoc.guitartuner.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TunerSettingsTest {
    @Test
    fun defaultSettingsMatchStandardTunerBehavior() {
        val settings = TunerSettings()

        assertEquals(440.0, settings.calibration.a4Hz, 0.0)
        assertEquals(5.0, settings.centsTolerance, 0.0)
        assertEquals(0.008, settings.noiseGateRms, 0.0)
        assertEquals(ThemeMode.System, settings.themeMode)
    }

    @Test
    fun rejectsUnusableA4Calibration() {
        assertThrows(IllegalArgumentException::class.java) {
            PitchCalibration(a4Hz = 100.0)
        }
    }
}
