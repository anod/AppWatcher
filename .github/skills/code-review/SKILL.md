---
name: code-review
description: Code review guidance for the AppWatcher Android app. Use this when reviewing a pull request or code changes in this repository to focus on correctness, security, and AppWatcher's architecture and conventions.
---

# AppWatcher code review

Use this skill when reviewing pull requests or diffs in this repository. Prioritize high-signal feedback: correctness bugs, security/secret leaks, and violations of the architecture and conventions below. Do not raise style or formatting nitpicks — this project enforces a deliberate manual formatting style (see "Formatting policy").

## What to prioritize

1. Correctness and regressions in changed code.
2. Secret/private-file leaks (highest priority — see "Secrets and private files").
3. Violations of the module boundaries and conventions listed below.
4. Missing or incorrect Room migrations when the schema changes.
5. Concurrency/lifecycle issues in Compose scenes and view models.

Skip or down-rank: wrapping, blank lines, import order, and other purely stylistic matters — the repo intentionally disables these checks.

## Secrets and private files

Flag as blocking if a change commits or prints any of these:

- `local.properties`, `app/google-services.json`, build outputs, or IDE settings.
- Release signing, Play API, or Firebase values, including `APPWATCHER_GOOGLE_SERVICES_FILE`, `APPWATCHER_KEYSTORE_FILE`, `APPWATCHER_KEYSTORE_PASSWORD`, `APPWATCHER_KEY_ALIAS`, `APPWATCHER_KEY_PASSWORD`.
- Firebase project/app ids, OAuth tokens, raw crash logs, or private Crashlytics URLs pasted into source, PR descriptions, or docs.

Verify new secrets are read from Gradle user home / environment, not tracked files.

## Architecture and module boundaries

- Multi-module app: `:app` (Compose app), `:playstore` (Play Store/DFE APIs as KMP Android), and `:lib:*` shared modules (`applog`, `compose`, `context`, `framework`, `graphics`, `ktx`, `notification`, `permissions`, `playservices`, `viewmodel`). Flag changes that leak app-specific concerns into `:lib:*` or bypass `:playstore` for Play Store/network calls.
- `:playstore` intentionally keeps Kotlin sources under `src/androidMain/java`; don't "fix" the path without updating the MPP source-set wiring.
- Compose BOM versions come from `lib:compose` via an API platform dependency; consuming modules should inherit versions rather than pin their own.

## Compose scenes, screens, and view models

- View models extend `BaseFlowViewModel<State, Event, Action>` (`app/src/main/java/com/anod/appwatcher/utils/BaseFlowViewModel.kt` or `lib/viewmodel/.../BaseFlowViewModel.kt`), expose `viewStates`, and emit one-shot `viewActions`. Follow nearby imports for which base class to use.
- Scene composables collect `viewStates`, render a `*Screen`, and collect `viewActions` inside `LaunchedEffect`. Reusable `*Screen` functions should take immutable state and expose a single typed `onEvent` callback; keep dependency lookup and `viewActions` collection in the scene wrapper.
- `viewActions` collectors must use stable `LaunchedEffect` keys (the view model or `Unit`). For changing callbacks (e.g. navigation lambdas), wrap with `rememberUpdatedState` and call the current value inside the collector — do NOT key the collector by the lambda. Flag collectors keyed on unstable lambdas.
- Navigation uses AndroidX Navigation 3: `AppWatcherActivity` owns `NavDisplay`, `rememberNavBackStack`, and serializable `SceneNavKey` entries.

## Theming

- Each scene applies its own `AppTheme`. Flag any global theme wrapper added around `NavDisplay` or `AppWatcherActivity`.
- Details/dialogs may apply nested `AppTheme` for custom app/tag colors, but avoid nested themes that both update system bars. In list-detail layouts, the detail pane must not fight the list scene for status bar ownership; standalone details/dialogs may own themed bars.
- In list-detail detail panes, protect interactive/text content from `WindowInsets.displayCutout` while letting header background and app bar draw behind the cutout.
- List/detail uses the custom `rememberResizableListDetailSceneStrategy` / `ResizableListDetailSceneStrategy` around `ListDetailPaneScaffold`. Preserve the themed scaffold background and `minPaneWidth` clamp when touching split-pane gaps or resize behavior.

## Watchlist paging (high-risk area)

- Watchlist screens share `WatchListPage`, `WatchListScreen`, `WatchListPagerFactory`, `SectionHeaderFactory`, and `WatchListPagingSource` (`app/src/main/java/com/anod/appwatcher/watchlist/WatchListPagingSource.kt`). Paging data is cached in the pager factory; section headers are inserted with `PagingData.insertSeparators`.
- Paging uses a custom raw-query `PagingSource`, not Room-generated paging. Ordering must be deterministic with a stable row-id tie-breaker, and `AppListTable.Queries.changes(db.apps())` must invalidate cached pager factories when app rows mutate.
- Duplicate Lazy/Paging keys usually mean duplicate data within one paging generation. Flag any "fix" that adds volatile values to Compose keys to hide duplicates; the real fix is at the ordering/invalidation/section-key layer.
- `WatchListStateViewModel` takes `WatchListTagFilter.None`, `Untagged`, or `Tag(id)`, plus `showOnDeviceApps` and `showRecentlyInstalledApps`. Keep `Tag` for UI state/title/color and `WatchListTagFilter` for query semantics so "no tag filter" and explicit "untagged" stay distinct — flag code that conflates them.

## Data layer

- Room schema is in `app/schemas`; migrations live in `AppsDatabase` (`app/src/main/java/com/anod/appwatcher/database/AppsDatabase.kt`); app list queries live in `AppListTable.Queries`. If an entity/schema changes, verify a matching migration and updated schema JSON.
- Play Store documents are converted to entities outside Room entity constructors.

## Dependency injection

- The project uses `koin-core` only — there is no Koin Compose helper dependency. Flag added Koin-Compose usage.
- Use `KoinComponent` with `by inject()` or `app/src/main/java/com/anod/appwatcher/utils/KoinExtensions.kt` in view models/services. Avoid Koin lookups inside low-level reusable composables unless nearby code already does so.
- Pass dependencies and theme data explicitly into scene composables.

## Submodule / `lib` pointer changes

- `lib` is a git submodule. Changes to files under `lib` must be committed and pushed in the submodule repo first; the parent repo should then update the `lib` gitlink to the exact pushed SHA. Flag a parent-repo submodule pointer bump whose commit isn't shown as pushed/fetchable, and flag `lib` source edits committed only in the parent repo.

## Formatting policy (do not nitpick)

- Preserve existing Kotlin formatting. Do not request broad `ktlintFormat`/autoformat or collapsing of multiline constructor/property/data-class declarations.
- `.editorconfig` intentionally disables several ktlint wrapping rules and sets `insert_final_newline = false`. Do not suggest re-enabling them, and do not ask for a trailing newline on files.
- Declaration parameters/properties may stay on their own lines; short inheritance lists may stay inline; `&&`/`||` should not be split onto standalone lines.

## Build and test expectations

- CI runs on JDK 21 and executes `./gradlew testDebugUnitTest` (test failures are `continue-on-error`, so check reports rather than assuming green). Relevant local commands: `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`, `ktlintCheck`.
- When behavior changes, check that unit tests were added or updated where the codebase already has coverage (e.g. `WatchListPagingSourceTest`).