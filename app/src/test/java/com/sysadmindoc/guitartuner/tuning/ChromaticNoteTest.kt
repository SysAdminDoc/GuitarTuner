package com.sysadmindoc.guitartuner.tuning

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChromaticNoteTest {
    @Test
    fun resolvesA4Exactly() {
        val result = ChromaticNote.resolve(440.0)
        assertEquals("A4", result.scientificPitch)
        assertTrue(abs(result.cents) < 0.001)
    }

    @Test
    fun resolvesMiddleC() {
        val result = ChromaticNote.resolve(261.63)
        assertEquals("C4", result.scientificPitch)
        assertTrue(abs(result.cents) < 1.0)
    }

    @Test
    fun resolvesLowE() {
        val result = ChromaticNote.resolve(82.41)
        assertEquals("E2", result.scientificPitch)
        assertTrue(abs(result.cents) < 1.0)
    }

    @Test
    fun resolvesSharpNote() {
        val result = ChromaticNote.resolve(450.0)
        assertEquals("A4", result.scientificPitch)
        assertTrue(result.cents > 0)
    }

    @Test
    fun resolvesFlatNote() {
        val result = ChromaticNote.resolve(430.0)
        assertEquals("A4", result.scientificPitch)
        assertTrue(result.cents < 0)
    }

    @Test
    fun usesCustomA4Calibration() {
        val result = ChromaticNote.resolve(442.0, a4Hz = 442.0)
        assertEquals("A4", result.scientificPitch)
        assertTrue(abs(result.cents) < 0.001)
    }
}
