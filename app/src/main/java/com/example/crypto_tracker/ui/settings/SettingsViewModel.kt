package com.example.crypto_tracker.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.crypto_tracker.auth.GoogleSignIn
import com.example.crypto_tracker.auth.SignInResult
import com.example.crypto_tracker.data.preferences.SessionManager
import com.example.crypto_tracker.data.preferences.SessionStore
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.data.preferences.ThemePreferences
import com.example.crypto_tracker.data.preferences.ThemeStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AccountState {
    data object SignedOut : AccountState
    data class SignedIn(val displayName: String, val email: String) : AccountState
}

data class SettingsUiState(
    val themeMode: ThemeMode,
    val account: AccountState,
    val privacyAccepted: Boolean = false,
    val isSigningIn: Boolean = false
) {
    /** Sign-in requires explicit consent and must not run twice at once. */
    val canSignIn: Boolean get() = privacyAccepted && !isSigningIn
}

/** One-off messages shown as toasts by the screen. */
sealed interface SettingsEvent {
    data class Message(val text: String, val isLong: Boolean = false) : SettingsEvent
}

/** Owns theme choice and account/session state for the Settings screen. */
class SettingsViewModel(
    private val themeStore: ThemeStore,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(themeMode = themeStore.getThemeMode(), account = loadAccount())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun setThemeMode(mode: ThemeMode) {
        themeStore.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setPrivacyAccepted(accepted: Boolean) {
        _uiState.update { it.copy(privacyAccepted = accepted) }
    }

    fun signIn(googleSignIn: GoogleSignIn) {
        if (!_uiState.value.canSignIn) return
        _uiState.update { it.copy(isSigningIn = true) }

        viewModelScope.launch {
            when (val result = googleSignIn.signIn()) {
                is SignInResult.Success -> onSignedIn(result)
                is SignInResult.Failed -> _events.tryEmit(SettingsEvent.Message(result.reason, isLong = true))
                SignInResult.Cancelled -> Unit
            }
            _uiState.update { it.copy(isSigningIn = false) }
        }
    }

    fun signOut() {
        sessionStore.clearSession()
        _uiState.update { it.copy(account = AccountState.SignedOut) }
    }

    private fun onSignedIn(result: SignInResult.Success) {
        sessionStore.savePrivacyConsent()
        sessionStore.saveUserSession(result.email, result.displayName)
        _uiState.update { it.copy(account = AccountState.SignedIn(result.displayName, result.email)) }
        _events.tryEmit(SettingsEvent.Message("Welcome, ${result.displayName}!"))
    }

    private fun loadAccount(): AccountState {
        val email = sessionStore.getUserEmail()
        if (!sessionStore.isLoggedIn() || email == null) return AccountState.SignedOut
        return AccountState.SignedIn(sessionStore.getUserName() ?: "Authenticated User", email)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                SettingsViewModel(ThemePreferences(app), SessionManager(app))
            }
        }
    }
}
