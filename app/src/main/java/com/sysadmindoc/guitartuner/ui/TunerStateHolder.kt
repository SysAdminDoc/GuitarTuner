package com.sysadmindoc.guitartuner.ui

import com.sysadmindoc.guitartuner.settings.CustomTuningRepository
import com.sysadmindoc.guitartuner.settings.StartupTuningMode
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.settings.MeterStyle
import com.sysadmindoc.guitartuner.settings.TunerPreferencesRepository
import com.sysadmindoc.guitartuner.settings.ThemeMode
import com.sysadmindoc.guitartuner.settings.PegTurnDirection
import com.sysadmindoc.guitartuner.tuning.CustomTuningJsonCodec
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.TuningCatalog
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TunerStateHolder(
    private val preferencesRepository: TunerPreferencesRepository,
    private val customTuningRepository: CustomTuningRepository,
    private val scope: CoroutineScope,
) {
    fun selectTuning(tuning: TuningDefinition) {
        scope.launch { preferencesRepository.rememberLastUsedTuning(tuning.id) }
    }

    fun setStartupMode(
        mode: StartupTuningMode,
        preferences: StoredTunerPreferences,
    ): String {
        val newTuningId = when (mode) {
            StartupTuningMode.StandardDefault -> GuitarTunings.StandardId
            StartupTuningMode.LastUsed -> preferences.lastUsedTuningId
            StartupTuningMode.Favorite -> preferences.favoriteTuningId
        }
        scope.launch { preferencesRepository.setStartupMode(mode) }
        return newTuningId
    }

    fun setFavoriteTuning(tuningId: String) {
        scope.launch { preferencesRepository.setFavoriteTuning(tuningId) }
    }

    fun setThemeMode(mode: ThemeMode) {
        scope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setFreezeAfterDecay(enabled: Boolean) {
        scope.launch { preferencesRepository.setFreezeAfterDecay(enabled) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        scope.launch { preferencesRepository.setHapticEnabled(enabled) }
    }

    fun setAutoAdvanceGuided(enabled: Boolean) {
        scope.launch { preferencesRepository.setAutoAdvanceGuided(enabled) }
    }

    fun setSpokenFeedback(enabled: Boolean) {
        scope.launch { preferencesRepository.setSpokenFeedback(enabled) }
    }

    fun setMeterStyle(style: MeterStyle) {
        scope.launch { preferencesRepository.setMeterStyle(style) }
    }

    fun setA4Hz(a4Hz: Double) {
        scope.launch { preferencesRepository.setA4Hz(a4Hz) }
    }

    fun setCentsTolerance(cents: Double) {
        scope.launch { preferencesRepository.setCentsTolerance(cents) }
    }

    fun setNoiseGateRms(rms: Double) {
        scope.launch { preferencesRepository.setNoiseGateRms(rms) }
    }

    fun setPegTurnDirection(stringNumber: Int, direction: PegTurnDirection) {
        scope.launch { preferencesRepository.setPegTurnDirection(stringNumber, direction) }
    }

    suspend fun processImport(source: String): TuningFileMessage {
        if (source.length > MaxImportFileSize) return TuningFileMessage.FileTooLarge
        return try {
            val result = customTuningRepository.replaceFromJson(source)
            if (result.errors.isEmpty()) {
                TuningFileMessage.Imported(result.tunings.size)
            } else {
                TuningFileMessage.Error(result.errors.joinToString(separator = "\n"))
            }
        } catch (_: Exception) {
            TuningFileMessage.ReadError
        }
    }

    fun buildExportJson(catalog: TuningCatalog): Pair<String, TuningFileMessage>? {
        val customOnly = catalog.tunings.filterNot { it.isBuiltIn }
        if (customOnly.isEmpty()) return null
        return CustomTuningJsonCodec.encode(customOnly) to TuningFileMessage.Exported(customOnly.size)
    }

    companion object {
        const val MaxImportFileSize = 512_000

        fun determinePrimaryAction(
            hasPermission: Boolean,
            permanentlyDenied: Boolean,
            isListening: Boolean,
        ): PrimaryAction = when {
            !hasPermission && permanentlyDenied -> PrimaryAction.OpenSettings
            !hasPermission -> PrimaryAction.RequestPermission
            isListening -> PrimaryAction.Stop
            else -> PrimaryAction.Start
        }

        fun measureA4FromLive(frequencyHz: Double?, confidence: Double): Double? {
            if (frequencyHz == null || frequencyHz !in 400.0..480.0 || confidence < 0.8) return null
            return kotlin.math.round(frequencyHz).coerceIn(400.0, 480.0)
        }
    }
}

enum class PrimaryAction {
    OpenSettings,
    RequestPermission,
    Stop,
    Start,
}
