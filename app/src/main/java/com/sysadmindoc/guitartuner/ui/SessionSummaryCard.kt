package com.sysadmindoc.guitartuner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sysadmindoc.guitartuner.tuning.GuitarString
import kotlin.math.abs

@Composable
internal fun SessionSummaryCard(
    strings: List<GuitarString>,
    results: Map<Int, Double>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) return

    Surface(
        modifier = modifier,
        shape = PanelShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(PanelBorderWidth, MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Tuning complete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            for (string in strings.sortedByDescending { it.stringNumber }) {
                val cents = results[string.stringNumber]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${string.stringNumber} ${string.scientificPitch}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (cents != null) {
                            val sign = if (cents >= 0) "+" else ""
                            "$sign${formatOneDecimal(cents)} cents"
                        } else {
                            "--"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cents != null && abs(cents) <= 5.0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                    )
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = PanelShape,
                contentPadding = CompactButtonPadding,
            ) {
                Text("OK")
            }
        }
    }
}
