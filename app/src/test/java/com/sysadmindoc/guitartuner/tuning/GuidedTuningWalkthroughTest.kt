package com.sysadmindoc.guitartuner.tuning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GuidedTuningWalkthroughTest {
    @Test
    fun startsAtLowEAndWalksToHighE() {
        val strings = StandardGuitarTuning.strings

        assertEquals("E2", guidedTuningStep(strings, 6)!!.string.scientificPitch)
        assertEquals("A2", guidedTuningStep(strings, nextGuidedStringNumber(strings, 6))!!.string.scientificPitch)
        assertEquals("E4", guidedTuningStep(strings, 1)!!.string.scientificPitch)
    }

    @Test
    fun clampsPreviousAndNextAtEnds() {
        val strings = StandardGuitarTuning.strings

        assertEquals(6, previousGuidedStringNumber(strings, 6))
        assertEquals(1, nextGuidedStringNumber(strings, 1))
    }

    @Test
    fun invalidSelectionFallsBackToFirstStep() {
        val step = guidedTuningStep(StandardGuitarTuning.strings, 99)

        assertNotNull(step)
        assertEquals(1, step!!.stepNumber)
        assertEquals("E2", step.string.scientificPitch)
    }

    @Test
    fun emptyStringsReturnsNull() {
        assertNull(guidedTuningStep(emptyList(), 1))
    }

    @Test
    fun emptyStringsPreservesSelectedStringNumber() {
        assertEquals(3, previousGuidedStringNumber(emptyList(), 3))
        assertEquals(3, nextGuidedStringNumber(emptyList(), 3))
    }
}
