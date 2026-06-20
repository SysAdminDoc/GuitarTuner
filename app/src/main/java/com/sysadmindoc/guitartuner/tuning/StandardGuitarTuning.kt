package com.sysadmindoc.guitartuner.tuning

import kotlin.math.pow

object StandardGuitarTuning {
    val strings: List<GuitarString> = listOf(
        GuitarString(stringNumber = 6, name = "Low E", scientificPitch = "E2", frequencyHz = 82.41),
        GuitarString(stringNumber = 5, name = "A", scientificPitch = "A2", frequencyHz = 110.00),
        GuitarString(stringNumber = 4, name = "D", scientificPitch = "D3", frequencyHz = 146.83),
        GuitarString(stringNumber = 3, name = "G", scientificPitch = "G3", frequencyHz = 196.00),
        GuitarString(stringNumber = 2, name = "B", scientificPitch = "B3", frequencyHz = 246.94),
        GuitarString(stringNumber = 1, name = "High E", scientificPitch = "E4", frequencyHz = 329.63),
    )
}

data class TuningDefinition(
    val id: String,
    val name: String,
    val strings: List<GuitarString>,
    val isBuiltIn: Boolean,
    val minFrequencyHz: Double = 70.0,
    val maxFrequencyHz: Double = 450.0,
)

object GuitarTunings {
    const val StandardId = "standard"
    const val HalfStepDownId = "half_step_down"
    const val DropDId = "drop_d"
    const val OpenGId = "open_g"
    const val DadgadId = "dadgad"
    const val BassStandardId = "bass_standard"
    const val UkuleleStandardId = "ukulele_standard"
    private const val ReferenceA4Hz = 440.0

    val standard: TuningDefinition = TuningDefinition(
        id = StandardId,
        name = "Standard",
        strings = StandardGuitarTuning.strings,
        isBuiltIn = true,
    )

    val halfStepDown: TuningDefinition = TuningDefinition(
        id = HalfStepDownId,
        name = "Half-step down",
        strings = listOf(
            GuitarString(stringNumber = 6, name = "Low Eb", scientificPitch = "Eb2", frequencyHz = 77.78),
            GuitarString(stringNumber = 5, name = "Ab", scientificPitch = "Ab2", frequencyHz = 103.83),
            GuitarString(stringNumber = 4, name = "Db", scientificPitch = "Db3", frequencyHz = 138.59),
            GuitarString(stringNumber = 3, name = "Gb", scientificPitch = "Gb3", frequencyHz = 185.00),
            GuitarString(stringNumber = 2, name = "Bb", scientificPitch = "Bb3", frequencyHz = 233.08),
            GuitarString(stringNumber = 1, name = "High Eb", scientificPitch = "Eb4", frequencyHz = 311.13),
        ),
        isBuiltIn = true,
    )

    val dropD: TuningDefinition = TuningDefinition(
        id = DropDId,
        name = "Drop D",
        strings = listOf(
            GuitarString(stringNumber = 6, name = "D", scientificPitch = "D2", frequencyHz = 73.42),
            GuitarString(stringNumber = 5, name = "A", scientificPitch = "A2", frequencyHz = 110.00),
            GuitarString(stringNumber = 4, name = "D", scientificPitch = "D3", frequencyHz = 146.83),
            GuitarString(stringNumber = 3, name = "G", scientificPitch = "G3", frequencyHz = 196.00),
            GuitarString(stringNumber = 2, name = "B", scientificPitch = "B3", frequencyHz = 246.94),
            GuitarString(stringNumber = 1, name = "High E", scientificPitch = "E4", frequencyHz = 329.63),
        ),
        isBuiltIn = true,
    )

    val openG: TuningDefinition = TuningDefinition(
        id = OpenGId,
        name = "Open G",
        strings = listOf(
            GuitarString(stringNumber = 6, name = "D", scientificPitch = "D2", frequencyHz = 73.42),
            GuitarString(stringNumber = 5, name = "G", scientificPitch = "G2", frequencyHz = 98.00),
            GuitarString(stringNumber = 4, name = "D", scientificPitch = "D3", frequencyHz = 146.83),
            GuitarString(stringNumber = 3, name = "G", scientificPitch = "G3", frequencyHz = 196.00),
            GuitarString(stringNumber = 2, name = "B", scientificPitch = "B3", frequencyHz = 246.94),
            GuitarString(stringNumber = 1, name = "D", scientificPitch = "D4", frequencyHz = 293.66),
        ),
        isBuiltIn = true,
    )

    val dadgad: TuningDefinition = TuningDefinition(
        id = DadgadId,
        name = "DADGAD",
        strings = listOf(
            GuitarString(stringNumber = 6, name = "D", scientificPitch = "D2", frequencyHz = 73.42),
            GuitarString(stringNumber = 5, name = "A", scientificPitch = "A2", frequencyHz = 110.00),
            GuitarString(stringNumber = 4, name = "D", scientificPitch = "D3", frequencyHz = 146.83),
            GuitarString(stringNumber = 3, name = "G", scientificPitch = "G3", frequencyHz = 196.00),
            GuitarString(stringNumber = 2, name = "A", scientificPitch = "A3", frequencyHz = 220.00),
            GuitarString(stringNumber = 1, name = "D", scientificPitch = "D4", frequencyHz = 293.66),
        ),
        isBuiltIn = true,
    )

    val bassStandard: TuningDefinition = TuningDefinition(
        id = BassStandardId,
        name = "Bass standard",
        strings = listOf(
            GuitarString(stringNumber = 4, name = "E", scientificPitch = "E1", frequencyHz = 41.20),
            GuitarString(stringNumber = 3, name = "A", scientificPitch = "A1", frequencyHz = 55.00),
            GuitarString(stringNumber = 2, name = "D", scientificPitch = "D2", frequencyHz = 73.42),
            GuitarString(stringNumber = 1, name = "G", scientificPitch = "G2", frequencyHz = 98.00),
        ),
        isBuiltIn = true,
        minFrequencyHz = 35.0,
        maxFrequencyHz = 200.0,
    )

    val ukuleleStandard: TuningDefinition = TuningDefinition(
        id = UkuleleStandardId,
        name = "Ukulele standard",
        strings = listOf(
            GuitarString(stringNumber = 4, name = "G", scientificPitch = "G4", frequencyHz = 392.00),
            GuitarString(stringNumber = 3, name = "C", scientificPitch = "C4", frequencyHz = 261.63),
            GuitarString(stringNumber = 2, name = "E", scientificPitch = "E4", frequencyHz = 329.63),
            GuitarString(stringNumber = 1, name = "A", scientificPitch = "A4", frequencyHz = 440.00),
        ),
        isBuiltIn = true,
        minFrequencyHz = 200.0,
        maxFrequencyHz = 520.0,
    )

    val builtIns: List<TuningDefinition> = listOf(
        standard,
        halfStepDown,
        dropD,
        openG,
        dadgad,
        bassStandard,
        ukuleleStandard,
    )

    fun catalog(
        customTunings: List<TuningDefinition>,
        a4Hz: Double = ReferenceA4Hz,
        capoFret: Int = 0,
    ): TuningCatalog =
        TuningCatalog(
            (builtIns + customTunings.filterNot { custom ->
                builtIns.any { builtIn -> builtIn.id == custom.id }
            }).map { tuning -> tuning.withA4Calibration(a4Hz).withCapo(capoFret) },
        )

    private fun TuningDefinition.withCapo(fret: Int): TuningDefinition {
        if (fret <= 0) return this
        val ratio = 2.0.pow(fret / 12.0)
        return copy(
            strings = strings.map { string ->
                string.copy(frequencyHz = string.frequencyHz * ratio)
            },
        )
    }

    private fun TuningDefinition.withA4Calibration(a4Hz: Double): TuningDefinition {
        val ratio = a4Hz / ReferenceA4Hz
        if (ratio == 1.0) return this
        return copy(
            strings = strings.map { string ->
                string.copy(frequencyHz = string.frequencyHz * ratio)
            },
        )
    }
}

data class TuningCatalog(
    val tunings: List<TuningDefinition>,
) {
    fun find(id: String): TuningDefinition =
        tunings.firstOrNull { it.id == id } ?: GuitarTunings.standard
}
