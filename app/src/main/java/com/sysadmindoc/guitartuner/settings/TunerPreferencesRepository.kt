package com.sysadmindoc.guitartuner.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
            if (exception is IOException) {
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
                freezeAfterDecay = storedPreferences[FreezeAfterDecayKey] ?: false,
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

    suspend fun setFreezeAfterDecay(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FreezeAfterDecayKey] = enabled
        }
    }

    private fun String?.toStartupMode(): StartupTuningMode =
        StartupTuningMode.entries.firstOrNull { it.name == this } ?: StartupTuningMode.StandardDefault

    private companion object {
        val StartupModeKey = stringPreferencesKey("startup_mode")
        val LastUsedTuningKey = stringPreferencesKey("last_used_tuning_id")
        val FavoriteTuningKey = stringPreferencesKey("favorite_tuning_id")
        val FreezeAfterDecayKey = booleanPreferencesKey("freeze_after_decay")
    }
}
