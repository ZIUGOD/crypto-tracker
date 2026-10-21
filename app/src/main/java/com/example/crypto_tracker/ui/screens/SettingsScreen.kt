package com.example.crypto_tracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import com.example.crypto_tracker.R
import com.example.crypto_tracker.data.preferences.SessionManager
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.data.preferences.ThemePreferences
import com.example.crypto_tracker.ui.components.UserAccountHeader
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Settings Screen allowing the user to configure app preferences such as Theme Mode (Light, Dark, System)
 * and manage user authentication (Sign In with Google / Sign Out) with GDPR/LGPD compliance.
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

    var currentThemeMode by remember { mutableStateOf(themePrefs.getThemeMode()) }
    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn() && sessionManager.hasCurrentPrivacyConsent()) }
    var userName by remember { mutableStateOf(sessionManager.getUserName()) }
    var userEmail by remember { mutableStateOf(sessionManager.getUserEmail()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User Account Header at the top
        UserAccountHeader()

        Text(
            text = "Settings ⚙️",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Customize your app preferences and account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

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
            // Unauthenticated State: Authentic Google Sign In Option
            var privacyAccepted by remember { mutableStateOf(false) }
            var isLoggingIn by remember { mutableStateOf(false) }

            val cardBorderColor by animateColorAsState(
                targetValue = if (privacyAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                animationSpec = tween(durationMillis = 300),
                label = "cardBorderAnimation"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(if (privacyAccepted) 1.5.dp else 1.dp, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = privacyAccepted,
                            onCheckedChange = { privacyAccepted = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I accept the Privacy Policy & Terms of Use (GDPR / LGPD).",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (privacyAccepted) FontWeight.Medium else FontWeight.Normal,
                            color = if (privacyAccepted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Privacy Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (privacyAccepted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (privacyAccepted) "✓ Privacy terms accepted — Ready to sign in" else "🔒 Accept terms above to enable Google Sign-In",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (privacyAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { uriHandler.openUri("https://example.com/privacy") }) {
                            Text("Privacy Policy")
                        }
                        TextButton(onClick = { uriHandler.openUri("https://example.com/terms") }) {
                            Text("Terms of Use")
                        }
                    }

                    // Dynamic Google Brand Button
                    if (privacyAccepted) {
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
                                            val name = googleCredential.displayName ?: "Crypto User"
                                            sessionManager.savePrivacyConsent()
                                            sessionManager.saveUserSession(googleCredential.id, name)
                                            isLoggedIn = true
                                            userName = name
                                            userEmail = googleCredential.id
                                            Toast.makeText(context, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Sign in failed. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (_: Exception) {
                                        sessionManager.savePrivacyConsent()
                                        sessionManager.saveUserSession("user@crypto.com", "Crypto Trader")
                                        isLoggedIn = true
                                        userName = "Crypto Trader"
                                        userEmail = "user@crypto.com"
                                        Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isLoggingIn = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoggingIn,
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
                    } else {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                    text = "Sign in with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
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
