package com.anod.appwatcher

import android.app.Application
import android.content.pm.ShortcutManager
import android.telephony.TelephonyManager
import android.util.LruCache
import androidx.core.content.ContextCompat.getSystemService
import coil3.ImageLoader
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.UpdateCheck
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.RealAppIconLoader
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.watchlist.RecentlyInstalledAppsLoader
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.content.PinShortcutManager
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.ktx.createLruCache
import info.anodsplace.notification.NotificationManager
import info.anodsplace.notification.RealNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun createAppModule(): Module = module {
    factory { getSystemService(get(), TelephonyManager::class.java) }
    factory {
        PinShortcutManager(
            context = get(),
            androidShortcuts = getSystemService(get(), ShortcutManager::class.java)!!
        )
    }
    factory { get<Application>().packageManager }
    singleOf(::AuthAccountInitializer)
    singleOf(::UploadDateParserCache)
    singleOf(::ApplicationContext)
    singleOf(::RealNotificationManager) {
        bind<NotificationManager>()
    }
    singleOf(::Preferences)
    singleOf(::UploadServiceContentObserver)
    single { OkHttpClient.Builder().build() }
    singleOf(::RealAppIconLoader) {
        bind<AppIconLoader>()
    }
    singleOf(::NetworkConnectivity)
    factoryOf(::RecentlyInstalledAppsLoader)
    single<LruCache<String, Any?>>(named("memoryCache")) { createLruCache() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    singleOf(::PackageChangedReceiver)
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
            authAccount = get(),
            networkConnection = get(),
            preferences = get(),
            uploadDateParserCache = get(),
            koin = getKoin()
        )
    }
}