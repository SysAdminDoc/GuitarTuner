package com.sysadmindoc.guitartuner.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TonePlayerTest {
    @Test
    fun playableReferenceToneFrequencyRequiresFinitePracticalAudioRange() {
        assertTrue(isPlayableReferenceToneFrequency(82.41))
        assertTrue(isPlayableReferenceToneFrequency(440.0))
        assertTrue(isPlayableReferenceToneFrequency(5_000.0))

        assertFalse(isPlayableReferenceToneFrequency(19.99))
        assertFalse(isPlayableReferenceToneFrequency(5_000.1))
        assertFalse(isPlayableReferenceToneFrequency(Double.NaN))
        assertFalse(isPlayableReferenceToneFrequency(Double.POSITIVE_INFINITY))
        assertFalse(isPlayableReferenceToneFrequency(Double.NEGATIVE_INFINITY))
    }
}
