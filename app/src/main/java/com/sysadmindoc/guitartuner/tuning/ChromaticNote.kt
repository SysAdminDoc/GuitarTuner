package com.sysadmindoc.guitartuner.tuning

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

object ChromaticNote {
    private val NoteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun resolve(frequencyHz: Double, a4Hz: Double = 440.0): ChromaticResult {
        val semitonesFromA4 = CentsPerOctave / 100.0 * ln(frequencyHz / a4Hz) / Ln2
        val nearestSemitone = semitonesFromA4.roundToInt()
        val cents = (semitonesFromA4 - nearestSemitone) * 100.0
        val midiNote = 69 + nearestSemitone
        val octave = (midiNote / 12) - 1
        val noteIndex = ((midiNote % 12) + 12) % 12
        val noteName = NoteNames[noteIndex]
        val scientificPitch = "$noteName$octave"
        val targetFrequency = a4Hz * 2.0.pow(nearestSemitone / 12.0)
        return ChromaticResult(
            noteName = noteName,
            octave = octave,
            scientificPitch = scientificPitch,
            targetFrequencyHz = targetFrequency,
            cents = cents,
        )
    }

    private const val CentsPerOctave = 1200.0
    private val Ln2 = ln(2.0)
}

data class ChromaticResult(
    val noteName: String,
    val octave: Int,
    val scientificPitch: String,
    val targetFrequencyHz: Double,
    val cents: Double,
)
