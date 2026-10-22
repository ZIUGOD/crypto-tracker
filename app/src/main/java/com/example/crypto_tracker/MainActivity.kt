package com.example.crypto_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.preferences.ThemeMode
import com.example.crypto_tracker.data.preferences.ThemePreferences
import com.example.crypto_tracker.notification.CryptoPriceWorker
import com.example.crypto_tracker.ui.screens.HomeScreen
import com.example.crypto_tracker.ui.screens.NewsScreen
import com.example.crypto_tracker.ui.screens.SettingsScreen
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    modifier = Modifier.height(AppConfig.Navigation.NAV_BAR_HEIGHT_DP.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppDestinations.entries.forEach { destination ->
                            val isSelected = destination == currentDestination
                            val contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { currentDestination = destination }
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(destination.icon),
                                    contentDescription = destination.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(AppConfig.Navigation.NAV_BAR_ICON_SIZE_DP.dp)
                                )
                                if (AppConfig.Navigation.SHOW_NAVIGATION_LABELS) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
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

/**
 * Sample greeting component retained for UI testing and preview exercises.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.LightGray) {
        Text(
            text = "Hello! My name is $name and this is my first Android app!",
            modifier = modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CryptotrackerTheme {
        Greeting("ThirdBvsta")
    }
}
