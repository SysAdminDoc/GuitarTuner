package com.sysadmindoc.guitartuner.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sysadmindoc.guitartuner.audio.AudioCaptureController
import com.sysadmindoc.guitartuner.audio.SpokenFeedbackController
import com.sysadmindoc.guitartuner.audio.TonePlayer
import com.sysadmindoc.guitartuner.settings.CustomTuningRepository
import com.sysadmindoc.guitartuner.settings.TunerPreferencesRepository
import com.sysadmindoc.guitartuner.settings.tunerPreferencesDataStore
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningTargetSelection

class TunerViewModel(
    application: Application,
    private val savedState: SavedStateHandle,
) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext

    val preferencesRepository = TunerPreferencesRepository(context.tunerPreferencesDataStore)
    val customTuningRepository = CustomTuningRepository(context.tunerPreferencesDataStore)
    val stateHolder = TunerStateHolder(preferencesRepository, customTuningRepository, viewModelScope)

    val controller: AudioCaptureController = run {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val supportsUnprocessed = audioManager.getProperty(
            AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED,
        ) == "true"
        AudioCaptureController(
            scope = viewModelScope,
            supportsUnprocessedSource = supportsUnprocessed,
            audioManager = audioManager,
            context = context,
        )
    }

    val tonePlayer = TonePlayer()
    val spokenFeedback = SpokenFeedbackController(context)

    var selectedTuningId: String?
        get() = savedState["selectedTuningId"]
        set(value) { savedState["selectedTuningId"] = value }

    var tuningMode: TuningMode
        get() = savedState.get<String>("tuningMode")?.let {
            try { TuningMode.valueOf(it) } catch (_: Exception) { TuningMode.Auto }
        } ?: TuningMode.Auto
        set(value) { savedState["tuningMode"] = value.name }

    var guidedStringNumber: Int
        get() = savedState["guidedStringNumber"] ?: 6
        set(value) { savedState["guidedStringNumber"] = value }

    var selectedDeviceId: Int?
        get() = savedState["selectedDeviceId"]
        set(value) { savedState["selectedDeviceId"] = value }

    fun updateTargetSelection() {
        controller.setTargetSelection(
            when (tuningMode) {
                TuningMode.Auto -> TuningTargetSelection.auto()
                TuningMode.Guided -> TuningTargetSelection.guided(guidedStringNumber)
                TuningMode.Chromatic -> TuningTargetSelection.chromatic()
            },
        )
    }

    override fun onCleared() {
        controller.close()
        tonePlayer.stop()
        spokenFeedback.close()
    }
}
