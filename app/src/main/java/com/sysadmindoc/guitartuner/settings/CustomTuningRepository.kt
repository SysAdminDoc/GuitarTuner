package com.sysadmindoc.guitartuner.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sysadmindoc.guitartuner.tuning.CustomTuningImportResult
import com.sysadmindoc.guitartuner.tuning.CustomTuningJsonCodec
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomTuningRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val customTunings: Flow<List<TuningDefinition>> = dataStore.data.map { preferences ->
        preferences[CustomTuningsJsonKey]
            ?.let { CustomTuningJsonCodec.decode(it) }
            ?.takeIf { it.errors.isEmpty() }
            ?.tunings
            ?: emptyList()
    }

    suspend fun replaceFromJson(source: String): CustomTuningImportResult {
        val result = CustomTuningJsonCodec.decode(source)
        if (result.errors.isNotEmpty()) return result

        dataStore.edit { preferences ->
            preferences[CustomTuningsJsonKey] = CustomTuningJsonCodec.encode(result.tunings)
        }
        return result
    }

    private companion object {
        val CustomTuningsJsonKey = stringPreferencesKey("custom_tunings_json")
    }
}
