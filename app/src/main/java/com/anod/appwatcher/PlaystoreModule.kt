package com.anod.appwatcher

import com.anod.appwatcher.utils.DeviceInfoProvider
import com.anod.appwatcher.utils.PlaystoreAuthTokenProvider
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import finsky.api.DfeDeviceInfoProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun createPlayStoreModule(): Module = module {
    singleOf(::DeviceInfoProvider) { bind<DfeDeviceInfoProvider>() }
    factory<DfeApi> {
        DfeApiImpl(
            http = get(),
            context = get(),
            authTokenProvider = PlaystoreAuthTokenProvider(
                authTokenBlocking = get(),
                preferences = get()
            ),
            deviceInfoProvider = get()
        )
    }
}