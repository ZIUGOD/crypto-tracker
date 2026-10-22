package com.example.crypto_tracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.crypto_tracker.R
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.preferences.SessionManager
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.data.preferences.ThemePreferences
import com.example.crypto_tracker.ui.components.ScreenHeader
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Settings Screen allowing the user to configure app preferences such as Theme Mode (Light, Dark, System),
 * manage user authentication (Sign In with Google / Sign Out), and view app purpose information.
 *
 * @param modifier Layout modifier passed from parent scaffold padding.
 * @param onThemeChanged Callback invoked when theme mode changes so the activity can apply the new theme.
 * @param onSignedOut Callback invoked when user signs out.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onThemeChanged: (ThemeMode) -> Unit,
    onSignedOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val themePrefs = remember { ThemePreferences(context) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val webClientId = stringResource(R.string.google_web_client_id)
    val scrollState = rememberScrollState()

    var currentThemeMode by remember { mutableStateOf(themePrefs.getThemeMode()) }
    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
    var userName by remember { mutableStateOf(sessionManager.getUserName()) }
    var userEmail by remember { mutableStateOf(sessionManager.getUserEmail()) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Standardized Screen Header
        ScreenHeader(
            title = "Settings ⚙️",
            subtitle = "Customize your app preferences and account"
        )

        Text(
            text = "App Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Choose between Light, Dark, or System automatic mode",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
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

        Spacer(modifier = Modifier.height(28.dp))

        // Account / Sign In / Sign Out Section
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoggedIn && userEmail != null) {
            // Authenticated User State
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = userName ?: "Authenticated User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = userEmail!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    sessionManager.clearSession()
                    isLoggedIn = false
                    userName = null
                    userEmail = null
                    onSignedOut()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sign Out", fontWeight = FontWeight.Bold)
            }
        } else {
            // Unauthenticated State: Authentic Clean Google Sign In Option
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Google Brand Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Sign in to backup and sync your portfolio preferences securely across devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Privacy and data protection",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "We use your name and email only to authenticate your account and sync your preferences. We do not sell your personal data.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = privacyAccepted,
                                    onCheckedChange = { privacyAccepted = it }
                                )
                                Text(
                                    text = "I accept the Privacy Policy and Terms of Use.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        uriHandler.openUri(AppConfig.Legal.PRIVACY_POLICY_URL)
                                    }
                                ) {
                                    Text("Privacy Policy")
                                }
                                TextButton(
                                    onClick = {
                                        uriHandler.openUri(AppConfig.Legal.TERMS_OF_USE_URL)
                                    }
                                ) {
                                    Text("Terms of Use")
                                }
                            }
                        }
                    }

                    // Authentic Google Brand Button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoggingIn = true
                                try {
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(webClientId)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result: GetCredentialResponse = CredentialManager.create(context).getCredential(
                                        context = context,
                                        request = request
                                    )
                                    val credential = result.credential
                                    if (credential is androidx.credentials.CustomCredential &&
                                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    ) {
                                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val email = googleCredential.id
                                        val name = googleCredential.displayName ?: email.substringBefore("@")

                                        sessionManager.savePrivacyConsent()
                                        sessionManager.saveUserSession(email, name)
                                        isLoggedIn = true
                                        userName = name
                                        userEmail = email
                                        Toast.makeText(context, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Sign in failed. Please try again.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (_: GetCredentialCancellationException) {
                                    // User cancelled account picker
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Google Sign-In error: ${e.localizedMessage ?: "Service unavailable"}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isLoggingIn = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = privacyAccepted && !isLoggingIn,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isLoggingIn) "Signing in..." else "Sign in with Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // About / App Purpose Info Section
        Text(
            text = "About Crypto Tracker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "App Purpose Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "App Purpose & Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Crypto Tracker provides fast, hassle-free market visibility for active crypto traders and enthusiasts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                AppFeatureBullet(
                    icon = "🏆",
                    title = "Top 10 Live Rankings",
                    description = "Real-time market cap rankings updated automatically every 1 minute."
                )

                AppFeatureBullet(
                    icon = "🔔",
                    title = "Status Bar Notifications",
                    description = "Pin up to 5 favorite coins to check live prices instantly without opening the app."
                )

                AppFeatureBullet(
                    icon = "🔒",
                    title = "Privacy & Local Data",
                    description = "Your preferences are stored locally with optional Google Account sync."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AppFeatureBullet(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 16.sp)
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}
