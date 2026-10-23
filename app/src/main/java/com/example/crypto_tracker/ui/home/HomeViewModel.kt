package com.example.crypto_tracker.ui.home

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.cache.CachedCoins
import com.example.crypto_tracker.data.cache.CoinCache
import com.example.crypto_tracker.data.cache.CoinCachePreferences
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.data.repository.CachingCryptoRepository
import com.example.crypto_tracker.data.repository.CryptoRepository
import com.example.crypto_tracker.data.repository.ICryptoRepository
import com.example.crypto_tracker.notification.CryptoNotificationManager
import com.example.crypto_tracker.notification.NotificationPreferences
import com.example.crypto_tracker.notification.PinnedCoinsStore
import com.example.crypto_tracker.notification.PriceNotifier
import com.example.crypto_tracker.notification.ensureDefaultSelection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** State of the market ranking list. */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val coins: List<CryptoCoin>,
        val lastUpdated: String,
        /** True while showing the locally saved list from an earlier session, not a fresh fetch. */
        val isStale: Boolean = false
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/** One-off messages for the UI (shown once, never re-delivered on recomposition). */
sealed interface HomeEvent {
    data class PinLimitReached(val max: Int) : HomeEvent
}

/**
 * Owns everything the Home screen needs: market polling, which coins are pinned to the
 * notification bar, which card is expanded, and the notifications themselves.
 * The screen only renders state and forwards user actions.
 */
class HomeViewModel(
    private val repository: ICryptoRepository,
    private val pinnedStore: PinnedCoinsStore,
    private val notifier: PriceNotifier,
    /** Emits true while the app is in the foreground; polling pauses otherwise to save battery and data. */
    private val isAppActive: Flow<Boolean>,
    /** Read once at start so the last known list shows instantly, even offline. */
    cache: CoinCache = CoinCache.None
) : ViewModel() {

    private val timeFormat = SimpleDateFormat(AppConfig.Timing.LAST_UPDATED_TIME_PATTERN, Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat(AppConfig.Timing.CACHED_DATE_TIME_PATTERN, Locale.getDefault())

    private val _uiState = MutableStateFlow<HomeUiState>(cache.load()?.toStaleState() ?: HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _pinnedCoinIds = MutableStateFlow(pinnedStore.getSelectedCoinIds())
    val pinnedCoinIds: StateFlow<Set<String>> = _pinnedCoinIds.asStateFlow()

    private val _expandedCoinId = MutableStateFlow<String?>(null)
    val expandedCoinId: StateFlow<String?> = _expandedCoinId.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    private val fetchMutex = Mutex()

    init {
        viewModelScope.launch {
            isAppActive.distinctUntilChanged().collectLatest { active ->
                if (active) pollWhileActive()
            }
        }
    }

    /** Fetches immediately, then every [AppConfig.Timing.UI_POLLING_INTERVAL_MS], until cancelled. */
    private suspend fun pollWhileActive() {
        while (true) {
            fetchCryptos()
            delay(AppConfig.Timing.UI_POLLING_INTERVAL_MS)
        }
    }

    /** Manual refresh or retry. */
    fun refresh() {
        viewModelScope.launch { fetchCryptos() }
    }

    /** Accordion behaviour: expanding a card collapses the previous one. */
    fun toggleExpanded(coinId: String) {
        _expandedCoinId.update { current -> if (current == coinId) null else coinId }
    }

    /** Pins or unpins [coinId], enforcing [AppConfig.Notifications.MAX_NOTIFIED_COINS]. */
    fun togglePinned(coinId: String) {
        val current = _pinnedCoinIds.value
        val max = AppConfig.Notifications.MAX_NOTIFIED_COINS
        when {
            coinId in current -> savePinned(current - coinId)
            current.size >= max -> _events.tryEmit(HomeEvent.PinLimitReached(max))
            else -> savePinned(current + coinId)
        }
    }

    private fun savePinned(coinIds: Set<String>) {
        _pinnedCoinIds.value = coinIds
        pinnedStore.setSelectedCoinIds(coinIds)
        (_uiState.value as? HomeUiState.Success)?.let { notifier.showPortfolioNotification(it.coins, coinIds) }
    }

    /** Serialized so a manual refresh never overlaps a polling tick. */
    private suspend fun fetchCryptos() = fetchMutex.withLock {
        // Only show the full loading state while there is no data yet.
        if (_uiState.value !is HomeUiState.Success) {
            _uiState.update { HomeUiState.Loading }
        }

        repository.fetchTopCryptos()
            .onSuccess(::onMarketDataLoaded)
            .onFailure(::onMarketDataFailed)
    }

    private fun onMarketDataLoaded(coins: List<CryptoCoin>) {
        _uiState.update { HomeUiState.Success(coins, currentTimeLabel()) }
        pinTopCoinOnFirstLaunch(coins)
        notifier.showPortfolioNotification(coins, _pinnedCoinIds.value)
    }

    private fun onMarketDataFailed(error: Throwable) {
        // Keep showing the last good data when a refresh fails.
        if (_uiState.value !is HomeUiState.Success) {
            _uiState.update { HomeUiState.Error(error.localizedMessage ?: "Failed to load market rankings.") }
        }
    }

    private fun pinTopCoinOnFirstLaunch(coins: List<CryptoCoin>) {
        if (coins.isEmpty()) return
        _pinnedCoinIds.value = pinnedStore.ensureDefaultSelection(coins)
    }

    private fun currentTimeLabel(): String = timeFormat.format(Date())

    private fun CachedCoins.toStaleState(): HomeUiState.Success {
        val day = SimpleDateFormat("yyyyMMdd", Locale.US)
        val sameDay = day.format(Date(savedAtMillis)) == day.format(Date())
        val format = if (sameDay) timeFormat else dateTimeFormat
        return HomeUiState.Success(coins, format.format(Date(savedAtMillis)), isStale = true)
    }

    companion object {
        private fun appInForeground(): Flow<Boolean> =
            ProcessLifecycleOwner.get().lifecycle.currentStateFlow
                .map { it.isAtLeast(Lifecycle.State.STARTED) }

        /** Production wiring. Tests construct [HomeViewModel] directly with fakes. */
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val cache = CoinCachePreferences(app)
                HomeViewModel(
                    repository = CachingCryptoRepository(CryptoRepository(), cache),
                    pinnedStore = NotificationPreferences(app),
                    notifier = CryptoNotificationManager(app),
                    isAppActive = appInForeground(),
                    cache = cache
                )
            }
        }
    }
}
