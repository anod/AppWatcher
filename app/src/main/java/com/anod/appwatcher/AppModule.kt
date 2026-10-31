package com.anod.appwatcher

import android.app.Application
import android.content.Context
import android.content.pm.ShortcutManager
import android.telephony.TelephonyManager
import android.text.format.DateUtils
import android.util.LruCache
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.DeviceRegistrationNotification
import com.anod.appwatcher.accounts.PlaySessionCoordinator
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticRecorder
import com.anod.appwatcher.diagnostics.WorkInfoSource
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.UpdateCheck
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.PackageStateCache
import com.anod.appwatcher.utils.RealAppIconLoader
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.watchlist.RecentlyInstalledAppsLoader
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.content.PinShortcutManager
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.ktx.createLruCache
import info.anodsplace.notification.NotificationManager
import info.anodsplace.notification.RealNotificationManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
    singleOf(::PlaySessionCoordinator)
    single { DiagnosticAllowlist.parse(BuildConfig.DIAGNOSTIC_ACCOUNT_HASHES) }
    single {
        WorkInfoSource { name ->
            WorkManager.getInstance(get<Context>()).getWorkInfosForUniqueWorkFlow(name).first()
        }
    }
    singleOf(::DiagnosticRecorder)
    singleOf(::DeviceRegistrationNotification)
    singleOf(::AuthAccountInitializer)
    singleOf(::UploadDateParserCache)
    singleOf(::ApplicationContext)
    singleOf(::RealNotificationManager) {
        bind<NotificationManager>()
    }
    singleOf(::Preferences)
    singleOf(::UploadServiceContentObserver)
    // Bounds a whole call so a stalled connection cannot keep a background sync waiting indefinitely.
    single { OkHttpClient.Builder().callTimeout(2, TimeUnit.MINUTES).build() }
    singleOf(::RealAppIconLoader) {
        bind<AppIconLoader>()
    }
    singleOf(::NetworkConnectivity)
    factoryOf(::RecentlyInstalledAppsLoader)
    single<LruCache<String, Any?>>(named("memoryCache")) { createLruCache() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    singleOf(::PackageChangedReceiver)
    singleOf(::PackageStateCache)
    singleOf(::AuthTokenBlocking)
    single {
        HttpClient(OkHttp) {
            engine {
                preconfigured = get<OkHttpClient>()
            }
        }
    }
    single {
        ImageLoader.Builder(get())
            .components {
                add(
                    KtorNetworkFetcherFactory(httpClient = get<HttpClient>())
                )
                add(RealAppIconLoader.PackageIconFetcher.Factory(get()))
            }
            .build()
    }
    factory {
        UpdateCheck(
            context = get(),
            packageStates = get(),
            notificationManager = get(),
            database = get(),
            authAccount = get(),
            networkConnection = get(),
            preferences = get(),
            playSessionCoordinator = get(),
            uploadDateParserCache = get(),
            deviceRegistrationNotification = get(),
            koin = getKoin(),
            timeouts = UpdateCheck.SyncTimeouts(syncMillis = 6 * DateUtils.MINUTE_IN_MILLIS)
        )
    }
}