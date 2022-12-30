package com.anod.appwatcher.utils

import android.accounts.Account
import android.util.LruCache
import coil.ImageLoader
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.net.NetworkConnectivity
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

val KoinComponent.prefs: Preferences
    get() = get()

val KoinComponent.appsDatabase: AppsDatabase
    get() = get()

val KoinComponent.networkConnection: NetworkConnectivity
    get() = get()

val KoinComponent.appScope: CoroutineScope
    get() = get()

val KoinComponent.account: Account?
    get() = getKoin().getOrNull()