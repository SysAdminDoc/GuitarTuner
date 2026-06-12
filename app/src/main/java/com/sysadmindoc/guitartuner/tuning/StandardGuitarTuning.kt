package com.sysadmindoc.guitartuner.tuning

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
)

object GuitarTunings {
    const val StandardId = "standard"

    val standard: TuningDefinition = TuningDefinition(
        id = StandardId,
        name = "Standard",
        strings = StandardGuitarTuning.strings,
        isBuiltIn = true,
    )

    val builtIns: List<TuningDefinition> = listOf(standard)

    fun catalog(customTunings: List<TuningDefinition>): TuningCatalog =
        TuningCatalog(
            builtIns + customTunings.filterNot { custom ->
                builtIns.any { builtIn -> builtIn.id == custom.id }
            },
        )
}

data class TuningCatalog(
    val tunings: List<TuningDefinition>,
) {
    fun find(id: String): TuningDefinition =
        tunings.firstOrNull { it.id == id } ?: GuitarTunings.standard
}
