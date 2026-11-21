package com.anod.appwatcher.installed

import android.accounts.Account
import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.accounts.AccountAuthTokenProvider
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PackageChangedReceiver
import info.anodsplace.notification.NotificationManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** How the installed apps list picks up the pull to refresh setting when it opens. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@LooperMode(LooperMode.Mode.PAUSED)
class InstalledListViewModelSettingsTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val preferences = Preferences(context, NotificationManager.NoOp(), appScope, DiagnosticAllowlist(emptySet()))
    private val authToken = AuthTokenBlocking.create(GrantingTokenProvider())
    private val viewModelStore = ViewModelStore()

    @Before
    fun setUp() {
        preferences.account = AuthAccount("user@example.com", AuthTokenBlocking.ACCOUNT_TYPE, "4d2", "checkin", "config")
        startKoin {
            modules(module {
                single<Context> { context }
                single { preferences }
                single { authToken }
                single { PackageChangedReceiver() }
            })
        }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        stopKoin()
        appScope.cancel()
    }

    /** Waits for the account check the list starts with, so that nothing is still running when the test ends. */
    private fun openList(): InstalledListViewModel {
        val opened = InstalledListViewModel(state = SavedStateHandle(), showAction = false, sortId = Preferences.SORT_DATE_DESC)
        viewModelStore.put("installed", opened)
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (authToken.tokenState.value.isEmpty()) {
            if (System.nanoTime() > deadline) fail("Timed out waiting for the account check")
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        return opened
    }

    @Test
    fun pullToRefreshIsOnWhenTheSettingIsOn() {
        preferences.enablePullToRefresh = true

        assertTrue(openList().viewState.enablePullToRefresh)
    }

    @Test
    fun pullToRefreshIsOffWhenTheSettingIsOff() {
        preferences.enablePullToRefresh = false

        assertFalse(openList().viewState.enablePullToRefresh)
    }

    private class GrantingTokenProvider : AccountAuthTokenProvider {
        override fun getAuthToken(account: Account) = "token"

        override fun invalidateAuthToken(token: String) = Unit
    }
}