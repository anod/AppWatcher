package com.anod.appwatcher

import android.app.Application
import android.app.NotificationManager
import android.telephony.TelephonyManager
import android.util.LruCache
import androidx.core.content.ContextCompat.getSystemService
import coil.ImageLoader
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.UpdateCheck
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.RealAppIconLoader
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.watchlist.RecentlyInstalledPackagesLoader
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.framework.util.createLruCache
import info.anodsplace.playstore.DeviceId
import info.anodsplace.playstore.DeviceInfoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun createAppModule(): Module = module {
    single(named("deviceId")) { DeviceId(get()).load() }
    factory { getSystemService(get(), TelephonyManager::class.java) }
    factory { getSystemService(get(), NotificationManager::class.java) }
    factory { get<Application>().packageManager }

    factory<DeviceInfoProvider> {
        object : DeviceInfoProvider {
            override val deviceId: String = get(named("deviceId"))
            override val simOperator: String = getOrNull<TelephonyManager>()?.simOperator ?: ""
        }
    }

    singleOf(::UploadDateParserCache)
    singleOf(::ApplicationContext)
    singleOf(::Preferences)
    singleOf(::UploadServiceContentObserver)
    single { OkHttpClient.Builder().build() }
    singleOf(::RealAppIconLoader) {
        bind<AppIconLoader>()
    }
    singleOf(::NetworkConnectivity)
    singleOf(::RecentlyInstalledPackagesLoader)
    single<LruCache<String, Any?>>(named("memoryCache")) { createLruCache() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    singleOf(::PackageChangedReceiver)
    factory { get<Preferences>().account }
    singleOf(::AuthTokenBlocking)
    single {
        ImageLoader.Builder(get())
                .components {
                    add(RealAppIconLoader.PackageIconFetcher.Factory(get()))
                }
                .build()
    }
    factory {
        UpdateCheck(
                context = get(),
                packageManager = get(),
                notificationManager = get(),
                database = get(),
                authToken = get(),
                networkConnection = get(),
                preferences = get(),
                uploadDateParserCache = get(),
                koin = getKoin()
        )
    }

}