package com.anod.appwatcher.sync

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import com.anod.appwatcher.database.entities.Schedule
import info.anodsplace.framework.net.NetworkConnectivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

/** The "Wi-Fi only" update setting decides whether a scheduled refresh runs on the current network. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class UpdateCheckWifiOnlyTest {
    private val manualSync = Data.Builder().putBoolean(UpdateCheck.EXTRAS_MANUAL, true).build()
    private lateinit var harness: UpdateCheckHarness
    private val context get() = harness.context
    private val dfeApi get() = harness.dfeApi
    private val preferences get() = harness.preferences
    private val database get() = harness.database
    private val updateCheck get() = harness.updateCheck

    @Before
    fun setUp() {
        harness = UpdateCheckHarness(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        harness.close()
    }

    private fun connectTo(type: Int?) {
        val shadow = shadowOf(context.getSystemService(ConnectivityManager::class.java))
        shadow.clearAllNetworks()
        shadow.setActiveNetworkInfo(null)
        if (type != null) {
            val info = ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.CONNECTED, type, 0, true, NetworkInfo.State.CONNECTED)
            val network = ShadowNetwork.newInstance(type)
            val capabilities = ShadowNetworkCapabilities.newInstance()
            val transport = if (type == ConnectivityManager.TYPE_WIFI) NetworkCapabilities.TRANSPORT_WIFI else NetworkCapabilities.TRANSPORT_CELLULAR
            shadowOf(capabilities).addTransportType(transport)
            shadow.setActiveNetworkInfo(info)
            shadow.addNetwork(network, info)
            shadow.setNetworkCapabilities(network, capabilities)
        }
        assertEquals(type == ConnectivityManager.TYPE_WIFI, NetworkConnectivity(context).isWifiEnabled)
    }

    private suspend fun schedules() = database.schedules().load().first()

    @Test
    fun scheduledRefreshIsSkippedOnMobileDataWhenWifiOnlyIsOn() = runBlocking {
        preferences.isWifiOnly = true
        connectTo(ConnectivityManager.TYPE_MOBILE)

        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        val schedule = schedules().single()
        assertEquals(Schedule.STATUS_SKIPPED_NO_WIFI, schedule.result)
        assertEquals(Schedule.REASON_SCHEDULE, schedule.reason)
        assertEquals(0, dfeApi.uploadCalls)
        assertEquals(-1L, preferences.lastUpdateTime)
    }

    @Test
    fun scheduledRefreshIsSkippedWithoutANetworkWhenWifiOnlyIsOn() = runBlocking {
        preferences.isWifiOnly = true
        connectTo(null)

        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        assertEquals(Schedule.STATUS_SKIPPED_NO_WIFI, schedules().single().result)
        assertEquals(0, dfeApi.uploadCalls)
    }

    @Test
    fun scheduledRefreshRunsOnWifiWhenWifiOnlyIsOn() = runBlocking {
        preferences.isWifiOnly = true
        connectTo(ConnectivityManager.TYPE_WIFI)

        assertEquals(0, updateCheck.perform(Data.EMPTY))

        assertEquals(Schedule.STATUS_SUCCESS, schedules().single().result)
        assertTrue(preferences.lastUpdateTime > 0)
    }

    @Test
    fun scheduledRefreshRunsOnMobileDataWhenWifiOnlyIsOff() = runBlocking {
        preferences.isWifiOnly = false
        connectTo(ConnectivityManager.TYPE_MOBILE)

        assertEquals(0, updateCheck.perform(Data.EMPTY))

        assertEquals(Schedule.STATUS_SUCCESS, schedules().single().result)
        assertTrue(preferences.lastUpdateTime > 0)
    }

    @Test
    fun manualRefreshRunsOnMobileDataEvenWhenWifiOnlyIsOn() = runBlocking {
        preferences.isWifiOnly = true
        connectTo(ConnectivityManager.TYPE_MOBILE)

        assertEquals(0, updateCheck.perform(manualSync))

        val schedule = schedules().single()
        assertEquals(Schedule.STATUS_SUCCESS, schedule.result)
        assertEquals(Schedule.REASON_MANUAL, schedule.reason)
    }

    @Test
    fun skippedRefreshRunsOnceTheDeviceJoinsWifi() = runBlocking {
        preferences.isWifiOnly = true
        connectTo(ConnectivityManager.TYPE_MOBILE)
        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        connectTo(ConnectivityManager.TYPE_WIFI)
        assertEquals(0, updateCheck.perform(Data.EMPTY))

        assertEquals(listOf(Schedule.STATUS_SUCCESS, Schedule.STATUS_SKIPPED_NO_WIFI), schedules().map { it.result })
    }

    @Test
    fun turningWifiOnlyOffLetsASkippedRefreshRunOnMobileData() = runBlocking {
        connectTo(ConnectivityManager.TYPE_MOBILE)
        preferences.isWifiOnly = true
        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        preferences.isWifiOnly = false
        assertEquals(0, updateCheck.perform(Data.EMPTY))

        assertEquals(listOf(Schedule.STATUS_SUCCESS, Schedule.STATUS_SKIPPED_NO_WIFI), schedules().map { it.result })
    }
}