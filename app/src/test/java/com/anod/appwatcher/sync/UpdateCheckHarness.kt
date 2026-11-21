package com.anod.appwatcher.sync

import android.accounts.Account
import android.content.Context
import androidx.room.Room
import com.anod.appwatcher.accounts.AccountAuthTokenProvider
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.DeviceRegistrationNotification
import com.anod.appwatcher.accounts.FakeDfeApi
import com.anod.appwatcher.accounts.PlaySessionCoordinator
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.preferences.TestNotificationManager
import com.anod.appwatcher.utils.PackageStateCache
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.DfeApi
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.net.NetworkConnectivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * An [UpdateCheck] for a signed-in account that is not restricted to Wi-Fi, backed by an in-memory
 * database, a fake Play Store and a recording notification manager.
 */
internal class UpdateCheckHarness(val context: Context) {
    val dfeApi = FakeDfeApi()
    val notifications = TestNotificationManager()
    val database: AppsDatabase = Room.inMemoryDatabaseBuilder(context, AppsDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val appScope = CoroutineScope(Dispatchers.Unconfined)
    private val koin = koinApplication {
        modules(module { single<DfeApi> { dfeApi } })
    }
    val preferences = Preferences(context, notifications, appScope, DiagnosticAllowlist(emptySet()))
    val updateCheck: UpdateCheck

    init {
        preferences.account = AuthAccount("account@example.com", AuthTokenBlocking.ACCOUNT_TYPE, "existing-id", "", "")
        preferences.isWifiOnly = false
        preferences.isDriveSyncEnabled = false
        preferences.lastUpdateTime = -1L
        val deviceRegistrationNotification = DeviceRegistrationNotification(ApplicationContext(context), notifications)
        val coordinator = PlaySessionCoordinator()
        val tokenProvider = object : AccountAuthTokenProvider {
            override fun getAuthToken(account: Account) = "token"
            override fun invalidateAuthToken(token: String) = Unit
        }
        updateCheck = UpdateCheck(
            ApplicationContext(context),
            PackageStateCache(context.packageManager),
            notifications,
            database,
            preferences,
            NetworkConnectivity(context),
            AuthAccountInitializer(
                preferences,
                AuthTokenBlocking.create(tokenProvider),
                dfeApi,
                coordinator,
                deviceRegistrationNotification
            ),
            UploadDateParserCache(),
            coordinator,
            deviceRegistrationNotification,
            koin.koin
        )
    }

    fun close() {
        preferences.account = null
        database.close()
        appScope.cancel()
        koin.close()
    }
}