package com.example.crypto_tracker.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.crypto_tracker.R
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.ui.components.GlassCard

private data class ThemeOption(val mode: ThemeMode, @StringRes val title: Int, @StringRes val subtitle: Int)

private val ThemeOptions = listOf(
    ThemeOption(ThemeMode.SYSTEM, R.string.theme_system_title, R.string.theme_system_subtitle),
    ThemeOption(ThemeMode.LIGHT, R.string.theme_light_title, R.string.theme_light_subtitle),
    ThemeOption(ThemeMode.DARK, R.string.theme_dark_title, R.string.theme_dark_subtitle)
)

/** Radio group to choose between system, light and dark appearance. */
@Composable
internal fun ThemeSection(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    SectionTitle(stringResource(R.string.theme_title))
    Text(
        text = stringResource(R.string.theme_description),
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
                OptionRow(
                    title = stringResource(option.title),
                    subtitle = stringResource(option.subtitle),
                    selected = option.mode == selected,
                    onClick = { onSelected(option.mode) }
                )
            }
        }
    }
}
