package com.anod.appwatcher.utils

import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.net.NetworkConnectivity
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

val KoinComponent.prefs: Preferences
    get() = get()

val KoinComponent.appsDatabase: AppsDatabase
    get() = get()

val KoinComponent.networkConnection: NetworkConnectivity
    get() = get()

val KoinComponent.appScope: CoroutineScope
    get() = get()