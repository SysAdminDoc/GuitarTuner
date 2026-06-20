package com.sysadmindoc.guitartuner.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.tunerPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tuner_preferences",
)

class TunerPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<StoredTunerPreferences> = dataStore.data
        .catch { exception ->
            if (
                exception is IOException ||
                exception is CorruptionException ||
                exception is kotlinx.serialization.SerializationException
            ) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { storedPreferences ->
            StoredTunerPreferences(
                startupMode = storedPreferences[StartupModeKey].toStartupMode(),
                lastUsedTuningId = storedPreferences[LastUsedTuningKey] ?: GuitarTunings.StandardId,
                favoriteTuningId = storedPreferences[FavoriteTuningKey] ?: GuitarTunings.StandardId,
                themeMode = storedPreferences[ThemeModeKey].toThemeMode(),
                freezeAfterDecay = storedPreferences[FreezeAfterDecayKey] ?: false,
                hapticEnabled = storedPreferences[HapticEnabledKey] ?: false,
                autoAdvanceGuided = storedPreferences[AutoAdvanceGuidedKey] ?: true,
                spokenFeedback = storedPreferences[SpokenFeedbackKey] ?: false,
                meterStyle = storedPreferences[MeterStyleKey].toMeterStyle(),
                a4Hz = storedPreferences[A4HzKey].sanitizeA4Hz(),
                centsTolerance = storedPreferences[CentsToleranceKey].sanitizeCentsTolerance(),
                noiseGateRms = storedPreferences[NoiseGateRmsKey].sanitizeNoiseGateRms(),
                pegTurnDirections = decodePegTurnDirections(storedPreferences[PegDirectionsKey]),
                leftHanded = storedPreferences[LeftHandedKey] ?: false,
                capoFret = (storedPreferences[CapoFretKey] ?: 0).coerceIn(0, MaxCapoFret),
            )
        }

    suspend fun setStartupMode(mode: StartupTuningMode) {
        dataStore.edit { preferences ->
            preferences[StartupModeKey] = mode.name
        }
    }

    suspend fun rememberLastUsedTuning(tuningId: String) {
        dataStore.edit { preferences ->
            preferences[LastUsedTuningKey] = tuningId
        }
    }

    suspend fun setFavoriteTuning(tuningId: String) {
        dataStore.edit { preferences ->
            preferences[FavoriteTuningKey] = tuningId
            preferences[StartupModeKey] = StartupTuningMode.Favorite.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[ThemeModeKey] = mode.name
        }
    }

    suspend fun setFreezeAfterDecay(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FreezeAfterDecayKey] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HapticEnabledKey] = enabled
        }
    }

    suspend fun setAutoAdvanceGuided(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AutoAdvanceGuidedKey] = enabled
        }
    }

    suspend fun setSpokenFeedback(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SpokenFeedbackKey] = enabled
        }
    }

    suspend fun setMeterStyle(style: MeterStyle) {
        dataStore.edit { preferences ->
            preferences[MeterStyleKey] = style.name
        }
    }

    suspend fun setA4Hz(a4Hz: Double) {
        val calibration = PitchCalibration(a4Hz)
        dataStore.edit { preferences ->
            preferences[A4HzKey] = calibration.a4Hz
        }
    }

    suspend fun setCentsTolerance(centsTolerance: Double) {
        require(centsTolerance in 1.0..25.0) { "Cents tolerance must stay within 1 and 25 cents." }
        dataStore.edit { preferences ->
            preferences[CentsToleranceKey] = centsTolerance
        }
    }

    suspend fun setNoiseGateRms(noiseGateRms: Double) {
        require(noiseGateRms in 0.001..0.030) { "Noise gate must stay within 0.001 and 0.030 RMS." }
        dataStore.edit { preferences ->
            preferences[NoiseGateRmsKey] = noiseGateRms
        }
    }

    suspend fun setCapoFret(fret: Int) {
        dataStore.edit { preferences ->
            preferences[CapoFretKey] = fret.coerceIn(0, MaxCapoFret)
        }
    }

    suspend fun setLeftHanded(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LeftHandedKey] = enabled
        }
    }

    suspend fun setPegTurnDirection(
        stringNumber: Int,
        direction: PegTurnDirection,
    ) {
        dataStore.edit { preferences ->
            val current = decodePegTurnDirections(preferences[PegDirectionsKey]).toMutableMap()
            current[stringNumber] = direction
            preferences[PegDirectionsKey] = encodePegTurnDirections(current)
        }
    }

    private fun String?.toStartupMode(): StartupTuningMode =
        StartupTuningMode.entries.firstOrNull { it.name == this } ?: StartupTuningMode.StandardDefault

    private fun String?.toThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

    private fun String?.toMeterStyle(): MeterStyle =
        MeterStyle.entries.firstOrNull { it.name == this } ?: MeterStyle.Normal

    private fun Double?.sanitizeA4Hz(): Double =
        this?.takeIf { it in 400.0..480.0 } ?: PitchCalibration().a4Hz

    private fun Double?.sanitizeCentsTolerance(): Double =
        this?.takeIf { it in 1.0..25.0 } ?: TunerSettings().centsTolerance

    private fun Double?.sanitizeNoiseGateRms(): Double =
        this?.takeIf { it in 0.001..0.030 } ?: TunerSettings().noiseGateRms

    private companion object {
        val StartupModeKey = stringPreferencesKey("startup_mode")
        val LastUsedTuningKey = stringPreferencesKey("last_used_tuning_id")
        val FavoriteTuningKey = stringPreferencesKey("favorite_tuning_id")
        val ThemeModeKey = stringPreferencesKey("theme_mode")
        val FreezeAfterDecayKey = booleanPreferencesKey("freeze_after_decay")
        val HapticEnabledKey = booleanPreferencesKey("haptic_enabled")
        val AutoAdvanceGuidedKey = booleanPreferencesKey("auto_advance_guided")
        val SpokenFeedbackKey = booleanPreferencesKey("spoken_feedback")
        val MeterStyleKey = stringPreferencesKey("meter_style")
        val A4HzKey = doublePreferencesKey("a4_hz")
        val CentsToleranceKey = doublePreferencesKey("cents_tolerance")
        val NoiseGateRmsKey = doublePreferencesKey("noise_gate_rms")
        val PegDirectionsKey = stringPreferencesKey("peg_directions")
        val LeftHandedKey = booleanPreferencesKey("left_handed")
        val CapoFretKey = intPreferencesKey("capo_fret")
        const val MaxCapoFret = 12
    }
}
