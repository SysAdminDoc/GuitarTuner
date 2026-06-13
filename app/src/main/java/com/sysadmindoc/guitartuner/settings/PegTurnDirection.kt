package com.sysadmindoc.guitartuner.settings

enum class PegTurnDirection {
    Left,
    Right,
}

fun PegTurnDirection.opposite(): PegTurnDirection = when (this) {
    PegTurnDirection.Left -> PegTurnDirection.Right
    PegTurnDirection.Right -> PegTurnDirection.Left
}

fun encodePegTurnDirections(directions: Map<Int, PegTurnDirection>): String =
    directions
        .toSortedMap(reverseOrder())
        .map { (stringNumber, direction) -> "$stringNumber=${direction.name}" }
        .joinToString(separator = ";")

fun decodePegTurnDirections(value: String?): Map<Int, PegTurnDirection> {
    if (value.isNullOrBlank()) return emptyMap()
    return value
        .split(";")
        .mapNotNull { entry ->
            val parts = entry.split("=")
            val stringNumber = parts.getOrNull(0)?.toIntOrNull()
            val direction = parts.getOrNull(1)?.let { encoded ->
                PegTurnDirection.entries.firstOrNull { it.name == encoded }
            }
            if (stringNumber == null || direction == null) {
                null
            } else {
                stringNumber to direction
            }
        }
        .toMap()
}
