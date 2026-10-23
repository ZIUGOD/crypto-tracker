package com.example.crypto_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.data.preferences.ThemePreferences
import com.example.crypto_tracker.notification.CryptoPriceWorker
import com.example.crypto_tracker.ui.components.BannerAdView
import com.example.crypto_tracker.ui.components.GlassBackground
import com.example.crypto_tracker.ui.home.HomeScreen
import com.example.crypto_tracker.ui.news.NewsScreen
import com.example.crypto_tracker.ui.settings.SettingsScreen
import com.example.crypto_tracker.ui.theme.CryptotrackerTheme
import com.google.android.gms.ads.MobileAds
import java.util.concurrent.TimeUnit

/**
 * Main Activity entry point for the Crypto Tracker Android application.
 */
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handle permission grant/denial if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the Mobile Ads SDK on main thread
        MobileAds.initialize(this) {}

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Enqueue background worker using AppConfig interval
        val workRequest = PeriodicWorkRequestBuilder<CryptoPriceWorker>(
            AppConfig.Timing.BACKGROUND_SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "CryptoPriceSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        val themePrefs = ThemePreferences(this)

        setContent {
            var themeMode by remember { mutableStateOf(themePrefs.getThemeMode()) }
            val systemDark = isSystemInDarkTheme()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            CryptotrackerTheme(darkTheme = darkTheme) {
                CryptotrackerApp(
                    onThemeChanged = { newMode ->
                        themeMode = newMode
                    }
                )
            }
        }
    }
}

/**
 * Top-level application composable managing customizable bottom navigation scaffold and destination screens.
 *
 * @param onThemeChanged Callback invoked when theme mode changes.
 */
@PreviewScreenSizes
@Composable
fun CryptotrackerApp(
    onThemeChanged: (ThemeMode) -> Unit = {}
) {
    // Retains selected destination state across configuration changes
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // Back from News/Settings returns to Home; from Home it falls through and closes the app.
    BackHandler(enabled = currentDestination != AppDestinations.HOME) {
        currentDestination = AppDestinations.HOME
    }

    GlassBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                // Single pinned banner shared by all screens: stays fixed while content scrolls
                // and is not reloaded on every tab switch.
                BannerAdView(
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                AppNavigationBar(
                    current = currentDestination,
                    onSelect = { currentDestination = it }
                )
            }
        ) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.NEWS -> NewsScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.SETTINGS -> SettingsScreen(
                    modifier = Modifier.padding(innerPadding),
                    onThemeChanged = onThemeChanged
                )
            }
        }
    }
}

/**
 * Enumeration of main top-level navigation destinations in the application.
 *
 * @property label Localized display label for bottom navigation item.
 * @property icon Vector drawable resource ID for bottom navigation item.
 */
enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    NEWS("News", R.drawable.ic_news),
    SETTINGS("Settings", R.drawable.baseline_settings_24),
}
