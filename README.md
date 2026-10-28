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
`gradle.properties` (`GRADLE_USER_HOME`, otherwise `~/.gradle`). Values are normalized and
SHA-256 hashed before entering BuildConfig; generated constants contain digests, not addresses.
This allowlist is a rollout mechanism, not a security boundary. An absent/empty property disables
the allowlist. Consent persists on this device and is bound to
the selected account. Recording is off by default. When disabled or not allowed, no logging tee
is installed.

When enabled, app log lines are also saved under `noBackupFilesDir/diagnostics`. Logs may contain
package names and error messages. Startup logs include device power/background restrictions,
recent process exit reasons and sync work state. Recording is bounded and non-blocking; lines
may be truncated or dropped under load. Files rotate with a 30-day retention and a 10 MiB cap,
evicting the oldest segments first. Nothing can be logged while Android does not run the app.

Logs stay on the device unless you manually Share them. Share creates a snapshot under the narrow
`diagnostic-exports` cache FileProvider path and grants temporary read permission through Android's
share sheet. Nothing uploads automatically. Up to three snapshots remain cached (at most 10 MiB
beyond the retained-log cap); cached exports expire after 24 hours on the next diagnostic operation.
Delete removes retained logs and snapshots; recording can create new lines afterward if still
enabled. Recipients control their exported copies. Release bundles must be rebuilt to include this feature.

## Author

Alex Gavrishev, 2012

## License

```license
Project is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.
http://creativecommons.org/licenses/by-sa/3.0/
```

 [1]: https://play.google.com/store/apps/details?id=com.anod.appwatcher