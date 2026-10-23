package com.example.crypto_tracker.ui.settings

import com.example.crypto_tracker.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.ui.components.GlassCard
import com.example.crypto_tracker.ui.components.GlassDefaults

private data class AppFeature(val icon: String, val title: String, val description: String)

@Composable
private fun appFeatures(): List<AppFeature> = listOf(
    AppFeature(
        icon = "🏆",
        title = stringResource(R.string.about_feature_rankings_title, AppConfig.Market.TOP_COINS_COUNT),
        description = stringResource(R.string.about_feature_rankings_description)
    ),
    AppFeature(
        icon = "🔔",
        title = stringResource(R.string.about_feature_notifications_title),
        description = stringResource(
            R.string.about_feature_notifications_description,
            AppConfig.Notifications.MAX_NOTIFIED_COINS
        )
    ),
    AppFeature(
        icon = "🔒",
        title = stringResource(R.string.about_feature_privacy_title),
        description = stringResource(R.string.about_feature_privacy_description)
    )
)

/** Short description of the app and its main features. */
@Composable
internal fun AboutSection() {
    SectionTitle(stringResource(R.string.about_title))
    Spacer(modifier = Modifier.height(8.dp))

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(GlassDefaults.CardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.about_purpose_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.about_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            appFeatures().forEach { feature -> FeatureRow(feature) }
        }
    }
}

@Composable
private fun FeatureRow(feature: AppFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = feature.icon, style = MaterialTheme.typography.bodyLarge)
        Column {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
