package com.sysadmindoc.guitartuner.tuning

data class GuidedTuningStep(
    val index: Int,
    val total: Int,
    val string: GuitarString,
) {
    val stepNumber: Int = index + 1
}

fun guidedTuningStep(
    strings: List<GuitarString>,
    selectedStringNumber: Int,
): GuidedTuningStep? {
    val ordered = strings.guidedOrder()
    if (ordered.isEmpty()) return null
    val index = ordered.indexOfFirst { it.stringNumber == selectedStringNumber }
        .takeIf { it >= 0 }
        ?: 0
    return GuidedTuningStep(
        index = index,
        total = ordered.size,
        string = ordered[index],
    )
}

fun previousGuidedStringNumber(
    strings: List<GuitarString>,
    selectedStringNumber: Int,
): Int {
    val step = guidedTuningStep(strings, selectedStringNumber) ?: return selectedStringNumber
    val previousIndex = (step.index - 1).coerceAtLeast(0)
    return strings.guidedOrder()[previousIndex].stringNumber
}

fun nextGuidedStringNumber(
    strings: List<GuitarString>,
    selectedStringNumber: Int,
): Int {
    val step = guidedTuningStep(strings, selectedStringNumber) ?: return selectedStringNumber
    val nextIndex = (step.index + 1).coerceAtMost(step.total - 1)
    return strings.guidedOrder()[nextIndex].stringNumber
}

internal fun List<GuitarString>.guidedOrder(): List<GuitarString> =
    sortedByDescending { it.stringNumber }
