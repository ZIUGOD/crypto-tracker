package com.example.crypto_tracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.ui.components.GlassCard

private data class ThemeOption(val mode: ThemeMode, val title: String, val subtitle: String)

private val ThemeOptions = listOf(
    ThemeOption(ThemeMode.SYSTEM, "Automatic (System Default)", "Follows your device system theme"),
    ThemeOption(ThemeMode.LIGHT, "Light Theme", "Always use light appearance"),
    ThemeOption(ThemeMode.DARK, "Dark Theme", "Always use dark appearance")
)

/** Radio group to choose between system, light and dark appearance. */
@Composable
internal fun ThemeSection(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    SectionTitle("App Theme")
    Text(
        text = "Choose between Light, Dark, or System automatic mode",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .selectableGroup()
        ) {
            ThemeOptions.forEach { option ->
                ThemeOptionRow(
                    title = option.title,
                    subtitle = option.subtitle,
                    selected = option.mode == selected,
                    onClick = { onSelected(option.mode) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Handled by the selectable modifier on the row
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
