package com.example.crypto_tracker.ui.settings

import androidx.lifecycle.viewModelScope
import com.example.crypto_tracker.auth.GoogleSignIn
import com.example.crypto_tracker.auth.SignInResult
import com.example.crypto_tracker.data.preferences.SessionStore
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.data.preferences.ThemeStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeThemeStore(var mode: ThemeMode = ThemeMode.SYSTEM) : ThemeStore {
        override fun getThemeMode() = mode
        override fun setThemeMode(mode: ThemeMode) { this.mode = mode }
    }

    private class FakeSessionStore(
        private var email: String? = null,
        private var name: String? = null
    ) : SessionStore {
        var consentSaved = false
        override fun isLoggedIn() = email != null
        override fun getUserName() = name
        override fun getUserEmail() = email
        override fun saveUserSession(email: String, name: String) { this.email = email; this.name = name }
        override fun savePrivacyConsent() { consentSaved = true }
        override fun clearSession() { email = null; name = null }
    }

    private val themeStore = FakeThemeStore()
    private val sessionStore = FakeSessionStore()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(session: FakeSessionStore = sessionStore) = SettingsViewModel(themeStore, session)

    @Test
    fun loadsSignedOutByDefault() {
        assertEquals(AccountState.SignedOut, viewModel().uiState.value.account)
    }

    @Test
    fun loadsStoredAccount() {
        val vm = viewModel(FakeSessionStore(email = "a@b.com", name = null))

        assertEquals(AccountState.SignedIn("Authenticated User", "a@b.com"), vm.uiState.value.account)
    }

    @Test
    fun setThemeMode_persistsAndUpdatesState() {
        val vm = viewModel()

        vm.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, themeStore.mode)
        assertEquals(ThemeMode.DARK, vm.uiState.value.themeMode)
    }

    @Test
    fun signIn_isBlockedUntilPrivacyIsAccepted() = runTest(dispatcher) {
        val vm = viewModel()
        var called = false

        vm.signIn { called = true; SignInResult.Cancelled }
        dispatcher.scheduler.runCurrent()

        assertFalse(called)
        vm.viewModelScope.cancel()
    }

    @Test
    fun signIn_successStoresSessionAndConsent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.setPrivacyAccepted(true)

        vm.signIn(GoogleSignIn { SignInResult.Success("a@b.com", "Ana") })
        dispatcher.scheduler.runCurrent()

        assertEquals(AccountState.SignedIn("Ana", "a@b.com"), vm.uiState.value.account)
        assertTrue(sessionStore.consentSaved)
        assertFalse(vm.uiState.value.isSigningIn)
        vm.viewModelScope.cancel()
    }

    @Test
    fun signIn_failureEmitsMessageAndStaysSignedOut() = runTest(dispatcher) {
        val vm = viewModel()
        vm.setPrivacyAccepted(true)
        var message: SettingsEvent? = null
        val job = launch(dispatcher) { message = vm.events.first() }
        dispatcher.scheduler.runCurrent()

        vm.signIn(GoogleSignIn { SignInResult.Failed("boom") })
        dispatcher.scheduler.runCurrent()

        assertEquals(SettingsEvent.Message("boom", isLong = true), message)
        assertEquals(AccountState.SignedOut, vm.uiState.value.account)
        job.cancel()
        vm.viewModelScope.cancel()
    }

    @Test
    fun signOut_clearsSession() {
        val vm = viewModel(FakeSessionStore(email = "a@b.com", name = "Ana"))

        vm.signOut()

        assertEquals(AccountState.SignedOut, vm.uiState.value.account)
    }
}
