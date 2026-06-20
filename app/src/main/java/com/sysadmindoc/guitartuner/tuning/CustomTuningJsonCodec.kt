package com.sysadmindoc.guitartuner.tuning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object CustomTuningJsonCodec {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun encode(tunings: List<TuningDefinition>): String {
        val file = CustomTuningFileDto(
            tunings = tunings
                .filterNot { it.isBuiltIn }
                .map { tuning ->
                    CustomTuningDto(
                        id = tuning.id,
                        name = tuning.name,
                        strings = tuning.strings.map { string ->
                            CustomStringDto(
                                stringNumber = string.stringNumber,
                                name = string.name,
                                scientificPitch = string.scientificPitch,
                                frequencyHz = string.frequencyHz,
                            )
                        },
                    )
                },
        )
        return json.encodeToString(CustomTuningFileDto.serializer(), file)
    }

    fun decode(source: String): CustomTuningImportResult {
        val file = try {
            json.decodeFromString(CustomTuningFileDto.serializer(), source)
        } catch (exception: SerializationException) {
            return CustomTuningImportResult(
                tunings = emptyList(),
                errors = listOf("Invalid JSON: ${exception.message ?: "could not parse file"}"),
            )
        } catch (exception: IllegalArgumentException) {
            return CustomTuningImportResult(
                tunings = emptyList(),
                errors = listOf("Invalid JSON: ${exception.message ?: "could not parse file"}"),
            )
        } catch (exception: Exception) {
            return CustomTuningImportResult(
                tunings = emptyList(),
                errors = listOf("Could not read tuning file: ${exception.message ?: "unexpected error"}"),
            )
        }

        val errors = mutableListOf<String>()
        if (file.schemaVersion != SupportedSchemaVersion) {
            errors += "Unsupported schemaVersion ${file.schemaVersion}; expected $SupportedSchemaVersion."
        }
        if (file.tunings.isEmpty()) {
            errors += "File does not contain any custom tunings."
        }

        val seenIds = mutableSetOf<String>()
        val tunings = file.tunings.mapIndexedNotNull { tuningIndex, dto ->
            val prefix = "tunings[$tuningIndex]"
            validateTuningDto(dto, prefix, seenIds, errors)
        }

        return CustomTuningImportResult(tunings = tunings, errors = errors)
    }

    private fun validateTuningDto(
        dto: CustomTuningDto,
        prefix: String,
        seenIds: MutableSet<String>,
        errors: MutableList<String>,
    ): TuningDefinition? {
        val id = dto.id.trim()
        val name = dto.name.trim()

        if (!IdPattern.matches(id)) {
            errors += "$prefix.id must use lowercase letters, numbers, underscores, or hyphens."
        }
        if (GuitarTunings.builtIns.any { it.id == id }) {
            errors += "$prefix.id cannot replace a built-in tuning."
        }
        if (!seenIds.add(id)) {
            errors += "$prefix.id duplicates another tuning id."
        }
        if (name.isBlank()) {
            errors += "$prefix.name is required."
        }
        if (dto.strings.size !in MinStringCount..MaxStringCount) {
            errors += "$prefix.strings must contain between $MinStringCount and $MaxStringCount strings."
        }

        val seenStringNumbers = mutableSetOf<Int>()
        val strings = dto.strings.mapIndexedNotNull { stringIndex, stringDto ->
            val stringPrefix = "$prefix.strings[$stringIndex]"
            validateStringDto(stringDto, stringPrefix, seenStringNumbers, errors)
        }.sortedByDescending { it.stringNumber }

        return if (errors.any { it.startsWith(prefix) }) {
            null
        } else {
            TuningDefinition(
                id = id,
                name = name,
                strings = strings,
                isBuiltIn = false,
            )
        }
    }

    private fun validateStringDto(
        dto: CustomStringDto,
        prefix: String,
        seenStringNumbers: MutableSet<Int>,
        errors: MutableList<String>,
    ): GuitarString? {
        if (dto.stringNumber !in 1..MaxStringCount) {
            errors += "$prefix.stringNumber must be between 1 and $MaxStringCount."
        }
        if (!seenStringNumbers.add(dto.stringNumber)) {
            errors += "$prefix.stringNumber duplicates another string."
        }
        if (dto.name.isBlank()) {
            errors += "$prefix.name is required."
        }
        if (dto.scientificPitch.isBlank()) {
            errors += "$prefix.scientificPitch is required."
        }
        if (dto.frequencyHz !in MinFrequencyHz..MaxFrequencyHz) {
            errors += "$prefix.frequencyHz must be between $MinFrequencyHz and $MaxFrequencyHz."
        }

        return if (errors.any { it.startsWith(prefix) }) {
            null
        } else {
            GuitarString(
                stringNumber = dto.stringNumber,
                name = dto.name.trim(),
                scientificPitch = dto.scientificPitch.trim(),
                frequencyHz = dto.frequencyHz,
            )
        }
    }

    private const val SupportedSchemaVersion = 1
    private const val MinStringCount = 1
    private const val MaxStringCount = 12
    private const val MinFrequencyHz = 20.0
    private const val MaxFrequencyHz = 500.0
    private val IdPattern = Regex("[a-z0-9][a-z0-9_-]{1,48}")
}

data class CustomTuningImportResult(
    val tunings: List<TuningDefinition>,
    val errors: List<String>,
)

@Serializable
private data class CustomTuningFileDto(
    val schemaVersion: Int = 1,
    val tunings: List<CustomTuningDto>,
)

@Serializable
private data class CustomTuningDto(
    val id: String,
    val name: String,
    val strings: List<CustomStringDto>,
)

@Serializable
private data class CustomStringDto(
    val stringNumber: Int,
    val name: String,
    @SerialName("note")
    val scientificPitch: String,
    val frequencyHz: Double,
)
