package com.sysadmindoc.guitartuner.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryRecoveryTest {
    @Test
    fun tunerPreferencesRecoverToDefaultsAfterCorruption() = runBlocking {
        val preferences = TunerPreferencesRepository(
            ThrowingPreferencesDataStore(CorruptionException("bad preferences")),
        ).preferences.first()

        assertEquals(StoredTunerPreferences(), preferences)
    }

    @Test
    fun customTuningsRecoverToEmptyListAfterReadFailure() = runBlocking {
        val tunings = CustomTuningRepository(
            ThrowingPreferencesDataStore(IOException("unreadable preferences")),
        ).customTunings.first()

        assertTrue(tunings.isEmpty())
    }

    private class ThrowingPreferencesDataStore(
        private val throwable: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw throwable }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(emptyPreferences())
    }
}
