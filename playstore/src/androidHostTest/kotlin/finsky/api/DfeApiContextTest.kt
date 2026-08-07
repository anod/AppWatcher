package finsky.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DfeApiContextTest {

    @Test
    fun explicitIdentityReplacesPersistedDeviceHeaders() {
        val apiContext = DfeApiContext(
            context = ApplicationProvider.getApplicationContext(),
            authTokenProvider = FakeAuthProvider(),
            deviceInfo = FakeDeviceInfoProvider()
        )

        val headers = apiContext.createDefaultHeaders(
            DfeDeviceIdentity(
                deviceId = "new-device",
                deviceCheckinConsistencyToken = "new-checkin"
            )
        )

        assertEquals("new-device", headers["X-DFE-Device-Id"])
        assertEquals("new-checkin", headers["X-DFE-Device-Checkin-Consistency-Token"])
        assertFalse(headers.containsKey("X-DFE-Device-Config-Token"))
    }

    @Test
    fun firstCheckinOmitsPersistedDeviceId() {
        val apiContext = DfeApiContext(
            context = ApplicationProvider.getApplicationContext(),
            authTokenProvider = FakeAuthProvider(),
            deviceInfo = FakeDeviceInfoProvider()
        )

        assertFalse(apiContext.createAuthHeaders(includeDeviceId = false).containsKey("device"))
    }

    private class FakeAuthProvider : DfeAuthProvider {
        override val gfsId = "old-device"
        override val gfsToken = "old-checkin"
        override val authToken = "auth-token"
        override val accountName = "account@example.com"
        override val deviceConfigToken = "old-config"
    }

    private class FakeDeviceInfoProvider : DfeDeviceInfoProvider {
        override val deviceId = "physical-device"
        override val simOperator = ""
        override val cellOperator = ""
        override val roaming = "mobile-notroaming"
        override val build: DfeDeviceBuild = FakeDeviceBuild()
        override val client = "android-google"
        override val gsfVersion = 1L
        override val otaInstalled = false
        override val locale = DfeLocale("en", "US", "en_US")
        override val timeZone = "UTC"
        override val configuration: DfeDeviceConfiguration = FakeDeviceConfiguration()
        override val playVersionCode = 1L
        override val playVersionName = "1"
    }

    private class FakeDeviceBuild : DfeDeviceBuild {
        override val id = "build-id"
        override val fingerprint = "fingerprint"
        override val hardware = "hardware"
        override val brand = "brand"
        override val radio = "radio"
        override val bootloader = "bootloader"
        override val device = "device"
        override val sdkVersion = 35
        override val releaseVersion = "15"
        override val model = "model"
        override val manifacturer = "manufacturer"
        override val product = "product"
        override val abis = arrayOf("arm64-v8a")
    }

    private class FakeDeviceConfiguration : DfeDeviceConfiguration {
        override val touchScreen = 3
        override val keyboard = 1
        override val navigation = 1
        override val screenLayout = 2
        override val hasHardKeyboard = false
        override val hasFiveWayNavigation = false
        override val lowRamDevice = 0
        override val maxNumOfCPUCores = 8
        override val totalMemoryBytes = 8_000_000_000L
        override val deviceClass = 0
        override val screenDensity = 420
        override val screenWidth = 1080
        override val screenHeight = 2400
        override val sharedLibraries = emptyList<String>()
        override val features = emptyList<String>()
        override val locales = listOf("en_US")
        override val glEsVersion = 0
        override val glExtensions = emptyList<String>()
        override val isWideScreen = false
    }
}
