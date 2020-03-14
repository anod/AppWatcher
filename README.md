App Watcher for Android
=======================

![Android CI](https://github.com/anod/AppWatcher/workflows/Android%20CI/badge.svg)

App Watcher notifies you about updates of applications from Play Store even the app is not installed currently on your device and provides quick access to the "What's new" section.

With App Watcher you can follow changes of apps you love, know about new features, bug fixes and new levels update for games.

App Watcher checks daily the list for updates in Play Store and will notify you when there is a new version available.

Features:

 * Add an app from Play Market to watch list (Use Share)
 * Manual and automatic updates
 * Quick shortcut to see what's new/changelog in app 
 * Share application
 * Update notification
 * Google Drive backup
 * Filter list by: Installed/Not installed
 * Import from PLay Store watchlist

[Google Play Store][1]

## Implementation highlights

* SQLite database - store the data
* Room - access to the data
* AccountManager - access to 3rd party account on device
* Android's backup service
* SyncAdapter & AccountAuthenticator - create android account for synchronization
* Fragments
* Coroutines
* GoogleDrive integration
* Protocol Buffers

## Author

Alex Gavrishev, 2012
 
## License

    Project is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.
	http://creativecommons.org/licenses/by-sa/3.0/
 

 [1]: https://play.google.com/store/apps/details?id=com.anod.appwatcher
