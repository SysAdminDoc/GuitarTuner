package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.audio.InputDeviceInfo
import com.sysadmindoc.guitartuner.settings.PegTurnDirection
import com.sysadmindoc.guitartuner.settings.StartupTuningMode
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.settings.ThemeMode
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningMode

@Composable
internal fun TunerSettingsPanel(
    activeTuning: TuningDefinition,
    tunings: List<TuningDefinition>,
    tuningMode: TuningMode,
    guidedStringNumber: Int,
    preferences: StoredTunerPreferences,
    onTuningModeSelected: (TuningMode) -> Unit,
    onGuidedStringSelected: (Int) -> Unit,
    onStartupModeSelected: (StartupTuningMode) -> Unit,
    onSetFavoriteTuning: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onFreezeAfterDecayChanged: (Boolean) -> Unit,
    onHapticEnabledChanged: (Boolean) -> Unit,
    onAutoAdvanceGuidedChanged: (Boolean) -> Unit,
    onMeasureA4: () -> Unit,
    onA4CalibrationChanged: (Double) -> Unit,
    onCentsToleranceChanged: (Double) -> Unit,
    onNoiseGateChanged: (Double) -> Unit,
    onPegTurnDirectionChanged: (Int, PegTurnDirection) -> Unit,
    onTuningSelected: (TuningDefinition) -> Unit,
    onImportTunings: () -> Unit,
    onExportTunings: () -> Unit,
    inputDevices: List<InputDeviceInfo>,
    selectedDeviceId: Int?,
    onInputDeviceSelected: (Int?) -> Unit,
    onPlayTone: (Double) -> Unit,
    tuningFileMessage: TuningFileMessage?,
    modifier: Modifier = Modifier,
) {
    val hasCustomTunings = tunings.any { !it.isBuiltIn }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TuningSection(activeTuning, tunings, onTuningSelected)
        ModeSection(
            activeTuning = activeTuning,
            tuningMode = tuningMode,
            guidedStringNumber = guidedStringNumber,
            preferences = preferences,
            onTuningModeSelected = onTuningModeSelected,
            onGuidedStringSelected = onGuidedStringSelected,
            onPegTurnDirectionChanged = onPegTurnDirectionChanged,
            onPlayTone = onPlayTone,
        )
        DefaultsSection(
            preferences = preferences,
            onStartupModeSelected = onStartupModeSelected,
            onSetFavoriteTuning = onSetFavoriteTuning,
            onThemeModeSelected = onThemeModeSelected,
            onFreezeAfterDecayChanged = onFreezeAfterDecayChanged,
            onHapticEnabledChanged = onHapticEnabledChanged,
            onAutoAdvanceGuidedChanged = onAutoAdvanceGuidedChanged,
        )
        PrecisionSection(
            preferences = preferences,
            onMeasureA4 = onMeasureA4,
            onA4CalibrationChanged = onA4CalibrationChanged,
            onCentsToleranceChanged = onCentsToleranceChanged,
            onNoiseGateChanged = onNoiseGateChanged,
        )
        if (inputDevices.isNotEmpty()) {
            InputDeviceSection(
                inputDevices = inputDevices,
                selectedDeviceId = selectedDeviceId,
                onInputDeviceSelected = onInputDeviceSelected,
            )
        }
        FilesSection(
            hasCustomTunings = hasCustomTunings,
            onImportTunings = onImportTunings,
            onExportTunings = onExportTunings,
            tuningFileMessage = tuningFileMessage,
        )
    }
}
