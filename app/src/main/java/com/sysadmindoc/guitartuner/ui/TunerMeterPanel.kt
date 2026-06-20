package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.audio.TunerSessionState
import com.sysadmindoc.guitartuner.settings.MeterStyle
import com.sysadmindoc.guitartuner.settings.NoteNaming
import com.sysadmindoc.guitartuner.settings.PegTurnDirection
import com.sysadmindoc.guitartuner.tuning.GuitarString
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningStatus

@Composable
internal fun TunerMeterPanel(
    state: TunerSessionState,
    hasAudioPermission: Boolean,
    permissionPermanentlyDenied: Boolean,
    activeTuning: TuningDefinition,
    tuningMode: TuningMode,
    guidedStringNumber: Int,
    pegTurnDirections: Map<Int, PegTurnDirection>,
    noteNaming: NoteNaming = NoteNaming.Scientific,
    meterStyle: MeterStyle = MeterStyle.Normal,
    modifier: Modifier = Modifier,
) {
    val guidedTarget = if (tuningMode == TuningMode.Guided) {
        activeTuning.strings.firstOrNull { it.stringNumber == guidedStringNumber }
    } else {
        null
    }
    Surface(
        modifier = modifier,
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TargetString(
                state = state,
                guidedTarget = guidedTarget,
                activeTuning = activeTuning,
                tuningMode = tuningMode,
                noteNaming = noteNaming,
            )
            when (meterStyle) {
                MeterStyle.Normal -> CentsMeter(state)
                MeterStyle.Strobe -> StrobeMeter(state)
            }
            PitchHistoryTimeline(state)
            FrequencyReadout(
                state = state,
                hasAudioPermission = hasAudioPermission,
                permissionPermanentlyDenied = permissionPermanentlyDenied,
                pegTurnDirections = pegTurnDirections,
                guidedTarget = guidedTarget,
            )
            TrustSignal()
        }
    }
}

@Composable
private fun TargetString(
    state: TunerSessionState,
    guidedTarget: GuitarString?,
    activeTuning: TuningDefinition,
    tuningMode: TuningMode,
    noteNaming: NoteNaming = NoteNaming.Scientific,
) {
    val measurement = state.measurement
    val target = measurement.target ?: guidedTarget
    val displayPitch = when (noteNaming) {
        NoteNaming.Scientific -> target?.scientificPitch
        NoteNaming.Solfege -> target?.solfegePitch
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = tuningMode.label(),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            val noteMaxSp = with(density) { (maxWidth / 3).toSp() }
            val noteFontSize = if (noteMaxSp < 92.sp) noteMaxSp else 92.sp
            Text(
                text = displayPitch ?: stringResource(R.string.target_auto_detect_short),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = noteFontSize,
                lineHeight = noteFontSize * 1.05f,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Text(
            text = when {
                measurement.target != null -> stringResource(
                    R.string.target_detected_summary,
                    measurement.target.stringNumber,
                    measurement.target.name,
                    formatOneDecimal(measurement.target.frequencyHz),
                )
                guidedTarget != null -> stringResource(
                    R.string.target_guided_prompt,
                    guidedTarget.stringNumber,
                    guidedTarget.scientificPitch,
                )
                else -> activeTuning.name
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun CentsMeter(state: TunerSessionState) {
    val cents = state.measurement.cents
    val boundedCents = (cents ?: 0.0).coerceIn(-50.0, 50.0)
    val resources = androidx.compose.ui.platform.LocalContext.current.resources
    @Suppress("LocalContextGetResourceValueCall")
    val accessibility = tuningMeterAccessibility(state.measurement) { resId, args ->
        if (args.isEmpty()) resources.getString(resId) else resources.getString(resId, *args)
    }
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val centerColor = MaterialTheme.colorScheme.primary
    val flatColor = MaterialTheme.colorScheme.primary
    val sharpColor = MaterialTheme.colorScheme.tertiary
    val markerColor = statusColor(state.measurement.status)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .semantics {
                    contentDescription = accessibility.contentDescription
                    stateDescription = accessibility.stateDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(accessibility.progressCents, -50f..50f)
                    liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height / 2f
                val left = 16.dp.toPx()
                val right = size.width - 16.dp.toPx()
                val centerX = size.width / 2f
                drawLine(
                    color = trackColor,
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                for (tick in -50..50 step 5) {
                    val fraction = (tick + 50) / 100f
                    val x = left + fraction * (right - left)
                    val isMajor = tick % 10 == 0
                    val tickHeight = if (isMajor) 28.dp.toPx() else 18.dp.toPx()
                    val tickColor = when {
                        tick < 0 -> flatColor
                        tick > 0 -> sharpColor
                        else -> centerColor
                    }
                    drawLine(
                        color = tickColor.copy(alpha = if (isMajor) 0.9f else 0.56f),
                        start = Offset(x, y - tickHeight / 2f),
                        end = Offset(x, y + tickHeight / 2f),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                drawLine(
                    color = centerColor,
                    start = Offset(centerX, y - 38.dp.toPx()),
                    end = Offset(centerX, y + 38.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                val markerX = centerX + (boundedCents / 50.0).toFloat() * ((right - left) / 2f)
                drawLine(
                    color = markerColor,
                    start = Offset(markerX, y - 31.dp.toPx()),
                    end = Offset(markerX, y + 31.dp.toPx()),
                    strokeWidth = 7.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.meter_flat),
                style = MaterialTheme.typography.labelMedium,
                color = flatColor,
            )
            Text(
                text = stringResource(R.string.meter_in_tune),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.meter_sharp),
                style = MaterialTheme.typography.labelMedium,
                color = sharpColor,
            )
        }
    }
}

@Composable
private fun StrobeMeter(state: TunerSessionState) {
    val hasDetection = state.measurement.status == TuningStatus.InTune ||
        state.measurement.status == TuningStatus.TuneUp ||
        state.measurement.status == TuningStatus.TuneDown

    val currentCents = remember { mutableFloatStateOf(0f) }
    currentCents.floatValue = (state.measurement.cents ?: 0.0).coerceIn(-50.0, 50.0).toFloat()
    val phase = remember { mutableFloatStateOf(0f) }
    val bandColor = statusColor(state.measurement.status)
    val inTuneColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(hasDetection) {
        if (!hasDetection) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos > 0L) {
                    val dtSec = (nanos - lastNanos) / 1_000_000_000f
                    val speed = (currentCents.floatValue / 50f) * StrobeMaxSpeed
                    phase.floatValue = (phase.floatValue + speed * dtSec) % (2f * Math.PI.toFloat())
                }
                lastNanos = nanos
            }
        }
    }

    val resources = androidx.compose.ui.platform.LocalContext.current.resources
    @Suppress("LocalContextGetResourceValueCall")
    val accessibility = tuningMeterAccessibility(state.measurement) { resId, args ->
        if (args.isEmpty()) resources.getString(resId) else resources.getString(resId, *args)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .semantics {
                    contentDescription = accessibility.contentDescription
                    stateDescription = accessibility.stateDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(accessibility.progressCents, -50f..50f)
                    liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bandWidth = 24.dp.toPx()
                val numBands = (size.width / bandWidth).toInt() + 2
                val phaseOffset = phase.floatValue / (2f * Math.PI.toFloat()) * bandWidth
                for (i in -1..numBands) {
                    val x = i * bandWidth + phaseOffset
                    val brightness = (kotlin.math.cos(
                        2.0 * Math.PI * (x / bandWidth),
                    ).toFloat() + 1f) / 2f
                    drawRect(
                        color = bandColor.copy(alpha = brightness * 0.7f),
                        topLeft = Offset(x, 0f),
                        size = androidx.compose.ui.geometry.Size(bandWidth / 2f, size.height),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.meter_flat),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.meter_in_tune),
                style = MaterialTheme.typography.labelMedium,
                color = inTuneColor,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.meter_sharp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

private const val StrobeMaxSpeed = 12f

@Composable
private fun PitchHistoryTimeline(state: TunerSessionState) {
    val buffer = remember { FloatArray(HistoryMaxSamples) }
    val head = remember { mutableIntStateOf(0) }
    val count = remember { mutableIntStateOf(0) }
    val cents = state.measurement.cents
    val hasDetection = state.measurement.status == TuningStatus.InTune ||
        state.measurement.status == TuningStatus.TuneUp ||
        state.measurement.status == TuningStatus.TuneDown ||
        state.measurement.status == TuningStatus.Overshoot

    LaunchedEffect(cents, hasDetection) {
        if (hasDetection && cents != null) {
            val writeIndex = (head.intValue + count.intValue) % HistoryMaxSamples
            buffer[writeIndex] = cents.coerceIn(-50.0, 50.0).toFloat()
            if (count.intValue < HistoryMaxSamples) {
                count.intValue++
            } else {
                head.intValue = (head.intValue + 1) % HistoryMaxSamples
            }
        }
    }

    LaunchedEffect(state.isListening) {
        if (!state.isListening) {
            head.intValue = 0
            count.intValue = 0
        }
    }

    if (count.intValue < 2) return

    val lineColor = MaterialTheme.colorScheme.primary
    val zeroColor = MaterialTheme.colorScheme.outlineVariant

    val lastIndex = (head.intValue + count.intValue - 1) % HistoryMaxSamples
    val lastCents = buffer[lastIndex]
    val trendDescription = stringResource(
        when {
            kotlin.math.abs(lastCents) <= 5f -> R.string.a11y_in_tune
            lastCents < 0f -> R.string.a11y_pitch_flat
            else -> R.string.a11y_pitch_sharp
        },
    )

    val currentHead = head.intValue
    val currentCount = count.intValue

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .semantics {
                contentDescription = trendDescription
            },
    ) {
        val pad = 8.dp.toPx()
        val left = pad
        val right = size.width - pad
        val top = 2.dp.toPx()
        val bottom = size.height - 2.dp.toPx()
        val centerY = (top + bottom) / 2f
        val width = right - left
        val height = bottom - top

        drawLine(
            color = zeroColor,
            start = Offset(left, centerY),
            end = Offset(right, centerY),
            strokeWidth = 1.dp.toPx(),
        )

        val path = Path()
        for (i in 0 until currentCount) {
            val bufferIndex = (currentHead + i) % HistoryMaxSamples
            val x = left + (i.toFloat() / (HistoryMaxSamples - 1)) * width
            val y = centerY - (buffer[bufferIndex] / 50f) * (height / 2f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

private const val HistoryMaxSamples = 120
