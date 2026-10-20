package com.example.crypto_tracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.data.preferences.ThemePreferences

/**
 * Settings Screen allowing the user to configure app preferences such as Theme Mode (Light, Dark, System).
 *
 * @param modifier Layout modifier passed from parent scaffold padding.
 * @param onThemeChanged Callback invoked when theme mode changes so the activity can apply the new theme.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onThemeChanged: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val themePrefs = remember { ThemePreferences(context) }
    var currentThemeMode by remember { mutableStateOf(themePrefs.getThemeMode()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings ⚙️",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Customize your app preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "App Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose between Light, Dark, or System automatic mode",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .selectableGroup()
            ) {
                ThemeOptionRow(
                    title = "Automatic (System Default)",
                    subtitle = "Follows your device system theme",
                    selected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = {
                        currentThemeMode = ThemeMode.SYSTEM
                        themePrefs.setThemeMode(ThemeMode.SYSTEM)
                        onThemeChanged(ThemeMode.SYSTEM)
                    }
                )

                ThemeOptionRow(
                    title = "Light Theme",
                    subtitle = "Always use light appearance",
                    selected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = {
                        currentThemeMode = ThemeMode.LIGHT
                        themePrefs.setThemeMode(ThemeMode.LIGHT)
                        onThemeChanged(ThemeMode.LIGHT)
                    }
                )

                ThemeOptionRow(
                    title = "Dark Theme",
                    subtitle = "Always use dark appearance",
                    selected = currentThemeMode == ThemeMode.DARK,
                    onClick = {
                        currentThemeMode = ThemeMode.DARK
                        themePrefs.setThemeMode(ThemeMode.DARK)
                        onThemeChanged(ThemeMode.DARK)
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Handled by selectable modifier on Row
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}
