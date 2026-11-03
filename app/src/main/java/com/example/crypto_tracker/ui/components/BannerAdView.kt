package com.example.crypto_tracker.ui.components

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.crypto_tracker.AppDestinations
import com.example.crypto_tracker.config.AppConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

/**
 * The banner slot shared by every screen, with one banner of its own per screen ([screen]).
 *
 * Each screen's [AdView] (and its own ad unit, see [AppConfig.Ads.bannerUnitId]) is created the first time
 * the screen is shown and then kept: switching tabs shows the screen's banner again, paused while hidden,
 * instead of requesting a new ad on every switch. Standard banner size (50dp height). The slot keeps its
 * height even when no ad is filled, so content below never jumps.
 */
@Composable
fun BannerAdView(
    screen: AppDestinations,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adViews = remember(context) { HashMap<AppDestinations, AdView>() }
    val failed = remember { mutableStateMapOf<AppDestinations, Boolean>() }
    val loaded = remember { HashSet<AppDestinations>() }

    val adView = adViews.getOrPut(screen) {
        createAdView(context, AppConfig.Ads.bannerUnitId(screen)) { failed[screen] = it }
    }

    // Deferred: ad SDK work on the main thread would otherwise compete with the first frames.
    LaunchedEffect(screen) {
        if (loaded.add(screen)) {
            delay(AppConfig.Ads.BANNER_LOAD_DELAY_MS)
            adView.loadAd(AdRequest.Builder().build())
        }
    }

    // Only the visible screen's banner runs; the others are paused.
    DisposableEffect(screen) {
        adView.resume()
        onDispose { adView.pause() }
    }
    DisposableEffect(adViews) {
        onDispose { adViews.values.forEach { it.destroy() } }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppConfig.Ads.BANNER_HEIGHT_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        if (failed[screen] != true) {
            key(screen) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        // The banner may still be attached to the slot of the screen we just left.
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        adView
                    }
                )
            }
        }
    }
}

private fun createAdView(context: Context, unitId: String, onFailedChange: (Boolean) -> Unit): AdView =
    AdView(context).apply {
        setAdSize(AdSize.BANNER)
        adUnitId = unitId
        adListener = object : AdListener() {
            override fun onAdLoaded() = onFailedChange(false)

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.w("BannerAdView", "Banner failed to load: ${adError.message}")
                onFailedChange(true)
            }
        }
    }
