package com.sysadmindoc.guitartuner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import com.sysadmindoc.guitartuner.ui.PrimaryAction
import com.sysadmindoc.guitartuner.ui.PrivacyScreen
import com.sysadmindoc.guitartuner.ui.TunerScreen
import com.sysadmindoc.guitartuner.ui.TunerStateHolder
import com.sysadmindoc.guitartuner.ui.TunerViewModel
import com.sysadmindoc.guitartuner.ui.TuningFileMessage
import com.sysadmindoc.guitartuner.ui.theme.GuitarTunerTheme
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quickTune = intent?.action == QuickTuneAction
        setContent {
            TunerRoute(quickTune = quickTune)
        }
    }

    companion object {
        const val QuickTuneAction = "com.sysadmindoc.guitartuner.QUICK_TUNE"
    }
}

@Composable
private fun TunerRoute(quickTune: Boolean = false) {
    val vm: TunerViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = vm.controller
    val stateHolder = vm.stateHolder
    val tonePlayer = vm.tonePlayer
    val spokenFeedback = vm.spokenFeedback

    var showPrivacy by rememberSaveable { mutableStateOf(false) }
    var tuningFileMessage by remember { mutableStateOf<TuningFileMessage?>(null) }

    val preferences by vm.preferencesRepository.preferences.collectAsStateWithLifecycle(
        initialValue = StoredTunerPreferences(),
    )
    val customTunings by vm.customTuningRepository.customTunings.collectAsStateWithLifecycle(
        initialValue = emptyList(),
    )
    val uncalibratedTuningCatalog = remember(customTunings) {
        GuitarTunings.catalog(customTunings)
    }
    val tuningCatalog = remember(customTunings, preferences.a4Hz, preferences.capoFret) {
        GuitarTunings.catalog(customTunings, preferences.a4Hz, preferences.capoFret)
    }
    val inputDevices = remember(controller) { controller.availableInputDevices() }
    val startupTuningId = preferences.startupTuningId()
    val activeTuning = remember(tuningCatalog, vm.selectedTuningId, startupTuningId) {
        tuningCatalog.find(vm.selectedTuningId ?: startupTuningId)
    }
    val state by controller.state.collectAsStateWithLifecycle()
    var hasAudioPermission by remember { mutableStateOf(context.hasAudioPermission()) }

    LaunchedEffect(startupTuningId, tuningCatalog) {
        if (vm.selectedTuningId == null || tuningCatalog.tunings.none { it.id == vm.selectedTuningId }) {
            vm.selectedTuningId = startupTuningId
        }
    }

    LaunchedEffect(activeTuning) {
        controller.setTuning(activeTuning.strings, activeTuning.minFrequencyHz, activeTuning.maxFrequencyHz)
        vm.preferencesRepository.rememberLastUsedTuning(activeTuning.id)
        if (activeTuning.strings.none { it.stringNumber == vm.guidedStringNumber }) {
            vm.guidedStringNumber = activeTuning.strings.firstOrNull()?.stringNumber ?: vm.guidedStringNumber
        }
    }

    LaunchedEffect(activeTuning.id, vm.tuningMode, vm.guidedStringNumber) {
        vm.updateTargetSelection()
    }

    LaunchedEffect(preferences.a4Hz) {
        controller.setA4Hz(preferences.a4Hz)
    }

    LaunchedEffect(preferences.freezeAfterDecay) {
        controller.setFreezeAfterDecay(preferences.freezeAfterDecay)
    }

    LaunchedEffect(preferences.centsTolerance) {
        controller.setCentsTolerance(preferences.centsTolerance)
    }

    LaunchedEffect(preferences.noiseGateRms) {
        controller.setNoiseGateRms(preferences.noiseGateRms)
    }

    val ttsResources = context.resources
    @Suppress("LocalContextGetResourceValueCall")
    LaunchedEffect(state.measurement, preferences.spokenFeedback, state.isListening) {
        if (!preferences.spokenFeedback || !state.isListening) {
            spokenFeedback.stop()
            return@LaunchedEffect
        }
        val measurement = state.measurement
        val target = measurement.target ?: return@LaunchedEffect
        val note = target.scientificPitch
        val cents = kotlin.math.abs(measurement.cents ?: 0.0).let {
            String.format(java.util.Locale.US, "%.0f", it)
        }
        val text = when (measurement.status) {
            TuningStatus.InTune -> ttsResources.getString(R.string.tts_in_tune, note)
            TuningStatus.TuneUp -> ttsResources.getString(R.string.tts_tune_up, note, cents)
            TuningStatus.TuneDown -> ttsResources.getString(R.string.tts_tune_down, note, cents)
            else -> return@LaunchedEffect
        }
        spokenFeedback.speak(text)
    }

    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            tuningFileMessage = try {
                val source = context.readTextFromUri(uri, TunerStateHolder.MaxImportFileSize)
                stateHolder.processImport(source)
            } catch (_: TuningFileTooLargeException) {
                TuningFileMessage.FileTooLarge
            } catch (_: Exception) {
                TuningFileMessage.ReadError
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val result = stateHolder.buildExportJson(uncalibratedTuningCatalog)
                if (result == null) {
                    tuningFileMessage = TuningFileMessage.NoCustomTunings
                } else {
                    context.writeTextToUri(uri, result.first)
                    tuningFileMessage = result.second
                }
            } catch (_: Exception) {
                tuningFileMessage = TuningFileMessage.WriteError
            }
        }
    }

    val activity = context as? ComponentActivity
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            permanentlyDenied = false
            controller.clearPermissionError()
            controller.start()
        } else {
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(
                Manifest.permission.RECORD_AUDIO,
            ) ?: false
            permanentlyDenied = !shouldShowRationale
            controller.notePermissionDenied()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val nowGranted = context.hasAudioPermission()
                    hasAudioPermission = nowGranted
                    if (nowGranted && state.permissionError) {
                        permanentlyDenied = false
                        controller.clearPermissionError()
                    }
                }
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP,
                Lifecycle.Event.ON_DESTROY,
                -> controller.stop()

                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(quickTune) {
        if (!quickTune) return@LaunchedEffect
        if (hasAudioPermission) {
            controller.start()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    GuitarTunerTheme(themeMode = preferences.themeMode) {
        if (showPrivacy) {
            PrivacyScreen(onBack = { showPrivacy = false })
        } else {
            TunerScreen(
                state = state,
                hasAudioPermission = hasAudioPermission,
                permissionPermanentlyDenied = permanentlyDenied,
                activeTuning = activeTuning,
                tunings = tuningCatalog.tunings,
                tuningMode = vm.tuningMode,
                guidedStringNumber = vm.guidedStringNumber,
                preferences = preferences,
                onPrimaryAction = {
                    when (TunerStateHolder.determinePrimaryAction(
                        hasAudioPermission, permanentlyDenied, state.isListening,
                    )) {
                        PrimaryAction.OpenSettings -> {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            )
                            context.startActivity(intent)
                        }
                        PrimaryAction.RequestPermission ->
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        PrimaryAction.Stop -> controller.stop()
                        PrimaryAction.Start -> controller.start()
                    }
                },
                onTuningModeSelected = { mode -> vm.tuningMode = mode },
                onGuidedStringSelected = { stringNumber -> vm.guidedStringNumber = stringNumber },
                onStartupModeSelected = { mode ->
                    vm.selectedTuningId = stateHolder.setStartupMode(mode, preferences)
                },
                onSetFavoriteTuning = { stateHolder.setFavoriteTuning(activeTuning.id) },
                onThemeModeSelected = { mode -> stateHolder.setThemeMode(mode) },
                onFreezeAfterDecayChanged = { enabled -> stateHolder.setFreezeAfterDecay(enabled) },
                onHapticEnabledChanged = { enabled -> stateHolder.setHapticEnabled(enabled) },
                onAutoAdvanceGuidedChanged = { enabled -> stateHolder.setAutoAdvanceGuided(enabled) },
                onSpokenFeedbackChanged = { enabled -> stateHolder.setSpokenFeedback(enabled) },
                onLeftHandedChanged = { enabled -> stateHolder.setLeftHanded(enabled) },
                onCapoFretChanged = { fret -> stateHolder.setCapoFret(fret) },
                onNoteNamingSelected = { naming -> stateHolder.setNoteNaming(naming) },
                onMeterStyleSelected = { style -> stateHolder.setMeterStyle(style) },
                onMeasureA4 = {
                    val measured = TunerStateHolder.measureA4FromLive(
                        state.pitchEstimate.frequencyHz, state.pitchEstimate.confidence,
                    )
                    if (measured != null) stateHolder.setA4Hz(measured)
                },
                onA4CalibrationChanged = { a4Hz -> stateHolder.setA4Hz(a4Hz) },
                onCentsToleranceChanged = { cents -> stateHolder.setCentsTolerance(cents) },
                onNoiseGateChanged = { rms -> stateHolder.setNoiseGateRms(rms) },
                onPegTurnDirectionChanged = { stringNumber, direction ->
                    stateHolder.setPegTurnDirection(stringNumber, direction)
                },
                onTuningSelected = { tuning ->
                    vm.selectedTuningId = tuning.id
                    stateHolder.selectTuning(tuning)
                },
                onImportTunings = {
                    importLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
                },
                onExportTunings = {
                    exportLauncher.launch("guitartuner-custom-tunings.json")
                },
                inputDevices = inputDevices,
                selectedDeviceId = vm.selectedDeviceId,
                onInputDeviceSelected = { deviceId ->
                    vm.selectedDeviceId = deviceId
                    controller.setPreferredDevice(deviceId)
                },
                onPlayTone = { frequencyHz ->
                    controller.stop()
                    tonePlayer.play(frequencyHz)
                },
                tuningFileMessage = tuningFileMessage,
                onShowPrivacy = { showPrivacy = true },
            )
        }
    }
}

private fun Context.hasAudioPermission(): Boolean =
    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private suspend fun Context.readTextFromUri(uri: Uri, maxChars: Int): String = withContext(Dispatchers.IO) {
    contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
        val input = reader ?: throw IOException("Could not open selected tuning file.")
        val text = StringBuilder()
        val buffer = CharArray(8_192)
        while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            if (text.length + read > maxChars) throw TuningFileTooLargeException()
            text.append(buffer, 0, read)
        }
        text.toString()
    }
}

private suspend fun Context.writeTextToUri(uri: Uri, text: String) = withContext(Dispatchers.IO) {
    contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
        val output = writer ?: throw IOException("Could not open export destination.")
        output.write(text)
    }
}

private class TuningFileTooLargeException : IOException()
