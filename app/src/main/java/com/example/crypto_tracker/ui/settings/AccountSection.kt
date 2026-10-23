package com.example.crypto_tracker.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.crypto_tracker.R
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.ui.components.GlassCard
import com.example.crypto_tracker.ui.components.GlassDefaults

/** Account block: profile + sign out when signed in, consent + Google sign-in otherwise. */
@Composable
internal fun AccountSection(
    account: AccountState,
    privacyAccepted: Boolean,
    isSigningIn: Boolean,
    canSignIn: Boolean,
    onPrivacyAcceptedChange: (Boolean) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    SectionTitle("Account")
    Spacer(modifier = Modifier.height(8.dp))

    when (account) {
        is AccountState.SignedIn -> SignedInCard(account, onSignOut)
        AccountState.SignedOut -> SignedOutCard(
            privacyAccepted = privacyAccepted,
            isSigningIn = isSigningIn,
            canSignIn = canSignIn,
            onPrivacyAcceptedChange = onPrivacyAcceptedChange,
            onSignIn = onSignIn
        )
    }
}

@Composable
private fun SignedInCard(account: AccountState.SignedIn, onSignOut: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(GlassDefaults.CardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = account.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = account.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onSignOut,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Sign Out", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SignedOutCard(
    privacyAccepted: Boolean,
    isSigningIn: Boolean,
    canSignIn: Boolean,
    onPrivacyAcceptedChange: (Boolean) -> Unit,
    onSignIn: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(GlassDefaults.CardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoogleLogo(size = 24)
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Sign in to backup and sync your portfolio preferences securely across devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PrivacyConsent(
                accepted = privacyAccepted,
                onAcceptedChange = onPrivacyAcceptedChange
            )

            Button(
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSignIn,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                GoogleLogo(size = 20)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isSigningIn) "Signing in..." else "Sign in with Google",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PrivacyConsent(accepted: Boolean, onAcceptedChange: (Boolean) -> Unit) {
    val uriHandler = LocalUriHandler.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = GlassDefaults.InnerShape
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Privacy and data protection",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "We use your name and email only to authenticate your account and sync your preferences. We do not sell your personal data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { uriHandler.openUri(AppConfig.Legal.PRIVACY_POLICY_URL) }) {
                    Text("Privacy Policy")
                }
                TextButton(onClick = { uriHandler.openUri(AppConfig.Legal.TERMS_OF_USE_URL) }) {
                    Text("Terms of Use")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = onAcceptedChange)
                Text(
                    text = "I accept the Privacy Policy and Terms of Use.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GoogleLogo(size: Int) {
    Image(
        painter = painterResource(id = R.drawable.ic_google_logo),
        contentDescription = "Google Logo",
        modifier = Modifier.size(size.dp)
    )
}
