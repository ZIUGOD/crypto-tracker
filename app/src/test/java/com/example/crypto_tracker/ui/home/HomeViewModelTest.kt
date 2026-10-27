package com.example.crypto_tracker.ui.home

import androidx.lifecycle.viewModelScope
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.cache.CachedCoins
import com.example.crypto_tracker.data.cache.CoinCache
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.data.repository.ICryptoRepository
import com.example.crypto_tracker.notification.PinnedCoinsStore
import com.example.crypto_tracker.notification.PriceNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.crypto_tracker.config.AppConfig.Timing
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.net.UnknownHostException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeRepository(var result: Result<List<CryptoCoin>>) : ICryptoRepository {
        var calls = 0
        override suspend fun fetchTopCryptos(): Result<List<CryptoCoin>> {
            calls++
            return result
        }
    }

    private class FakeStore(initial: Set<String> = emptySet(), private var initialized: Boolean = false) : PinnedCoinsStore {
        private var ids = initial
        override fun getSelectedCoinIds() = ids
        override fun setSelectedCoinIds(coinIds: Set<String>) {
            ids = coinIds
            initialized = true
        }
        override fun ensureDefaultSelectionFromTop1(top1CoinId: String): Set<String> {
            if (!initialized) setSelectedCoinIds(setOf(top1CoinId))
            return ids
        }
    }

    private class FakeNotifier : PriceNotifier {
        val shown = mutableListOf<Set<String>>()
        override fun showPortfolioNotification(coins: List<CryptoCoin>, selectedCoinIds: Set<String>) {
            shown += selectedCoinIds
        }
    }

    private fun coin(id: String, rank: Int) = CryptoCoin(id, rank, id.uppercase(), id, 100.0, 1.0)

    private val coins = (1..8).map { coin("coin$it", it) }
    private val repository = FakeRepository(Result.success(coins))
    private val notifier = FakeNotifier()
    private val appActive = MutableStateFlow(true)

    private fun viewModel(store: PinnedCoinsStore = FakeStore(), repo: ICryptoRepository = repository) =
        HomeViewModel(repo, store, notifier, appActive)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun HomeViewModel.settle() {
        dispatcher.scheduler.runCurrent()
    }

    @Test
    fun startsLoading() {
        val vm = viewModel()
        assertTrue(vm.uiState.value is HomeUiState.Loading)
        vm.viewModelScope.cancel()
    }

    @Test
    fun emitsSuccessWithCoins() = runTest(dispatcher) {
        val vm = viewModel()
        vm.settle()

        assertEquals(coins, (vm.uiState.value as HomeUiState.Success).coins)
        vm.viewModelScope.cancel()
    }

    @Test
    fun emitsErrorWhenFirstLoadFails() = runTest(dispatcher) {
        val vm = viewModel(repo = FakeRepository(Result.failure(Exception("boom"))))
        vm.settle()

        assertEquals(HomeUiState.Error(HomeError.UNAVAILABLE), vm.uiState.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun reportsNoConnectionWhenTheDeviceIsOffline() = runTest(dispatcher) {
        val vm = viewModel(repo = FakeRepository(Result.failure(UnknownHostException("api.coingecko.com"))))
        vm.settle()

        assertEquals(HomeUiState.Error(HomeError.NO_CONNECTION), vm.uiState.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun serverFailuresAreNotReportedAsNoConnection() {
        assertEquals(HomeError.UNAVAILABLE, java.io.IOException("HTTP 429 from https://x").toHomeError())
        assertEquals(HomeError.NO_CONNECTION, java.net.SocketTimeoutException().toHomeError())
    }

    @Test
    fun keepsLastDataWhenARefreshFails() = runTest(dispatcher) {
        val repo = FakeRepository(Result.success(coins))
        val vm = viewModel(repo = repo)
        vm.settle()

        repo.result = Result.failure(Exception("offline"))
        vm.refresh()
        vm.settle()

        assertTrue(vm.uiState.value is HomeUiState.Success)
        vm.viewModelScope.cancel()
    }

    @Test
    fun pinsTheTopRankedCoinOnFirstLaunch() = runTest(dispatcher) {
        val vm = viewModel(store = FakeStore())
        vm.settle()

        assertEquals(setOf("coin1"), vm.pinnedCoinIds.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun keepsTheUsersSelectionAfterFirstLaunch() = runTest(dispatcher) {
        val vm = viewModel(store = FakeStore(initial = setOf("coin5"), initialized = true))
        vm.settle()

        assertEquals(setOf("coin5"), vm.pinnedCoinIds.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun togglePinned_addsAndRemoves() = runTest(dispatcher) {
        val store = FakeStore(initial = setOf("coin1"), initialized = true)
        val vm = viewModel(store = store)
        vm.settle()

        vm.togglePinned("coin2")
        assertEquals(setOf("coin1", "coin2"), store.getSelectedCoinIds())

        vm.togglePinned("coin1")
        assertEquals(setOf("coin2"), vm.pinnedCoinIds.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun togglePinned_refreshesNotifications() = runTest(dispatcher) {
        val vm = viewModel(store = FakeStore(setOf("coin1"), initialized = true))
        vm.settle()
        notifier.shown.clear()

        vm.togglePinned("coin2")

        assertEquals(listOf(setOf("coin1", "coin2")), notifier.shown)
        vm.viewModelScope.cancel()
    }

    @Test
    fun togglePinned_rejectsBeyondTheLimitAndEmitsEvent() = runTest(dispatcher) {
        val max = AppConfig.Notifications.MAX_NOTIFIED_COINS
        val full = coins.take(max).map { it.id }.toSet()
        val vm = viewModel(store = FakeStore(initial = full, initialized = true))
        vm.settle()

        var event: HomeEvent? = null
        val job = launch(dispatcher) { event = vm.events.first() }
        dispatcher.scheduler.runCurrent()

        vm.togglePinned("coin8")
        dispatcher.scheduler.runCurrent()

        assertEquals(full, vm.pinnedCoinIds.value)
        assertEquals(HomeEvent.PinLimitReached(max), event)
        job.cancel()
        vm.viewModelScope.cancel()
    }

    @Test
    fun toggleExpanded_isAnAccordion() {
        val vm = viewModel()

        vm.toggleExpanded("coin1")
        assertEquals("coin1", vm.expandedCoinId.value)

        vm.toggleExpanded("coin2")
        assertEquals("coin2", vm.expandedCoinId.value)

        vm.toggleExpanded("coin2")
        assertNull(vm.expandedCoinId.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun polls_everyIntervalWhileTheAppIsActive() = runTest(dispatcher) {
        val repo = FakeRepository(Result.success(coins))
        val vm = viewModel(repo = repo)
        vm.settle()
        assertEquals(1, repo.calls)

        dispatcher.scheduler.advanceTimeBy(Timing.UI_POLLING_INTERVAL_MS + 1)
        dispatcher.scheduler.runCurrent()

        assertEquals(2, repo.calls)
        vm.viewModelScope.cancel()
    }

    @Test
    fun polling_pausesInBackgroundAndResumesInForeground() = runTest(dispatcher) {
        val repo = FakeRepository(Result.success(coins))
        val vm = viewModel(repo = repo)
        vm.settle()

        appActive.value = false
        vm.settle()
        dispatcher.scheduler.advanceTimeBy(Timing.UI_POLLING_INTERVAL_MS * 3)
        dispatcher.scheduler.runCurrent()
        assertEquals(1, repo.calls)

        appActive.value = true
        vm.settle()
        assertEquals(2, repo.calls)
        vm.viewModelScope.cancel()
    }

    private class FakeCache(var cached: CachedCoins?) : CoinCache {
        var saved: List<CryptoCoin>? = null
        override fun load() = cached
        override fun save(coins: List<CryptoCoin>, savedAtMillis: Long) {
            saved = coins
        }
    }

    private fun viewModelWithCache(cache: CoinCache, repo: ICryptoRepository = repository) =
        HomeViewModel(repo, FakeStore(), notifier, appActive, cache)

    @Test
    fun showsCachedCoinsImmediatelyAsStale() {
        val cached = coins.take(3)
        val vm = viewModelWithCache(FakeCache(CachedCoins(cached, System.currentTimeMillis())))

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(cached, state.coins)
        assertTrue(state.isStale)
        vm.viewModelScope.cancel()
    }

    @Test
    fun replacesCachedCoinsWithFreshOnesAfterFetch() = runTest(dispatcher) {
        val vm = viewModelWithCache(FakeCache(CachedCoins(coins.take(3), System.currentTimeMillis())))
        vm.settle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(coins, state.coins)
        assertTrue(!state.isStale)
        vm.viewModelScope.cancel()
    }

    @Test
    fun keepsCachedCoinsWhenTheFirstFetchFails() = runTest(dispatcher) {
        val cached = coins.take(3)
        val vm = viewModelWithCache(
            FakeCache(CachedCoins(cached, System.currentTimeMillis())),
            FakeRepository(Result.failure(Exception("offline")))
        )
        vm.settle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(cached, state.coins)
        assertTrue(state.isStale)
        vm.viewModelScope.cancel()
    }
}
