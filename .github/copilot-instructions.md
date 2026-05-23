# AppWatcher Copilot instructions

## Build, test, and lint

- Setup: `git submodule update --init --recursive`, `local.properties` with `sdk.dir=...`, and `app/google-services.json` from `google-services.json.debug`.
- Commands:
  - Build debug APK: `./gradlew :app:assembleDebug`
  - Install debug APK: `./gradlew :app:installDebug`
  - Run app unit tests: `./gradlew :app:testDebugUnitTest`
  - Run one test class/method: `./gradlew :app:testDebugUnitTest --tests "com.anod.appwatcher.watchlist.WatchListPagingSourceTest"`
  - Android lint: `./gradlew :app:lintDebug`
  - ktlint checks: `./gradlew ktlintCheck`
- On Windows, use `.\gradlew.bat` instead of `./gradlew`.
- CI runs JDK 21, writes `app/google-services.json` from secrets, initializes submodules, and runs `./gradlew testDebugUnitTest`; test failures are `continue-on-error`, so inspect uploaded reports.

## Architecture

- Multi-module Android app: `:app` is the Compose app, `:playstore` wraps Play Store/DFE APIs as KMP Android, and shared code is under `:lib:*` (`applog`, `compose`, `context`, `framework`, `graphics`, `ktx`, `notification`, `permissions`, `playservices`, `viewmodel`).
- `AppWatcherApplication` starts Koin and wires Room `AppsDatabase`, `Preferences`, network/connectivity, notifications, icon loading, Play Store API, backup, installed-app support, and app-wide coroutine scope.
- Navigation uses AndroidX Navigation 3: `AppWatcherActivity` owns `NavDisplay`, `rememberNavBackStack`, and serializable `SceneNavKey` entries.
- Each scene is responsible for applying its own `AppTheme`; do not add a global app theme wrapper around `NavDisplay` or `AppWatcherActivity`. Details and dialogs may apply nested `AppTheme` for custom app/tag colors, but avoid nested themes that both update system bars.
- View models generally extend `BaseFlowViewModel<State, Event, Action>`, expose `viewStates`, and emit one-shot `viewActions`. There are app-local and `lib:viewmodel` implementations; follow nearby imports.
- Scene composables collect `viewStates`, render a `*Screen`, and collect `viewActions` in `LaunchedEffect` for navigation/toasts/activity starts.
- Watchlist-style screens share `WatchListPage`, `WatchListScreen`, `WatchListPagerFactory`, `SectionHeaderFactory`, and `WatchListPagingSource`. Paging data is cached in the pager factory and section headers are inserted with `PagingData.insertSeparators`.
- `WatchListStateViewModel.listPagerFactory()` derives `WatchListPagingSource.Config` from the current filter/tag/preferences. Main watchlist and tag watchlist can both use `Tag.empty`, so be careful to distinguish “no tag filter” from an explicit untagged view when changing list behavior.
- Room schema is in `app/schemas`; migrations are in `AppsDatabase`; app list queries live in `AppListTable.Queries`. Play Store documents are converted to entities outside Room entity constructors.
- Play Store/network calls go through `:playstore` and DFE APIs in feature view models/paging sources. Sync/account/backup are split across `sync`, `accounts`, and `backup`.

## Repository-specific conventions

- Pass dependencies/theme data explicitly into scene composables. The project only uses `koin-core`; there is no Koin Compose helper dependency.
- Use `KoinComponent` with `by inject()` or `utils/KoinExtensions.kt` in view models/services. Avoid Koin lookups inside low-level reusable composables unless nearby code already does so.
- Top-level Compose screen functions should generally take immutable screen state as input and expose a single typed `onEvent` callback for output. Keep dependency lookup and `viewActions` collection in the scene wrapper rather than the reusable screen where practical.
- Preserve existing Kotlin formatting. Do not run broad `ktlintFormat`/autoformat over source files unless explicitly requested; keep existing multiline constructor/property declarations and avoid collapsing multiline data classes just to satisfy formatting tools.
- Keep `AppTheme` ownership at scene/window boundaries. Details in list-detail layouts should not fight the list scene for status bar ownership; standalone details/dialogs may own themed bars.
- The `playstore` module intentionally has Kotlin sources under `src/androidMain/java`; its Gradle config explicitly adds that directory. Do not “clean up” the source path without updating the MPP source-set wiring.
- `:playstore:testAndroidHostTest` may discover zero tests; its Gradle build disables failure for no discovered tests.
- Compose BOM versions are exposed from `lib:compose` with an API platform dependency so consuming modules inherit Compose artifact versions.
- Avoid committing generated/local files such as `local.properties`, `app/google-services.json`, build outputs, or IDE settings.
