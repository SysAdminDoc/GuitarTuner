package com.sysadmindoc.guitartuner.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioInputLevelTest {
    @Test
    fun reportsSilentReadsAfterEnoughFrames() {
        var level = AudioInputLevel()

        repeat(20) {
            level = AudioInputLevel.fromPcmRead(
                samples = ShortArray(1024),
                length = 1024,
                previous = level,
                sourceLabel = "Mic",
                sampleRateHz = 44_100,
            )
        }

        assertTrue(level.isEffectivelySilent)
        assertEquals("Mic", level.sourceLabel)
        assertEquals(44_100, level.sampleRateHz)
    }

    @Test
    fun reportsPeakAndRmsForAudibleInput() {
        val samples = ShortArray(1024) { index ->
            if (index % 2 == 0) 16_384.toShort() else (-16_384).toShort()
        }

        val level = AudioInputLevel.fromPcmRead(
            samples = samples,
            length = samples.size,
            sourceLabel = "Voice performance",
            sampleRateHz = 48_000,
        )

        assertFalse(level.isEffectivelySilent)
        assertTrue(level.rms > 0.49)
        assertTrue(level.peak > 0.49)
        assertEquals(1L, level.readCount)
        assertEquals(samples.size, level.lastReadSize)
    }
}
