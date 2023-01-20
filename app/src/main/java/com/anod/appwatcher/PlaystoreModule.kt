package com.anod.appwatcher

import android.accounts.Account
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PlaystoreAuthTokenProvider
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import org.koin.core.module.Module
import org.koin.dsl.module

fun createPlayStoreModule(): Module = module {
    factory<DfeApi> {
       DfeApiImpl(
           http = get(),
           context = get(),
           account = get<Preferences>().account ?: Account("unknown", "unknown"),
           authTokenProvider = PlaystoreAuthTokenProvider(
               authTokenBlocking = get()
           ),
           deviceInfoProvider = get()
       )
    }
}