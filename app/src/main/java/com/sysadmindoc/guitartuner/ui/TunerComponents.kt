package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.settings.StartupTuningMode
import com.sysadmindoc.guitartuner.settings.ThemeMode
import com.sysadmindoc.guitartuner.tuning.GuitarString
import com.sysadmindoc.guitartuner.tuning.TuningDefinition
import com.sysadmindoc.guitartuner.tuning.TuningMode

internal val PanelShape = RoundedCornerShape(8.dp)
internal val CompactButtonPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
internal val MinTouchTarget = 48.dp
internal val PanelBorderWidth = 1.dp

@Composable
internal fun SettingsSection(
    title: String,
    helper: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
internal fun ToggleSettingRow(
    label: String,
    helper: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val checkedState = if (checked) {
        stringResource(R.string.state_on)
    } else {
        stringResource(R.string.state_off)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTouchTarget),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = checkedState
            },
        )
    }
}

@Composable
internal fun NumericSettingRow(
    label: String,
    value: String,
    decreaseLabel: String,
    increaseLabel: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedButton(
            onClick = onDecrease,
            enabled = canDecrease,
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
            modifier = Modifier.defaultMinSize(minWidth = MinTouchTarget, minHeight = MinTouchTarget),
        ) {
            Text(decreaseLabel)
        }
        Text(
            text = value,
            modifier = Modifier.widthIn(min = 72.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(
            onClick = onIncrease,
            enabled = canIncrease,
            shape = PanelShape,
            contentPadding = CompactButtonPadding,
            modifier = Modifier.defaultMinSize(minWidth = MinTouchTarget, minHeight = MinTouchTarget),
        ) {
            Text(increaseLabel)
        }
    }
}

@Composable
internal fun SelectableOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = CompactButtonPadding,
) {
    val selectedState = if (selected) {
        stringResource(R.string.state_selected)
    } else {
        stringResource(R.string.state_not_selected)
    }
    val semanticsModifier = modifier.semantics {
        this.selected = selected
        stateDescription = selectedState
    }
    if (selected) {
        Button(
            onClick = onClick,
            modifier = semanticsModifier.defaultMinSize(minHeight = MinTouchTarget),
            shape = PanelShape,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.primary),
        ) {
            Text(label, maxLines = 2, textAlign = TextAlign.Center)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = semanticsModifier.defaultMinSize(minHeight = MinTouchTarget),
            shape = PanelShape,
            contentPadding = contentPadding,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Text(label, maxLines = 2, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun TuningChoiceButton(
    tuning: TuningDefinition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (tuning.isBuiltIn) {
        tuning.name
    } else {
        stringResource(R.string.tuning_custom_suffix, tuning.name)
    }
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
internal fun StartupModeButton(
    mode: StartupTuningMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = mode.label(),
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
    )
}

@Composable
internal fun ThemeModeButton(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = mode.label(),
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
    )
}

@Composable
internal fun TuningModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
internal fun GuidedStringButton(
    string: GuitarString,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = "${string.stringNumber} ${string.scientificPitch}"
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
    )
}

@Composable
internal fun PegDirectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableOptionButton(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
internal fun TuningChoiceGrid(
    tunings: List<TuningDefinition>,
    activeTuning: TuningDefinition,
    onTuningSelected: (TuningDefinition) -> Unit,
) {
    for (row in tunings.chunked(2)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (tuning in row) {
                TuningChoiceButton(
                    tuning = tuning,
                    selected = tuning.id == activeTuning.id,
                    onClick = { onTuningSelected(tuning) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun GuidedStringGrid(
    strings: List<GuitarString>,
    guidedStringNumber: Int,
    onGuidedStringSelected: (Int) -> Unit,
) {
    for (row in strings.chunked(3)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (string in row) {
                GuidedStringButton(
                    string = string,
                    selected = string.stringNumber == guidedStringNumber,
                    onClick = { onGuidedStringSelected(string.stringNumber) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(3 - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun StartupTuningMode.label(): String = when (this) {
    StartupTuningMode.StandardDefault -> stringResource(R.string.startup_standard)
    StartupTuningMode.LastUsed -> stringResource(R.string.startup_last)
    StartupTuningMode.Favorite -> stringResource(R.string.startup_favorite)
}

@Composable
internal fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> stringResource(R.string.theme_system)
    ThemeMode.Dark -> stringResource(R.string.theme_dark)
    ThemeMode.Light -> stringResource(R.string.theme_light)
}

@Composable
internal fun TuningMode.label(): String = when (this) {
    TuningMode.Auto -> stringResource(R.string.mode_auto)
    TuningMode.Guided -> stringResource(R.string.mode_guided)
    TuningMode.Chromatic -> stringResource(R.string.mode_chromatic)
}

internal fun GuitarString.walkthroughLabel(): String =
    "$name ($scientificPitch)"
