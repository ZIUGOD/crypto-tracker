# Crypto Tracker - Development Guide

## Run / verify
```
./gradlew :app:assembleDebug        # build
./gradlew :app:testDebugUnitTest    # unit tests (parser, formatters, ViewModels)
./gradlew :app:lintDebug            # lint (report in app/build/reports)
```
Optional Gradle property: `GOOGLE_WEB_CLIENT_ID` overrides the OAuth web client ID (see `app/build.gradle.kts`).

## Project layout (feature-first)
```
AppNavigationBar.kt  bottom glass navigation bar (sizes/spacing in AppConfig.Navigation)
config/        AppConfig - every tweakable number, flag, URL and ID
auth/          GoogleSignIn (interface) + GoogleSignInClient (Credential Manager)
data/          model/, repository/ (CryptoRepository, CachingCryptoRepository, CoinParser, CoinDescriptions), cache/ (CoinCache: last list saved offline), preferences/
notification/  PinnedCoinsStore + PriceNotifier (interfaces), their Android implementations, WorkManager worker
ui/theme/      Color.kt (tokens), Theme.kt (Material scheme + shapes), Type.kt (+ FinancialText)
ui/components/ Glass.kt (design system), ScreenHeader, UserAccountHeader, BannerAdView
ui/home/       HomeScreen (+ stateless HomeContent), HomeViewModel, CryptoRankCard, CoinFormatters
ui/settings/   SettingsScreen, SettingsViewModel, ThemeSection, AccountSection, AboutSection
ui/news/       NewsScreen (placeholder)
ui/login/      LoginScreen (currently not wired into navigation)
```

## Where to change things
| Need to change... | Go to |
|---|---|
| A number, flag, URL, ID, interval, limit | `config/AppConfig.kt` |
| Brand colors (light/dark) | `ui/theme/Color.kt` -> mapped in `ui/theme/Theme.kt` |
| Type scale / monospace figures | `ui/theme/Type.kt` (`FinancialText`) |
| Glass look (radii, blur, opacity, spacing) | `GlassDefaults` in `ui/components/Glass.kt` |
| Background texture | `GlassBackground` in `ui/components/Glass.kt` (toggle: `AppConfig.Design`) |
| Provider JSON mapping | `data/repository/CoinParser.kt` (pure, unit-tested) |
| Coin blurbs | `data/repository/CoinDescriptions.kt` |
| Pin/notification rules | `HomeViewModel` (`togglePinned`) and `PinnedCoinsStore.ensureDefaultSelection` |
| UI text (any language) | `res/values/strings.xml` (English, default + fallback) and `res/values-<lang>/strings.xml` |
| Number / currency formats | `format/CoinFormatters.kt` (take a `Locale`) |
| Ad unit IDs | `AppConfig.Ads` (+ `APPLICATION_ID` in `AndroidManifest.xml`) |

## Localization (English + Portuguese-BR)
- English is the default language **and** the fallback: `res/values/strings.xml`. The first launch is always English (it does not follow the device language); the choice is stored by `LanguagePreferences` and applied by `AppLocale.wrap(context)` in `MainActivity.attachBaseContext`. Changing it in Settings recreates the Activity.
- No UI text in Kotlin: use `stringResource(R.string.x)` (or `context.getString` for toasts/notifications). ViewModels never hold text - they emit typed state/events (`HomeError`, `SettingsEvent`) and the screen maps them to strings.
- Anything that runs without an Activity (notifications, the background worker) wraps its context with `AppLocale.wrap`, see `CryptoNotificationManager`.
- Prices and percentages are formatted with the selected locale (`US$1.234,50` in pt-BR, always in US dollars). Never format with a hard-coded `Locale.US` in UI code; use `appLocale()` in composables.
- **Add a language:** (1) add an entry to `AppLanguage` (tag + native name); (2) create `res/values-<qualifier>/strings.xml` (`es` -> `values-es`, `pt-BR` -> `values-pt-rBR`) with every key of the default file. `StringResourcesTest` fails when a key or a `%1$s` placeholder is missing. The language picker lists the new entry automatically.

## Architecture rules
- One-way data flow: `Repository/Store -> ViewModel (StateFlow) -> Composable`. Screens render state and forward events; they never touch SharedPreferences, repositories or the notification manager.
- Every screen has a stateful entry point (gets the ViewModel) and stateless content composables (take state + lambdas) - this keeps them previewable and testable.
- ViewModels depend on interfaces (`ICryptoRepository`, `PinnedCoinsStore`, `PriceNotifier`, `ThemeStore`, `SessionStore`, `GoogleSignIn`) and expose a `Factory` for production wiring. Tests construct them directly with fakes.
- Polling runs only while the app is in the foreground (`HomeViewModel.isAppActive`); background refresh belongs to WorkManager. Screens collect state with `collectAsStateWithLifecycle`.
- Every successful fetch is saved by `CachingCryptoRepository` (used by the ViewModel and the worker); `HomeViewModel` shows that list as `isStale` on start, so the app works offline. The cache and session prefs are excluded from backup.
- Notifications are only re-posted when their text changes (`PostedNotificationCache`).
- One-off messages (toasts) are `SharedFlow` events, not state.
- Never keep an Activity `Context` in a ViewModel: build `GoogleSignInClient` in the UI and pass it per call.

## Code conventions
- Use theme tokens (`MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`) and `GlassCard` / `.glass()`; no literal colors or `fontSize` in screens.
- Financial figures use `FinancialText` styles; direction (up/down) is shown with arrow + sign, not color alone.
- Keep composables small and named for what they show (see `CryptoRankCard.kt`); a file over ~250 lines is a signal to split.
- No getters with side effects (`PinnedCoinsStore.getSelectedCoinIds` is a pure read).
- Every new parser, formatter or ViewModel behavior ships with a unit test.

## Before release checklist
- [ ] Replace AdMob test IDs (`AppConfig.Ads`, manifest `APPLICATION_ID`).
- [ ] Replace `AppConfig.Legal` URLs (currently `example.com`; policy pages are in `docs/`).
- [ ] Verify the CoinCap fallback endpoint still exists (CoinCap v2 may be retired).
- [ ] Move user-facing strings to `strings.xml` (needed for translation).
- [ ] Visual pass on a real device: both themes, small screens, font scale 1.3x, Android 11 (no blur).
