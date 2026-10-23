package com.example.crypto_tracker.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records the app's critical user journeys so ART can precompile them at install time:
 * cold start, scrolling the coin list, expanding a card and visiting every tab.
 *
 * Regenerate after big UI changes: `./gradlew :app:generateBaselineProfile` (device/emulator connected).
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = true
    ) {
        // The first-launch notification prompt would cover the list, so grant it up front.
        device.executeShellCommand("pm grant $TARGET_PACKAGE android.permission.POST_NOTIFICATIONS")
        pressHome()
        startActivityAndWait()

        // Coin list: the first launch may need the network, so wait for the first card ("Expand details").
        device.wait(Until.hasObject(By.descContains("Expand details")), 15_000)
        scrollList(device)

        device.findObject(By.descContains("Expand details"))?.let {
            it.click()
            device.waitForIdle()
            device.findObject(By.descContains("Collapse details"))?.click()
            device.waitForIdle()
        }

        for (tab in listOf("News", "Settings", "Home")) {
            device.findObject(By.desc(tab))?.click()
            device.waitForIdle()
        }
    }

    private fun scrollList(device: UiDevice) {
        val w = device.displayWidth
        val h = device.displayHeight
        repeat(3) {
            device.swipe(w / 2, h * 3 / 4, w / 2, h / 4, 20)
            device.waitForIdle()
        }
        repeat(3) {
            device.swipe(w / 2, h / 4, w / 2, h * 3 / 4, 20)
            device.waitForIdle()
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.cryptotracker.app"
    }
}
