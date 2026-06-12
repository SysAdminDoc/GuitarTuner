package com.sysadmindoc.guitartuner.settings

import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import org.junit.Assert.assertEquals
import org.junit.Test

class StoredTunerPreferencesTest {
    @Test
    fun standardStartupIgnoresSavedTuningIds() {
        val preferences = StoredTunerPreferences(
            startupMode = StartupTuningMode.StandardDefault,
            lastUsedTuningId = "drop_d",
            favoriteTuningId = "dadgad",
        )

        assertEquals(GuitarTunings.StandardId, preferences.startupTuningId())
    }

    @Test
    fun lastUsedStartupUsesLastUsedTuningId() {
        val preferences = StoredTunerPreferences(
            startupMode = StartupTuningMode.LastUsed,
            lastUsedTuningId = "drop_d",
            favoriteTuningId = "dadgad",
        )

        assertEquals("drop_d", preferences.startupTuningId())
    }

    @Test
    fun favoriteStartupUsesFavoriteTuningId() {
        val preferences = StoredTunerPreferences(
            startupMode = StartupTuningMode.Favorite,
            lastUsedTuningId = "drop_d",
            favoriteTuningId = "dadgad",
        )

        assertEquals("dadgad", preferences.startupTuningId())
    }
}
