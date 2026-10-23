package com.example.crypto_tracker.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crypto_tracker.R
import com.example.crypto_tracker.auth.GoogleSignInClient
import com.example.crypto_tracker.data.preferences.AppLanguage
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.ui.components.GlassDefaults
import com.example.crypto_tracker.ui.components.ScreenHeader

/**
 * Settings screen: theme mode, language, Google account and app information.
 * Stateful entry point; each section is a stateless composable in its own file.
 *
 * @param onThemeChanged Lets the activity re-theme immediately when the mode changes.
 * @param onLanguageChanged Lets the activity reload its resources when the language changes.
 * @param onSignedOut Invoked after the user signs out.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onThemeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit = {},
    onSignedOut: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val webClientId = stringResource(R.string.google_web_client_id)
    val googleSignIn = remember(context, webClientId) { GoogleSignInClient(context, webClientId) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Welcome -> Toast.makeText(
                    context,
                    resources.getString(R.string.toast_welcome, event.displayName),
                    Toast.LENGTH_SHORT
                ).show()
                SettingsEvent.SignInFailed -> Toast.makeText(
                    context,
                    resources.getString(R.string.sign_in_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(GlassDefaults.ScreenPadding)
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_subtitle)
        )

        ThemeSection(
            selected = uiState.themeMode,
            onSelected = { mode ->
                viewModel.setThemeMode(mode)
                onThemeChanged(mode)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        LanguageSection(
            selected = uiState.language,
            onSelected = { language ->
                if (language != uiState.language) {
                    viewModel.setLanguage(language)
                    onLanguageChanged(language)
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        AccountSection(
            account = uiState.account,
            privacyAccepted = uiState.privacyAccepted,
            isSigningIn = uiState.isSigningIn,
            canSignIn = uiState.canSignIn,
            onPrivacyAcceptedChange = viewModel::setPrivacyAccepted,
            onSignIn = { viewModel.signIn(googleSignIn) },
            onSignOut = {
                viewModel.signOut()
                onSignedOut()
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        AboutSection()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** Section title used by every block on this screen. */
@Composable
internal fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}
