package com.sysadmindoc.guitartuner.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class PegTurnDirectionTest {
    @Test
    fun encodesDirectionsInStringOrder() {
        val encoded = encodePegTurnDirections(
            mapOf(
                12 to PegTurnDirection.Left,
                1 to PegTurnDirection.Right,
                6 to PegTurnDirection.Left,
                0 to PegTurnDirection.Right,
            ),
        )

        assertEquals("6=Left;1=Right", encoded)
    }

    @Test
    fun decodesStoredDirectionsAndIgnoresInvalidEntries() {
        val decoded = decodePegTurnDirections("6=Left;bad=Right;7=Left;2=Missing;1=Right;3=Left=Right")

        assertEquals(
            mapOf(
                6 to PegTurnDirection.Left,
                1 to PegTurnDirection.Right,
            ),
            decoded,
        )
    }

    @Test
    fun reversesDirectionForTuneDownGuidance() {
        assertEquals(PegTurnDirection.Right, PegTurnDirection.Left.opposite())
        assertEquals(PegTurnDirection.Left, PegTurnDirection.Right.opposite())
    }
}
