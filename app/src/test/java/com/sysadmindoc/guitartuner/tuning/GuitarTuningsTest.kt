package com.sysadmindoc.guitartuner.tuning

import org.junit.Assert.assertEquals
import org.junit.Test

class GuitarTuningsTest {
    @Test
    fun catalogContainsBuiltInTuningsAndCustomTunings() {
        val custom = TuningDefinition(
            id = "custom",
            name = "Custom",
            strings = StandardGuitarTuning.strings,
            isBuiltIn = false,
        )
        val ids = GuitarTunings.catalog(listOf(custom)).tunings.map { it.id }

        assertEquals(
            listOf(
                GuitarTunings.StandardId,
                GuitarTunings.HalfStepDownId,
                GuitarTunings.DropDId,
                GuitarTunings.OpenGId,
                GuitarTunings.DadgadId,
                GuitarTunings.BassStandardId,
                GuitarTunings.UkuleleStandardId,
                "custom",
            ),
            ids,
        )
    }

    @Test
    fun builtInTuningsUseExpectedOpenStringTargets() {
        assertEquals(77.78, GuitarTunings.halfStepDown.string(6).frequencyHz, 0.001)
        assertEquals("Eb2", GuitarTunings.halfStepDown.string(6).scientificPitch)
        assertEquals(73.42, GuitarTunings.dropD.string(6).frequencyHz, 0.001)
        assertEquals(98.00, GuitarTunings.openG.string(5).frequencyHz, 0.001)
        assertEquals(220.00, GuitarTunings.dadgad.string(2).frequencyHz, 0.001)
    }

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

    private fun TuningDefinition.string(stringNumber: Int): GuitarString =
        strings.first { it.stringNumber == stringNumber }
}
