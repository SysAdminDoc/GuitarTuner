package com.sysadmindoc.guitartuner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sysadmindoc.guitartuner.audio.AudioCaptureController
import com.sysadmindoc.guitartuner.settings.CustomTuningRepository
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.settings.TunerPreferencesRepository
import com.sysadmindoc.guitartuner.settings.tunerPreferencesDataStore
import com.sysadmindoc.guitartuner.tuning.CustomTuningJsonCodec
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningTargetSelection
import com.sysadmindoc.guitartuner.ui.PrivacyScreen
import com.sysadmindoc.guitartuner.ui.TuningFileMessage
import com.sysadmindoc.guitartuner.ui.TunerScreen
import com.sysadmindoc.guitartuner.ui.theme.GuitarTunerTheme
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuitarTunerTheme {
                TunerRoute()
            }
        }
    }
}

@Composable
private fun TunerRoute() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val controller = remember(scope) { AudioCaptureController(scope = scope) }
    var showPrivacy by rememberSaveable { mutableStateOf(false) }
    var selectedTuningId by rememberSaveable { mutableStateOf<String?>(null) }
    var tuningMode by rememberSaveable { mutableStateOf(TuningMode.Guided) }
    var guidedStringNumber by rememberSaveable { mutableStateOf(6) }
    var tuningFileMessage by remember { mutableStateOf<TuningFileMessage?>(null) }
    val preferencesRepository = remember(context.applicationContext) {
        TunerPreferencesRepository(context.applicationContext.tunerPreferencesDataStore)
    }
    val customTuningRepository = remember(context.applicationContext) {
        CustomTuningRepository(context.applicationContext.tunerPreferencesDataStore)
    }
    val preferences by preferencesRepository.preferences.collectAsStateWithLifecycle(
        initialValue = StoredTunerPreferences(),
    )
    val customTunings by customTuningRepository.customTunings.collectAsStateWithLifecycle(
        initialValue = emptyList(),
    )
    val uncalibratedTuningCatalog = remember(customTunings) {
        GuitarTunings.catalog(customTunings)
    }
    val tuningCatalog = remember(customTunings, preferences.a4Hz) {
        GuitarTunings.catalog(customTunings, preferences.a4Hz)
    }
    val startupTuningId = preferences.startupTuningId()
    val activeTuning = remember(tuningCatalog, selectedTuningId, startupTuningId) {
        tuningCatalog.find(selectedTuningId ?: startupTuningId)
    }
    val state by controller.state.collectAsStateWithLifecycle()
    var hasAudioPermission by remember { mutableStateOf(context.hasAudioPermission()) }

    LaunchedEffect(startupTuningId, tuningCatalog) {
        if (selectedTuningId == null || tuningCatalog.tunings.none { it.id == selectedTuningId }) {
            selectedTuningId = startupTuningId
        }
    }

    LaunchedEffect(activeTuning) {
        controller.setTuning(activeTuning.strings)
        preferencesRepository.rememberLastUsedTuning(activeTuning.id)
        if (activeTuning.strings.none { it.stringNumber == guidedStringNumber }) {
            guidedStringNumber = activeTuning.strings.firstOrNull()?.stringNumber ?: guidedStringNumber
        }
    }

    LaunchedEffect(activeTuning.id, tuningMode, guidedStringNumber) {
        controller.setTargetSelection(
            when (tuningMode) {
                TuningMode.Auto -> TuningTargetSelection.auto()
                TuningMode.Guided -> TuningTargetSelection.guided(guidedStringNumber)
            },
        )
    }

    LaunchedEffect(preferences.freezeAfterDecay) {
        controller.setFreezeAfterDecay(preferences.freezeAfterDecay)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val source = context.readTextFromUri(uri)
            val result = customTuningRepository.replaceFromJson(source)
            tuningFileMessage = if (result.errors.isEmpty()) {
                TuningFileMessage.Imported(result.tunings.size)
            } else {
                TuningFileMessage.Error(result.errors.joinToString(separator = "\n"))
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val customOnly = uncalibratedTuningCatalog.tunings.filterNot { it.isBuiltIn }
            if (customOnly.isEmpty()) {
                tuningFileMessage = TuningFileMessage.NoCustomTunings
                return@launch
            }
            context.writeTextToUri(uri, CustomTuningJsonCodec.encode(customOnly))
            tuningFileMessage = TuningFileMessage.Exported(customOnly.size)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            controller.start()
        } else {
            controller.notePermissionDenied()
        }
    }

    DisposableEffect(lifecycleOwner, controller, context) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> hasAudioPermission = context.hasAudioPermission()
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
            controller.close()
        }
    }

    if (showPrivacy) {
        PrivacyScreen(onBack = { showPrivacy = false })
    } else {
        TunerScreen(
            state = state,
            hasAudioPermission = hasAudioPermission,
            activeTuning = activeTuning,
            tunings = tuningCatalog.tunings,
            tuningMode = tuningMode,
            guidedStringNumber = guidedStringNumber,
            preferences = preferences,
            onPrimaryAction = {
                when {
                    !hasAudioPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    state.isListening -> controller.stop()
                    else -> controller.start()
                }
            },
            onStop = controller::stop,
            onTuningModeSelected = { mode ->
                tuningMode = mode
            },
            onGuidedStringSelected = { stringNumber ->
                guidedStringNumber = stringNumber
            },
            onStartupModeSelected = { mode ->
                selectedTuningId = when (mode) {
                    com.sysadmindoc.guitartuner.settings.StartupTuningMode.StandardDefault ->
                        GuitarTunings.StandardId
                    com.sysadmindoc.guitartuner.settings.StartupTuningMode.LastUsed ->
                        preferences.lastUsedTuningId
                    com.sysadmindoc.guitartuner.settings.StartupTuningMode.Favorite ->
                        preferences.favoriteTuningId
                }
                scope.launch { preferencesRepository.setStartupMode(mode) }
            },
            onSetFavoriteTuning = {
                scope.launch { preferencesRepository.setFavoriteTuning(activeTuning.id) }
            },
            onFreezeAfterDecayChanged = { enabled ->
                scope.launch { preferencesRepository.setFreezeAfterDecay(enabled) }
            },
            onA4CalibrationChanged = { a4Hz ->
                scope.launch { preferencesRepository.setA4Hz(a4Hz) }
            },
            onPegTurnDirectionChanged = { stringNumber, direction ->
                scope.launch { preferencesRepository.setPegTurnDirection(stringNumber, direction) }
            },
            onTuningSelected = { tuning ->
                selectedTuningId = tuning.id
                scope.launch { preferencesRepository.rememberLastUsedTuning(tuning.id) }
            },
            onImportTunings = {
                importLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
            },
            onExportTunings = {
                exportLauncher.launch("guitartuner-custom-tunings.json")
            },
            tuningFileMessage = tuningFileMessage,
            onShowPrivacy = { showPrivacy = true },
        )
    }
}

private fun Context.hasAudioPermission(): Boolean =
    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private suspend fun Context.readTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
    contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
        reader?.readText() ?: throw IOException("Could not open selected tuning file.")
    }
}

private suspend fun Context.writeTextToUri(uri: Uri, text: String) = withContext(Dispatchers.IO) {
    contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
        val output = writer ?: throw IOException("Could not open export destination.")
        output.write(text)
    }
}
