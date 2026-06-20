package com.sysadmindoc.guitartuner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.R
import com.sysadmindoc.guitartuner.tuning.GuitarString
import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.TuningDefinition

@Composable
internal fun TuningEditorScreen(
    existingTuning: TuningDefinition?,
    onSave: (TuningDefinition) -> Unit,
    onDelete: ((String) -> Unit)?,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val isEditing = existingTuning != null
    var name by remember { mutableStateOf(existingTuning?.name ?: "") }
    var id by remember { mutableStateOf(existingTuning?.id ?: "") }
    var stringCount by remember { mutableIntStateOf(existingTuning?.strings?.size ?: 6) }
    val stringEntries = remember {
        mutableStateListOf<StringEntry>().apply {
            if (existingTuning != null) {
                addAll(existingTuning.strings.map {
                    StringEntry(it.name, it.scientificPitch, it.frequencyHz.toString())
                })
            } else {
                repeat(6) { add(StringEntry()) }
            }
        }
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun adjustStringCount(newCount: Int) {
        val clamped = newCount.coerceIn(1, 12)
        while (stringEntries.size < clamped) stringEntries.add(StringEntry())
        while (stringEntries.size > clamped) stringEntries.removeAt(stringEntries.lastIndex)
        stringCount = clamped
    }

    fun save() {
        val trimmedId = id.trim().lowercase().replace(Regex("[^a-z0-9_-]"), "_")
        if (trimmedId.length < 2) {
            errorMessage = "ID must be at least 2 characters."
            return
        }
        if (name.isBlank()) {
            errorMessage = "Name is required."
            return
        }
        if (GuitarTunings.builtIns.any { it.id == trimmedId }) {
            errorMessage = "Cannot use a built-in tuning ID."
            return
        }
        val strings = stringEntries.mapIndexedNotNull { index, entry ->
            val freq = entry.frequency.toDoubleOrNull()
            if (entry.note.isBlank() || entry.pitch.isBlank() || freq == null || freq !in 20.0..500.0) {
                errorMessage = "String ${index + 1}: check note, pitch, and frequency (20-500 Hz)."
                return
            }
            GuitarString(
                stringNumber = stringEntries.size - index,
                name = entry.note.trim(),
                scientificPitch = entry.pitch.trim(),
                frequencyHz = freq,
            )
        }
        if (strings.size != stringEntries.size) return
        errorMessage = null
        onSave(
            TuningDefinition(
                id = trimmedId,
                name = name.trim(),
                strings = strings,
                isBuiltIn = false,
            ),
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (isEditing) stringResource(R.string.editor_title_edit) else stringResource(R.string.editor_title_create),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.editor_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = id,
                onValueChange = { if (!isEditing) id = it },
                label = { Text(stringResource(R.string.editor_id_label)) },
                singleLine = true,
                enabled = !isEditing,
                modifier = Modifier.fillMaxWidth(),
            )
            NumericSettingRow(
                label = stringResource(R.string.editor_string_count),
                value = stringCount.toString(),
                decreaseLabel = "-",
                increaseLabel = "+",
                canDecrease = stringCount > 1,
                canIncrease = stringCount < 12,
                onDecrease = { adjustStringCount(stringCount - 1) },
                onIncrease = { adjustStringCount(stringCount + 1) },
            )
            for (i in stringEntries.indices) {
                val entry = stringEntries[i]
                Surface(
                    shape = PanelShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.editor_string_number, stringEntries.size - i),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = entry.note,
                                onValueChange = { stringEntries[i] = entry.copy(note = it) },
                                label = { Text(stringResource(R.string.editor_note)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = entry.pitch,
                                onValueChange = { stringEntries[i] = entry.copy(pitch = it) },
                                label = { Text(stringResource(R.string.editor_pitch)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = entry.frequency,
                                onValueChange = { stringEntries[i] = entry.copy(frequency = it) },
                                label = { Text("Hz") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = ::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MinTouchTarget),
                shape = PanelShape,
                contentPadding = CompactButtonPadding,
            ) {
                Text(stringResource(R.string.editor_save))
            }
            if (isEditing && onDelete != null) {
                OutlinedButton(
                    onClick = { onDelete(existingTuning!!.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = MinTouchTarget),
                    shape = PanelShape,
                    contentPadding = CompactButtonPadding,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.editor_delete))
                }
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MinTouchTarget),
                shape = PanelShape,
                contentPadding = CompactButtonPadding,
            ) {
                Text(stringResource(R.string.editor_cancel))
            }
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

private data class StringEntry(
    val note: String = "",
    val pitch: String = "",
    val frequency: String = "",
)
