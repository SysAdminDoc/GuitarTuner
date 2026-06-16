package com.sysadmindoc.guitartuner.tuning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomTuningJsonCodecTest {
    @Test
    fun decodesValidCustomTuning() {
        val result = CustomTuningJsonCodec.decode(validOpenGJson())

        assertTrue(result.errors.toString(), result.errors.isEmpty())
        assertEquals(1, result.tunings.size)
        val tuning = result.tunings.single()
        assertEquals("custom_open_g", tuning.id)
        assertEquals("Open G", tuning.name)
        assertEquals(false, tuning.isBuiltIn)
        assertEquals(listOf(6, 5, 4, 3, 2, 1), tuning.strings.map { it.stringNumber })
    }

    @Test
    fun rejectsBuiltInIdAndInvalidFrequency() {
        val result = CustomTuningJsonCodec.decode(
            validOpenGJson()
                .replace("\"custom_open_g\"", "\"standard\"")
                .replace("73.42", "10.0"),
        )

        assertTrue(result.tunings.isEmpty())
        assertTrue(result.errors.any { it.contains("cannot replace") })
        assertTrue(result.errors.any { it.contains("frequencyHz") })
    }

    @Test
    fun rejectsAnyBuiltInTuningIdCollision() {
        val result = CustomTuningJsonCodec.decode(
            validOpenGJson().replace("\"custom_open_g\"", "\"drop_d\""),
        )

        assertTrue(result.tunings.isEmpty())
        assertTrue(result.errors.any { it.contains("cannot replace a built-in tuning") })
    }

    @Test
    fun decodesCustomFourStringTuning() {
        val json = """
            {
              "schemaVersion": 1,
              "tunings": [
                {
                  "id": "custom_uke_low_g",
                  "name": "Ukulele Low G",
                  "strings": [
                    { "stringNumber": 4, "name": "G", "note": "G3", "frequencyHz": 196.00 },
                    { "stringNumber": 3, "name": "C", "note": "C4", "frequencyHz": 261.63 },
                    { "stringNumber": 2, "name": "E", "note": "E4", "frequencyHz": 329.63 },
                    { "stringNumber": 1, "name": "A", "note": "A4", "frequencyHz": 440.00 }
                  ]
                }
              ]
            }
        """.trimIndent()

        val result = CustomTuningJsonCodec.decode(json)

        assertTrue(result.errors.toString(), result.errors.isEmpty())
        assertEquals(1, result.tunings.size)
        assertEquals(4, result.tunings.single().strings.size)
        assertEquals("Ukulele Low G", result.tunings.single().name)
    }

    @Test
    fun roundTripsCustomTuningFile() {
        val decoded = CustomTuningJsonCodec.decode(validOpenGJson()).tunings
        val encoded = CustomTuningJsonCodec.encode(decoded)
        val roundTripped = CustomTuningJsonCodec.decode(encoded)

        assertTrue(roundTripped.errors.toString(), roundTripped.errors.isEmpty())
        assertEquals(decoded, roundTripped.tunings)
    }

    private fun validOpenGJson(): String = """
        {
          "schemaVersion": 1,
          "tunings": [
            {
              "id": "custom_open_g",
              "name": "Open G",
              "strings": [
                { "stringNumber": 6, "name": "D", "note": "D2", "frequencyHz": 73.42 },
                { "stringNumber": 5, "name": "G", "note": "G2", "frequencyHz": 98.00 },
                { "stringNumber": 4, "name": "D", "note": "D3", "frequencyHz": 146.83 },
                { "stringNumber": 3, "name": "G", "note": "G3", "frequencyHz": 196.00 },
                { "stringNumber": 2, "name": "B", "note": "B3", "frequencyHz": 246.94 },
                { "stringNumber": 1, "name": "D", "note": "D4", "frequencyHz": 293.66 }
              ]
            }
          ]
        }
    """.trimIndent()
}
