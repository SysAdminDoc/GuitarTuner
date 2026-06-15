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
        .filterKeys { it in SupportedStringNumbers }
        .toSortedMap(reverseOrder())
        .map { (stringNumber, direction) -> "$stringNumber=${direction.name}" }
        .joinToString(separator = ";")

fun decodePegTurnDirections(value: String?): Map<Int, PegTurnDirection> {
    if (value.isNullOrBlank()) return emptyMap()
    return value
        .split(";")
        .mapNotNull { entry ->
            val parts = entry.split("=")
            if (parts.size != 2) return@mapNotNull null
            val stringNumber = parts[0].toIntOrNull()?.takeIf { it in SupportedStringNumbers }
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

private val SupportedStringNumbers = 1..6
