package com.example.crypto_tracker.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Outcome of a sign-in attempt. */
sealed interface SignInResult {
    data class Success(val email: String, val displayName: String) : SignInResult

    /** The user dismissed the account picker; nothing should change. */
    data object Cancelled : SignInResult

    data class Failed(val reason: String) : SignInResult
}

/** Abstraction over the sign-in mechanism so ViewModels can be tested with a fake. */
fun interface GoogleSignIn {
    suspend fun signIn(): SignInResult
}

/**
 * Credential Manager implementation. Needs an Activity-backed [context] to show the account picker,
 * so create it in the UI layer and pass it to the ViewModel per call - never store it there.
 */
class GoogleSignInClient(
    private val context: Context,
    private val webClientId: String
) : GoogleSignIn {

    override suspend fun signIn(): SignInResult {
        return try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .build()
                )
                .build()

            val credential = CredentialManager.create(context).getCredential(context, request).credential
            toSignInResult(credential)
        } catch (_: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (e: Exception) {
            Log.w(TAG, "Google sign-in failed", e)
            SignInResult.Failed(SIGN_IN_FAILED_MESSAGE)
        }
    }

    private companion object {
        const val TAG = "GoogleSignIn"
        const val SIGN_IN_FAILED_MESSAGE = "Couldn't sign you in. Check your connection and try again."
    }

    private fun toSignInResult(credential: androidx.credentials.Credential): SignInResult {
        val isGoogleIdToken = credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        if (!isGoogleIdToken) return SignInResult.Failed(SIGN_IN_FAILED_MESSAGE)

        val google = GoogleIdTokenCredential.createFrom((credential as CustomCredential).data)
        return SignInResult.Success(
            email = google.id,
            displayName = google.displayName ?: google.id.substringBefore("@")
        )
    }
}
