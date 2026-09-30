package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.accounts.AccountAuthTokenProvider
import com.anod.appwatcher.accounts.AccountSelectionResult
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.DeviceRegistration
import com.anod.appwatcher.accounts.FakeDfeApi
import com.anod.appwatcher.accounts.PlaySessionCoordinator
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.search.SearchViewEvent
import com.anod.appwatcher.search.SearchViewModel
import com.anod.appwatcher.search.SearchViewState
import info.anodsplace.notification.NotificationManager
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@LooperMode(LooperMode.Mode.PAUSED)
class AccountSelectionViewModelTest {
    private val firstAccount = "first@example.com"
    private val secondAccount = "second@example.com"
    private val thirdAccount = "third@example.com"
    private val tokenProvider = BlockingTokenProvider(firstAccount)
    private val dfeApi = FakeDfeApi()
    private val appScope = CoroutineScope(Dispatchers.Unconfined)
    private val viewModelStore = ViewModelStore()
    private lateinit var preferences: Preferences
    private lateinit var database: AppsDatabase
    private lateinit var viewModel: MainViewModel
    private var originalCrashReports = false
    private var originalUpdateFrequency = 0
    private var originalVersionCode = 0

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preferences = Preferences(context, NotificationManager.NoOp(areNotificationsEnabled = true), appScope)
        originalCrashReports = preferences.collectCrashReports
        originalUpdateFrequency = preferences.updatesFrequency
        originalVersionCode = preferences.versionCode
        preferences.collectCrashReports = false
        preferences.updatesFrequency = 0
        runBlocking {
            assertTrue(
                preferences.saveAccount(
                    AuthAccount(firstAccount, AuthTokenBlocking.ACCOUNT_TYPE, "4d2", "checkin", "config"),
                    deviceRegistrationPending = false,
                    deviceRegistrationAuthorized = false,
                    deviceConfigRevision = DeviceRegistration.DEVICE_CONFIG_REVISION
                )
            )
        }
        database = Room.inMemoryDatabaseBuilder(context, AppsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val authToken = AuthTokenBlocking.create(tokenProvider)
        val initializer = AuthAccountInitializer(preferences, authToken, dfeApi, PlaySessionCoordinator())
        startKoin {
            modules(module {
                single<Context> { context }
                single { preferences }
                single { database }
                single { authToken }
                single { initializer }
            })
        }
        viewModel = MainViewModel()
        viewModelStore.put("main", viewModel)
    }

    @After
    fun tearDown() {
        tokenProvider.releaseFirst.countDown()
        viewModelStore.clear()
        database.close()
        preferences.account = null
        preferences.collectCrashReports = originalCrashReports
        preferences.updatesFrequency = originalUpdateFrequency
        preferences.versionCode = originalVersionCode
        stopKoin()
        appScope.cancel()
    }

    @Test
    fun choosingAnotherAccountWhileResumingOldOneSwitchesInBothDirections() = runBlocking {
        preferences.versionCode = 0
        viewModel.handleEvent(MainViewEvent.OnResume)
        awaitFirstToken()

        selectAccount(secondAccount)
        tokenProvider.releaseFirst.countDown()

        awaitAccount(secondAccount)
        assertEquals(secondAccount, preferences.account?.name)
        awaitUpgradeCheck()

        selectAccount(firstAccount)
        awaitAccount(firstAccount)
        assertEquals(firstAccount, preferences.account?.name)
        assertEquals(listOf(firstAccount, secondAccount, firstAccount), tokenProvider.requestedAccounts)
    }

    @Test
    fun latestExplicitAccountSelectionIsNotDropped() = runBlocking {
        selectAccount(firstAccount)
        awaitFirstToken()

        selectAccount(secondAccount)
        selectAccount(thirdAccount)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        tokenProvider.releaseFirst.countDown()

        awaitAccount(thirdAccount)
        assertEquals(thirdAccount, preferences.account?.name)
        assertEquals(listOf(firstAccount, thirdAccount), tokenProvider.requestedAccounts)
    }

    @Test
    fun autoSyncSchedulingFinishesBeforeSwitchingAccounts() = runBlocking {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        preferences.updatesFrequency = 3600
        preferences.versionCode = 0

        viewModel.handleEvent(MainViewEvent.OnResume)
        awaitFirstToken()
        tokenProvider.releaseFirst.countDown()

        awaitUpgradeCheck()
        selectAccount(secondAccount)
        awaitAccount(secondAccount)
        assertEquals(secondAccount, preferences.account?.name)
    }

    @Test
    fun searchAccountPickerResultOverridesInFlightAuthentication() = runBlocking {
        val searchViewModel = SearchViewModel(SearchViewState())
        viewModelStore.put("search", searchViewModel)
        searchViewModel.handleEvent(
            SearchViewEvent.SetAccount(
                AccountSelectionResult.Success(Account(firstAccount, AuthTokenBlocking.ACCOUNT_TYPE))
            )
        )
        awaitFirstToken()

        searchViewModel.handleEvent(
            SearchViewEvent.SetAccount(
                AccountSelectionResult.Success(Account(secondAccount, AuthTokenBlocking.ACCOUNT_TYPE))
            )
        )
        searchViewModel.handleEvent(
            SearchViewEvent.SetAccount(
                AccountSelectionResult.Success(Account(thirdAccount, AuthTokenBlocking.ACCOUNT_TYPE))
            )
        )
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        tokenProvider.releaseFirst.countDown()

        withTimeout(5_000) {
            while (!searchViewModel.viewState.authenticated || preferences.account?.name != thirdAccount) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
        }
        assertEquals(listOf(firstAccount, thirdAccount), tokenProvider.requestedAccounts)
    }

    @Test
    fun searchPickerSwitchesAccountWhileMainRegistrationIsRunning() = runBlocking {
        preferences.account = AuthAccount(firstAccount, AuthTokenBlocking.ACCOUNT_TYPE, "", "", "")
        selectAccount(firstAccount)
        awaitFirstToken()

        assertTrue(preferences.isDeviceRegistrationRequired)
        val searchViewModel = SearchViewModel(SearchViewState())
        viewModelStore.put("search", searchViewModel)
        searchViewModel.handleEvent(
            SearchViewEvent.SetAccount(
                AccountSelectionResult.Success(Account(secondAccount, AuthTokenBlocking.ACCOUNT_TYPE))
            )
        )
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        tokenProvider.releaseFirst.countDown()

        withTimeout(10_000) {
            while (!searchViewModel.viewState.authenticated || preferences.account?.name != secondAccount) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
        }
        assertEquals(listOf(firstAccount, secondAccount), tokenProvider.requestedAccounts)
    }

    @Test
    fun searchSelectionWinsOverQueuedMainSelection() = runBlocking {
        preferences.account = AuthAccount(firstAccount, AuthTokenBlocking.ACCOUNT_TYPE, "", "", "")
        selectAccount(firstAccount)
        awaitFirstToken()

        selectAccount(secondAccount)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(preferences.isDeviceRegistrationRequired)
        val searchViewModel = SearchViewModel(SearchViewState())
        viewModelStore.put("search", searchViewModel)
        searchViewModel.handleEvent(
            SearchViewEvent.SetAccount(
                AccountSelectionResult.Success(Account(thirdAccount, AuthTokenBlocking.ACCOUNT_TYPE))
            )
        )
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        tokenProvider.releaseFirst.countDown()

        withTimeout(10_000) {
            while (!searchViewModel.viewState.authenticated || preferences.account?.name != thirdAccount) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
        }
        assertEquals(listOf(firstAccount, thirdAccount), tokenProvider.requestedAccounts)
    }

    @Test
    fun mainShowsAccountSelectedFromSearchPickerOnReturn() = runBlocking {
        preferences.account = AuthAccount(firstAccount, AuthTokenBlocking.ACCOUNT_TYPE, "", "", "")
        preferences.versionCode = 0
        selectAccount(firstAccount)
        awaitFirstToken()

        assertTrue(preferences.isDeviceRegistrationRequired)
        val searchViewModel = SearchViewModel(SearchViewState())
        viewModelStore.put("search", searchViewModel)
        tokenProvider.releaseFirst.countDown()
        awaitUpgradeCheck()

        searchViewModel.handleEvent(
            SearchViewEvent.SetAccount(
                AccountSelectionResult.Success(Account(secondAccount, AuthTokenBlocking.ACCOUNT_TYPE))
            )
        )
        withTimeout(10_000) {
            while (!searchViewModel.viewState.authenticated || preferences.account?.name != secondAccount) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
        }

        viewModel.handleEvent(MainViewEvent.OnResume)
        assertEquals(secondAccount, viewModel.viewState.account?.name)
    }

    private fun selectAccount(name: String) {
        viewModel.handleEvent(
            MainViewEvent.SetAccount(
                AccountSelectionResult.Success(Account(name, AuthTokenBlocking.ACCOUNT_TYPE))
            )
        )
    }

    private suspend fun awaitAccount(name: String) {
        val observed = withTimeoutOrNull(10_000) {
            while (viewModel.viewState.account?.name != name) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
            true
        }
        assertTrue(
            "Expected $name; shown=${viewModel.viewState.account?.name}, saved=${preferences.account?.name}, " +
                "configReady=${!preferences.account?.deviceConfig.isNullOrEmpty()}, uploads=${dfeApi.uploadCalls}, " +
                "requests=${tokenProvider.requestedAccounts}",
            observed == true
        )
    }

    private suspend fun awaitFirstToken() {
        withTimeout(5_000) {
            while (!tokenProvider.firstRequested.await(0, TimeUnit.MILLISECONDS)) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
        }
    }

    private suspend fun awaitUpgradeCheck() {
        withTimeout(10_000) {
            while (preferences.versionCode != BuildConfig.VERSION_CODE) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                delay(10)
            }
        }
    }

    private class BlockingTokenProvider(private val blockedAccount: String) : AccountAuthTokenProvider {
        val firstRequested = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val requestedAccounts = CopyOnWriteArrayList<String>()
        private var blocked = false

        override fun getAuthToken(account: Account): String {
            requestedAccounts.add(account.name)
            if (account.name == blockedAccount && !blocked) {
                blocked = true
                firstRequested.countDown()
                check(releaseFirst.await(5, TimeUnit.SECONDS))
            }
            return "token-${account.name}"
        }

        override fun invalidateAuthToken(token: String) = Unit
    }
}