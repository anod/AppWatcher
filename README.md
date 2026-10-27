App Watcher for Android
=======================

![Android CI](https://github.com/anod/AppWatcher/workflows/Android%20CI/badge.svg)

App Watcher notifies you about updates of applications from Play Store even the app is not installed currently on your device and provides quick access to the "What's new" section.

With App Watcher you can follow changes of apps you love, know about new features, bug fixes and new levels update for games.

App Watcher checks daily the list for updates in Play Store and will notify you when there is a new version available.

## Features

- Add an app from Play Market to watch list (Use Share)
- Manual and automatic updates
- Quick shortcut to see what's new/changelog in app
- Share application
- Update notification
- Google Drive backup
- Filter list by: Installed/Not installed
- Import from PLay Store watchlist

[Google Play Store][1]

## Build

1. Checkout

    ```shell
    git clone
    git submodule init
    git submodule update
    ```

2. Configure

    - Define a valid SDK location
      1. Create local.properties file
      2. Set sdk.dir path (Example: `sdk.dir=~/Library/Android/sdk`)
    - For debug builds, move and rename `google-services.json.debug` into `app/google-services.json`
    - For release builds, keep private files out of git. Gradle reads the release Google Services file directly from `APPWATCHER_GOOGLE_SERVICES_FILE` in the user's Gradle user home `gradle.properties`, alongside signing values `APPWATCHER_KEYSTORE_FILE`, `APPWATCHER_KEYSTORE_PASSWORD`, `APPWATCHER_KEY_ALIAS`, and `APPWATCHER_KEY_PASSWORD`.

3. Build

    ```shell
    ./gradlew installDebug
    ```

Dependency updates prefer stable releases. Material3 remains on 1.5.0-beta01 to match Compose
Foundation 1.12.x; Material3 1.4.0 targets an older Foundation line. Google Play services Auth
remains on 21.6.0 because 22.0.0 removes the Google Sign-In APIs used by Drive backup. Moving
that dependency forward requires migrating the sign-in flow to Credential Manager.

## Local sync diagnostics

Settings offers local recording only for the selected diagnostic account. Configure the private,
comma-separated `APPWATCHER_DIAGNOSTIC_ACCOUNTS` property in the Gradle user home
`gradle.properties` (`GRADLE_USER_HOME`, otherwise `~/.gradle`). An absent/empty property
disables the allowlist. Values are trimmed, lowercased with `Locale.ROOT`, and SHA-256 hashed
before entering BuildConfig; generated constants contain only hexadecimal digests, not addresses.
This gate is a feature rollout mechanism, **not a security boundary**.

Recording defaults off, persists per selected account on this device, and is independent of
Crashlytics. Consent is stored as the selected account hash in device preferences. Switching to a different
account disables recording and discards its queued records; old files remain
shareable/deletable without enabling recording. Files live in `noBackupFilesDir` and never
enter cloud/device backups. Structured records accept only fixed event/stage/decision enums,
UUID correlation IDs, UTC times, counts and platform state codes/booleans: no account identifiers,
credentials, package names, payloads, exception messages or arbitrary AppLog text.

The serialized IO writer has a 128-event queue. Settings shows dropped events and storage failures;
the next successful write includes cumulative health counters. Segments rotate at 256 KiB and
oldest segments are deleted to enforce 10 MiB and up to 30 days, using the same retention constant
as refresh-history cleanup. Segment-level eviction can remove newer events early. Retention is
enforced on process start, writes, status refresh and export; Android must run the app to clean files.
Startup records WorkManager unique-work state, battery-optimization/background restrictions,
retained worker starts without terminal events, and available historical process-exit metadata
(never traces). Missing end events are evidence, not proof of a kill: dropping, deletion or eviction
can also remove events. No app can log while Android does not run its process.

Share creates a bounded file snapshot under the narrow `diagnostic-exports` cache FileProvider
path and grants temporary read permission through Android's share sheet. Nothing uploads
automatically, and no giant text intent is used. Up to three snapshots remain cached for recipients
(at most 10 MiB total beyond the retained-log cap); cached exports expire after 24 hours when the app
next processes a diagnostic operation. Exported copies are separate from retained recording files.
Delete removes retained records and snapshots (recording may subsequently create new events if
still enabled). Recipients control their own exported copies. A previously built release bundle
does not contain this feature and must be rebuilt separately.

Record field meanings (all events also have `time`, `process`, and `event`; `run` is a
WorkManager UUID when available):

- `SCHEDULE`: decision distinguishes manual/periodic keep/update; `count` is interval seconds,
  `flag` requires charging, `secondaryFlag` requires unmetered networking.
- `WORK_START` / terminal worker events: `attempt` is WorkManager's attempt count, `code`
  is its numeric stop reason on terminal events, and terminal `count` is updates or `-1`.
  A start's `flag` means manual work.
- `WORK_SNAPSHOT`: `code` is WorkInfo state ordinal (enqueued/running/succeeded/failed/blocked/
  cancelled), `flag` distinguishes manual unique work, and `attempt` is its current retry count.
- `STAGE` / `LOCK_*`: fixed stage names and the worker UUID correlate waiting and progress.
  `SYNC_RESULT` uses `count` for checked apps, `secondaryCount` for updates, `code` for
  unavailable apps on success or the Schedule failure status when decision is `FAILED`.
- `NOTIFICATION`: `decision` describes filtering/cancel/post-request, `count` is filtered apps,
  `secondaryCount` is pre-filter apps; `flag` is app notifications enabled and `secondaryFlag`
  is the channel enabled (absent means unknown). Authentication events have notification ID in
  `code`. A post request is not proof of OS delivery.
- `PROCESS_START`: `count` is unfinished retained workers, `secondaryCount` is app version code,
  `code` is SDK, `flag` means battery
  optimization exemption, `secondaryFlag` means background restricted. `POWER_STATE` flags
  mean battery saver and device idle. `PROCESS_EXIT` has reason in `code`, importance in
  `count`, original timestamp in `historicalTime`; the process UUID is the observing process.
- `FAILURE`: only a sanitized transient/unexpected decision and Schedule status; interruption
  events also have duration milliseconds in `count`. `RECOVERY` counts unfinished database
  runs and rows marked interrupted, with oldest start in `historicalTime`. `RECORDER_HEALTH`
  counts dropped events and failed storage operations in `count` / `secondaryCount`.
- `SNAPSHOT_UNAVAILABLE`: `code` identifies unavailable metadata: 1 restrictions, 2 power state,
  3 process exits, 4 periodic unique work, 5 manual unique work. Reads time out rather than
  blocking the recorder indefinitely; no exception text is captured.

## Author

Alex Gavrishev, 2012

## License

```license
Project is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.
http://creativecommons.org/licenses/by-sa/3.0/
```

 [1]: https://play.google.com/store/apps/details?id=com.anod.appwatcher