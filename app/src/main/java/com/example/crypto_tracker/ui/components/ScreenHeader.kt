package com.example.crypto_tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.crypto_tracker.config.AppConfig

/**
 * Standardized Header component used across all app screens for uniform title height, padding, typography,
 * and account badge. The AdMob banner is pinned once in the app Scaffold (see MainActivity).
 *
 * @param title Main headline text for the screen.
 * @param subtitle Descriptive subtitle text below headline.
 * @param modifier Layout modifier.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        if (AppConfig.Account.LOGIN_ENABLED) {
            UserAccountHeader()

            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
