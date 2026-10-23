package com.example.crypto_tracker.ui.settings

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
import com.example.crypto_tracker.data.preferences.AppLanguage
import com.example.crypto_tracker.ui.components.GlassCard

/** Radio group listing every [AppLanguage]; a new language shows up here automatically. */
@Composable
internal fun LanguageSection(
    selected: AppLanguage,
    onSelected: (AppLanguage) -> Unit
) {
    SectionTitle(stringResource(R.string.language_title))
    Text(
        text = stringResource(R.string.language_description),
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
            AppLanguage.entries.forEach { language ->
                OptionRow(
                    title = language.nativeName,
                    subtitle = null,
                    selected = language == selected,
                    onClick = { onSelected(language) }
                )
            }
        }
    }
}
