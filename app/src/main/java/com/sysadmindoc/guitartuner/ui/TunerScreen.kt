package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.audio.InputDeviceInfo
import com.sysadmindoc.guitartuner.audio.TunerSessionState
import com.sysadmindoc.guitartuner.settings.PegTurnDirection
import com.sysadmindoc.guitartuner.settings.StartupTuningMode
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.settings.ThemeMode
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import com.sysadmindoc.guitartuner.tuning.guidedTuningStep
import com.sysadmindoc.guitartuner.tuning.nextGuidedStringNumber
import kotlinx.coroutines.delay
import com.sysadmindoc.guitartuner.ui.theme.GuitarTunerTheme

@Composable
fun TunerScreen(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    permissionPermanentlyDenied: Boolean,
    activeTuning: TuningDefinition,
    tunings: List<TuningDefinition>,
    tuningMode: TuningMode,
    guidedStringNumber: Int,
    preferences: StoredTunerPreferences,
    onPrimaryAction: () -> Unit,
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
    tuningFileMessage: TuningFileMessage?,
    inputDevices: List<InputDeviceInfo>,
    selectedDeviceId: Int?,
    onInputDeviceSelected: (Int?) -> Unit,
    onPlayTone: (Double) -> Unit,
    onShowPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    var previousInTune by remember { mutableStateOf(false) }
    val currentlyInTune = state.measurement.status == TuningStatus.InTune
    LaunchedEffect(currentlyInTune) {
        if (currentlyInTune && !previousInTune && preferences.hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
        previousInTune = currentlyInTune
    }

    LaunchedEffect(currentlyInTune, tuningMode, guidedStringNumber) {
        if (!currentlyInTune || tuningMode != TuningMode.Guided || !preferences.autoAdvanceGuided) return@LaunchedEffect
        val step = guidedTuningStep(activeTuning.strings, guidedStringNumber)
        if (step.index >= step.total - 1) return@LaunchedEffect
        delay(1500)
        val nextString = nextGuidedStringNumber(activeTuning.strings, guidedStringNumber)
        if (preferences.hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
        onGuidedStringSelected(nextString)
    }

    var fullscreenMode by remember { mutableStateOf(false) }

    if (fullscreenMode && state.isListening) {
        FullscreenTunerView(
            state = state,
            onExit = { fullscreenMode = false },
        )
        return
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            val wideLayout = maxWidth >= 720.dp
            val mobileActionReserve = if (wideLayout) 0.dp else 96.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .padding(bottom = mobileActionReserve),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                TunerHeader(
                    state = state,
                    hasAudioPermission = hasAudioPermission,
                    activeTuning = activeTuning,
                    tuningMode = tuningMode,
                )

                if (wideLayout) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            TunerActionButtons(
                                state = state,
                                hasAudioPermission = hasAudioPermission,
                                permissionPermanentlyDenied = permissionPermanentlyDenied,
                                onPrimaryAction = onPrimaryAction,
                                onFullscreen = { fullscreenMode = true },
                                onShowPrivacy = onShowPrivacy,
                            )
                            TunerMeterPanel(
                                state = state,
                                hasAudioPermission = hasAudioPermission,
                                permissionPermanentlyDenied = permissionPermanentlyDenied,
                                activeTuning = activeTuning,
                                tuningMode = tuningMode,
                                guidedStringNumber = guidedStringNumber,
                                pegTurnDirections = preferences.pegTurnDirections,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        TunerSettingsPanel(
                            activeTuning = activeTuning,
                            tunings = tunings,
                            tuningMode = tuningMode,
                            guidedStringNumber = guidedStringNumber,
                            preferences = preferences,
                            onTuningModeSelected = onTuningModeSelected,
                            onGuidedStringSelected = onGuidedStringSelected,
                            onStartupModeSelected = onStartupModeSelected,
                            onSetFavoriteTuning = onSetFavoriteTuning,
                            onThemeModeSelected = onThemeModeSelected,
                            onFreezeAfterDecayChanged = onFreezeAfterDecayChanged,
                            onHapticEnabledChanged = onHapticEnabledChanged,
                            onAutoAdvanceGuidedChanged = onAutoAdvanceGuidedChanged,
                            onMeasureA4 = onMeasureA4,
                            onA4CalibrationChanged = onA4CalibrationChanged,
                            onCentsToleranceChanged = onCentsToleranceChanged,
                            onNoiseGateChanged = onNoiseGateChanged,
                            onPegTurnDirectionChanged = onPegTurnDirectionChanged,
                            onTuningSelected = onTuningSelected,
                            onImportTunings = onImportTunings,
                            onExportTunings = onExportTunings,
                            inputDevices = inputDevices,
                            selectedDeviceId = selectedDeviceId,
                            onInputDeviceSelected = onInputDeviceSelected,
                            onPlayTone = onPlayTone,
                            tuningFileMessage = tuningFileMessage,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    TunerMeterPanel(
                        state = state,
                        hasAudioPermission = hasAudioPermission,
                        permissionPermanentlyDenied = permissionPermanentlyDenied,
                        activeTuning = activeTuning,
                        tuningMode = tuningMode,
                        guidedStringNumber = guidedStringNumber,
                        pegTurnDirections = preferences.pegTurnDirections,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PrivacyDetailsButton(
                        onShowPrivacy = onShowPrivacy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TunerSettingsPanel(
                        activeTuning = activeTuning,
                        tunings = tunings,
                        tuningMode = tuningMode,
                        guidedStringNumber = guidedStringNumber,
                        preferences = preferences,
                        onTuningModeSelected = onTuningModeSelected,
                        onGuidedStringSelected = onGuidedStringSelected,
                        onStartupModeSelected = onStartupModeSelected,
                        onSetFavoriteTuning = onSetFavoriteTuning,
                        onThemeModeSelected = onThemeModeSelected,
                        onFreezeAfterDecayChanged = onFreezeAfterDecayChanged,
                        onHapticEnabledChanged = onHapticEnabledChanged,
                        onAutoAdvanceGuidedChanged = onAutoAdvanceGuidedChanged,
                        onMeasureA4 = onMeasureA4,
                        onA4CalibrationChanged = onA4CalibrationChanged,
                        onCentsToleranceChanged = onCentsToleranceChanged,
                        onNoiseGateChanged = onNoiseGateChanged,
                        onPegTurnDirectionChanged = onPegTurnDirectionChanged,
                        onTuningSelected = onTuningSelected,
                        onImportTunings = onImportTunings,
                        onExportTunings = onExportTunings,
                        inputDevices = inputDevices,
                        selectedDeviceId = selectedDeviceId,
                        onInputDeviceSelected = onInputDeviceSelected,
                        onPlayTone = onPlayTone,
                        tuningFileMessage = tuningFileMessage,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (!wideLayout) {
                MobileActionDock(
                    state = state,
                    hasAudioPermission = hasAudioPermission,
                    permissionPermanentlyDenied = permissionPermanentlyDenied,
                    onPrimaryAction = onPrimaryAction,
                    onFullscreen = { fullscreenMode = true },
                    onShowPrivacy = onShowPrivacy,
                )
            }
        }
    }
}

@Preview(name = "Phone portrait", widthDp = 360, heightDp = 740)
@Preview(name = "Phone landscape", widthDp = 840, heightDp = 360)
@Preview(name = "Split screen", widthDp = 320, heightDp = 600)
@Preview(name = "Tablet", widthDp = 900, heightDp = 1100)
@Preview(name = "Foldable", widthDp = 673, heightDp = 841)
@Composable
private fun TunerScreenPreview() {
    GuitarTunerTheme {
        TunerScreen(
            state = TunerSessionState(),
            hasAudioPermission = true,
            permissionPermanentlyDenied = false,
            activeTuning = GuitarTunings.standard,
            tunings = GuitarTunings.builtIns,
            tuningMode = TuningMode.Auto,
            guidedStringNumber = 6,
            preferences = StoredTunerPreferences(),
            onPrimaryAction = {},
            onTuningModeSelected = {},
            onGuidedStringSelected = {},
            onStartupModeSelected = {},
            onSetFavoriteTuning = {},
            onThemeModeSelected = {},
            onFreezeAfterDecayChanged = {},
            onHapticEnabledChanged = {},
            onAutoAdvanceGuidedChanged = {},
            onMeasureA4 = {},
            onA4CalibrationChanged = {},
            onCentsToleranceChanged = {},
            onNoiseGateChanged = {},
            onPegTurnDirectionChanged = { _, _ -> },
            onTuningSelected = {},
            onImportTunings = {},
            onExportTunings = {},
            inputDevices = emptyList(),
            selectedDeviceId = null,
            onInputDeviceSelected = {},
            onPlayTone = {},
            tuningFileMessage = null,
            onShowPrivacy = {},
        )
    }
}

@Composable
private fun BoxScope.MobileActionDock(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    permissionPermanentlyDenied: Boolean,
    onPrimaryAction: () -> Unit,
    onFullscreen: () -> Unit,
    onShowPrivacy: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        TunerActionButtons(
            state = state,
            hasAudioPermission = hasAudioPermission,
            permissionPermanentlyDenied = permissionPermanentlyDenied,
            onPrimaryAction = onPrimaryAction,
            onFullscreen = onFullscreen,
            onShowPrivacy = onShowPrivacy,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            showPrivacy = false,
        )
    }
}
