package com.sysadmindoc.guitartuner.tuning

data class GuitarString(
    val stringNumber: Int,
    val name: String,
    val scientificPitch: String,
    val frequencyHz: Double,
) {
    init {
        require(frequencyHz.isFinite() && frequencyHz > 0.0) {
            "GuitarString frequency must be a positive finite value."
        }
    }

    val displayName: String = "$stringNumber - $scientificPitch"

    val solfegePitch: String
        get() {
            val octaveSuffix = scientificPitch.takeLastWhile { it.isDigit() }
            val notePart = scientificPitch.removeSuffix(octaveSuffix)
            val solfege = SolfegeMap[notePart] ?: notePart
            return "$solfege$octaveSuffix"
        }

    companion object {
        private val SolfegeMap = mapOf(
            "C" to "Do", "C#" to "Do#", "Cb" to "Dob",
            "D" to "Re", "D#" to "Re#", "Db" to "Reb",
            "E" to "Mi", "Eb" to "Mib",
            "F" to "Fa", "F#" to "Fa#",
            "G" to "Sol", "G#" to "Sol#", "Gb" to "Solb",
            "A" to "La", "A#" to "La#", "Ab" to "Lab",
            "B" to "Si", "Bb" to "Sib",
        )
    }
}
