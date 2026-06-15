package com.sysadmindoc.guitartuner.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.sysadmindoc.guitartuner.settings.CustomTuningRepository
import com.sysadmindoc.guitartuner.settings.TunerPreferencesRepository
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TunerStateHolderTest {

    @Test
    fun primaryAction_noPermission_notDenied_requestsPermission() {
        val action = TunerStateHolder.determinePrimaryAction(
            hasPermission = false,
            permanentlyDenied = false,
            isListening = false,
        )
        assertEquals(PrimaryAction.RequestPermission, action)
    }

    @Test
    fun primaryAction_noPermission_permanentlyDenied_opensSettings() {
        val action = TunerStateHolder.determinePrimaryAction(
            hasPermission = false,
            permanentlyDenied = true,
            isListening = false,
        )
        assertEquals(PrimaryAction.OpenSettings, action)
    }

    @Test
    fun primaryAction_hasPermission_listening_stops() {
        val action = TunerStateHolder.determinePrimaryAction(
            hasPermission = true,
            permanentlyDenied = false,
            isListening = true,
        )
        assertEquals(PrimaryAction.Stop, action)
    }

    @Test
    fun primaryAction_hasPermission_idle_starts() {
        val action = TunerStateHolder.determinePrimaryAction(
            hasPermission = true,
            permanentlyDenied = false,
            isListening = false,
        )
        assertEquals(PrimaryAction.Start, action)
    }

    @Test
    fun primaryAction_permanentlyDenied_ignored_when_permission_granted() {
        val action = TunerStateHolder.determinePrimaryAction(
            hasPermission = true,
            permanentlyDenied = true,
            isListening = false,
        )
        assertEquals(PrimaryAction.Start, action)
    }

    @Test
    fun measureA4_validFrequencyAndConfidence_returnsRounded() {
        val result = TunerStateHolder.measureA4FromLive(441.7, 0.95)
        assertEquals(442.0, result!!, 0.01)
    }

    @Test
    fun measureA4_lowConfidence_returnsNull() {
        val result = TunerStateHolder.measureA4FromLive(440.0, 0.5)
        assertNull(result)
    }

    @Test
    fun measureA4_nullFrequency_returnsNull() {
        val result = TunerStateHolder.measureA4FromLive(null, 0.95)
        assertNull(result)
    }

    @Test
    fun measureA4_outOfRange_low_returnsNull() {
        val result = TunerStateHolder.measureA4FromLive(399.0, 0.95)
        assertNull(result)
    }

    @Test
    fun measureA4_outOfRange_high_returnsNull() {
        val result = TunerStateHolder.measureA4FromLive(481.0, 0.95)
        assertNull(result)
    }

    @Test
    fun measureA4_atLowerBound_returnsValue() {
        val result = TunerStateHolder.measureA4FromLive(400.0, 0.8)
        assertEquals(400.0, result!!, 0.01)
    }

    @Test
    fun measureA4_atUpperBound_returnsValue() {
        val result = TunerStateHolder.measureA4FromLive(480.0, 0.8)
        assertEquals(480.0, result!!, 0.01)
    }

    @Test
    fun measureA4_atConfidenceThreshold_returnsValue() {
        val result = TunerStateHolder.measureA4FromLive(440.0, 0.8)
        assertEquals(440.0, result!!, 0.01)
    }

    @Test
    fun measureA4_justBelowConfidenceThreshold_returnsNull() {
        val result = TunerStateHolder.measureA4FromLive(440.0, 0.79)
        assertNull(result)
    }

    @Test
    fun processImport_oversizedSource_returnsSpecificFileTooLargeMessage() = runBlocking {
        val holder = TunerStateHolder(
            preferencesRepository = TunerPreferencesRepository(UnusedPreferencesDataStore),
            customTuningRepository = CustomTuningRepository(UnusedPreferencesDataStore),
            scope = CoroutineScope(EmptyCoroutineContext),
        )

        val result = holder.processImport("x".repeat(TunerStateHolder.MaxImportFileSize + 1))

        assertEquals(TuningFileMessage.FileTooLarge, result)
    }

    private object UnusedPreferencesDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flowOf(emptyPreferences())

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(emptyPreferences())
    }
}
