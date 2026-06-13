package com.sysadmindoc.guitartuner.tuning

import org.junit.Assert.assertEquals
import org.junit.Test

class GuitarTuningsTest {
    @Test
    fun catalogScalesBuiltInFrequenciesWithA4Calibration() {
        val standard = GuitarTunings.catalog(emptyList(), a4Hz = 442.0).find(GuitarTunings.StandardId)
        val lowE = standard.strings.first { it.scientificPitch == "E2" }

        assertEquals(82.41 * 442.0 / 440.0, lowE.frequencyHz, 0.0001)
    }

    @Test
    fun catalogScalesCustomFrequenciesWithA4Calibration() {
        val custom = TuningDefinition(
            id = "custom",
            name = "Custom",
            strings = listOf(
                GuitarString(stringNumber = 6, name = "D", scientificPitch = "D2", frequencyHz = 73.42),
            ),
            isBuiltIn = false,
        )

        val calibrated = GuitarTunings.catalog(listOf(custom), a4Hz = 438.0).find("custom")

        assertEquals(73.42 * 438.0 / 440.0, calibrated.strings.first().frequencyHz, 0.0001)
    }
}
