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
        assertEquals(0.003, settings.noiseGateRms, 0.0)
        assertEquals(ThemeMode.System, settings.themeMode)
    }

    @Test
    fun storedPreferencesDefaultToConcertA() {
        val preferences = StoredTunerPreferences()

        assertEquals(440.0, preferences.a4Hz, 0.0)
        assertEquals(5.0, preferences.centsTolerance, 0.0)
        assertEquals(0.003, preferences.noiseGateRms, 0.0)
        assertEquals(ThemeMode.System, preferences.themeMode)
    }

    @Test
    fun rejectsUnusableA4Calibration() {
        assertThrows(IllegalArgumentException::class.java) {
            PitchCalibration(a4Hz = 100.0)
        }
    }

    @Test
    fun storedPreferencesRejectUnusableA4Calibration() {
        assertThrows(IllegalArgumentException::class.java) {
            StoredTunerPreferences(a4Hz = 100.0)
        }
    }

    @Test
    fun rejectsUnusableCentsTolerance() {
        assertThrows(IllegalArgumentException::class.java) {
            TunerSettings(centsTolerance = 0.5)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StoredTunerPreferences(centsTolerance = 40.0)
        }
    }

    @Test
    fun rejectsUnusableNoiseGate() {
        assertThrows(IllegalArgumentException::class.java) {
            TunerSettings(noiseGateRms = 0.001)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StoredTunerPreferences(noiseGateRms = 0.050)
        }
    }
}
