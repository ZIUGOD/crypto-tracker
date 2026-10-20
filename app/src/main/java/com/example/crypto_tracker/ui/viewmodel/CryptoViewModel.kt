package com.example.crypto_tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.data.repository.CryptoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UI State representing the state of the Home crypto market ranking screen.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val coins: List<CryptoCoin>,
        val lastUpdated: String
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * ViewModel responsible for managing business logic, state emission, and periodic auto-refresh
 * for [HomeScreen], ensuring a clean MVVM separation of concerns.
 */
class CryptoViewModel(
    private val repository: CryptoRepository = CryptoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    init {
        startPollingPrices()
    }

    /**
     * Starts a periodic polling loop that fetches top 10 cryptos every 60 seconds.
     */
    fun startPollingPrices() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                fetchCryptos()
                delay(60_000L) // 1 minute auto-refresh interval
            }
        }
    }

    /**
     * Triggers a manual refresh or retry.
     */
    fun refresh() {
        startPollingPrices()
    }

    private suspend fun fetchCryptos() {
        // Only show full loading state if we don't have data yet
        if (_uiState.value !is HomeUiState.Success) {
            _uiState.update { HomeUiState.Loading }
        }

        val result = repository.fetchTop10Cryptos()
        result.onSuccess { coins ->
            val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _uiState.update { HomeUiState.Success(coins, timeFormatted) }
        }.onFailure { error ->
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.update {
                    HomeUiState.Error(error.localizedMessage ?: "Failed to load market rankings.")
                }
            }
        }
    }
}
