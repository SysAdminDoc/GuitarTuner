package com.sysadmindoc.guitartuner.tuning

data class TuningTargetSelection(
    val mode: TuningMode = TuningMode.Auto,
    val guidedStringNumber: Int? = null,
) {
    fun selectedStrings(strings: List<GuitarString>): List<GuitarString> = when (mode) {
        TuningMode.Auto, TuningMode.Chromatic -> strings
        TuningMode.Guided -> strings.filter { it.stringNumber == guidedStringNumber }
            .ifEmpty { strings.take(1) }
    }

    companion object {
        fun auto(): TuningTargetSelection = TuningTargetSelection(mode = TuningMode.Auto)

        fun guided(stringNumber: Int): TuningTargetSelection = TuningTargetSelection(
            mode = TuningMode.Guided,
            guidedStringNumber = stringNumber,
        )

        fun chromatic(): TuningTargetSelection = TuningTargetSelection(mode = TuningMode.Chromatic)
    }
}

enum class TuningMode {
    Auto,
    Guided,
    Chromatic,
}
