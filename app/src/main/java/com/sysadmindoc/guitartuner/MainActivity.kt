package com.sysadmindoc.guitartuner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import com.sysadmindoc.guitartuner.settings.StoredTunerPreferences
import com.sysadmindoc.guitartuner.settings.TunerPreferencesRepository
import com.sysadmindoc.guitartuner.settings.tunerPreferencesDataStore
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.ui.PrivacyScreen
import com.sysadmindoc.guitartuner.ui.TunerScreen
import com.sysadmindoc.guitartuner.ui.theme.GuitarTunerTheme
import kotlinx.coroutines.launch

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
    val preferencesRepository = remember(context.applicationContext) {
        TunerPreferencesRepository(context.applicationContext.tunerPreferencesDataStore)
    }
    val preferences by preferencesRepository.preferences.collectAsStateWithLifecycle(
        initialValue = StoredTunerPreferences(),
    )
    val activeTuning = remember(preferences) {
        GuitarTunings.find(preferences.startupTuningId())
    }
    val state by controller.state.collectAsStateWithLifecycle()
    var hasAudioPermission by remember { mutableStateOf(context.hasAudioPermission()) }

    LaunchedEffect(activeTuning.id) {
        controller.setTuning(activeTuning.strings)
        preferencesRepository.rememberLastUsedTuning(activeTuning.id)
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
            preferences = preferences,
            onPrimaryAction = {
                when {
                    !hasAudioPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    state.isListening -> controller.stop()
                    else -> controller.start()
                }
            },
            onStop = controller::stop,
            onStartupModeSelected = { mode ->
                scope.launch { preferencesRepository.setStartupMode(mode) }
            },
            onSetFavoriteTuning = {
                scope.launch { preferencesRepository.setFavoriteTuning(activeTuning.id) }
            },
            onShowPrivacy = { showPrivacy = true },
        )
    }
}

private fun Context.hasAudioPermission(): Boolean =
    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
