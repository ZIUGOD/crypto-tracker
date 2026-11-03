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
import com.example.crypto_tracker.data.preferences.AppTheme
import com.example.crypto_tracker.ui.components.GlassCard

private data class AppThemeTexts(@StringRes val title: Int, @StringRes val subtitle: Int)

private fun AppTheme.texts() = when (this) {
    AppTheme.LITE -> AppThemeTexts(R.string.app_theme_lite_title, R.string.app_theme_lite_subtitle)
    AppTheme.GLASS -> AppThemeTexts(R.string.app_theme_glass_title, R.string.app_theme_glass_subtitle)
}

/** Radio group to choose the visual style (Lite or Glass). Lists only themes that are available. */
@Composable
internal fun AppThemeSection(
    selected: AppTheme,
    onSelected: (AppTheme) -> Unit
) {
    SectionTitle(stringResource(R.string.app_theme_title))
    Text(
        text = stringResource(R.string.app_theme_description),
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
            AppTheme.selectable.forEach { theme ->
                val texts = theme.texts()
                OptionRow(
                    title = stringResource(texts.title),
                    subtitle = stringResource(texts.subtitle),
                    selected = theme == selected,
                    onClick = { onSelected(theme) }
                )
            }
        }
    }
}
