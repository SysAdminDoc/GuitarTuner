package com.sysadmindoc.guitartuner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.audio.TunerSessionState
import com.sysadmindoc.guitartuner.tuning.TuningStatus

@Composable
internal fun FullscreenTunerView(
    state: TunerSessionState,
    onExit: () -> Unit,
) {
    BackHandler(onBack = onExit)

    val measurement = state.measurement
    val note = measurement.target?.scientificPitch ?: "--"
    val backgroundColor = when (measurement.status) {
        TuningStatus.InTune -> MaterialTheme.colorScheme.primary
        TuningStatus.Overshoot -> MaterialTheme.colorScheme.error
        TuningStatus.TuneUp,
        TuningStatus.TuneDown,
        -> MaterialTheme.colorScheme.tertiary

        else -> MaterialTheme.colorScheme.background
    }
    val contentColor = when (measurement.status) {
        TuningStatus.InTune -> MaterialTheme.colorScheme.onPrimary
        TuningStatus.Overshoot -> MaterialTheme.colorScheme.onError
        TuningStatus.TuneUp,
        TuningStatus.TuneDown,
        -> MaterialTheme.colorScheme.onTertiary

        else -> MaterialTheme.colorScheme.onBackground
    }
    val directionText = when (measurement.status) {
        TuningStatus.TuneUp -> stringResource(R.string.fullscreen_tune_up)
        TuningStatus.TuneDown -> stringResource(R.string.fullscreen_tune_down)
        TuningStatus.Overshoot -> stringResource(R.string.fullscreen_overshoot)
        TuningStatus.InTune -> stringResource(R.string.fullscreen_in_tune)
        else -> stringResource(R.string.fullscreen_waiting)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onExit),
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        KeepScreenOn()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val density = LocalDensity.current
            val availableWidth = maxWidth - 48.dp
            val noteMaxSp = with(density) { (availableWidth / 3).toSp() }
            val noteFontSize = min(92.sp, noteMaxSp)
            val directionMaxSp = with(density) { (availableWidth / 10).toSp() }
            val directionFontSize = min(34.sp, directionMaxSp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = note,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = noteFontSize,
                    lineHeight = noteFontSize * 1.05f,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Spacer(Modifier.size(16.dp))
                Text(
                    text = directionText,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = directionFontSize,
                    lineHeight = directionFontSize * 1.2f,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Spacer(Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.fullscreen_exit_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
