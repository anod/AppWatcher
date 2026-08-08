package com.anod.appwatcher.accounts

import android.os.Build
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeApi
import finsky.api.DfeDeviceIdentity
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

sealed class DeviceRegistrationException(message: String) : IllegalStateException(message)

class DeviceRegistrationPendingException :
    DeviceRegistrationException("Previous device check-in did not return a reusable identity")

class DeviceRegistrationRequiredException :
    DeviceRegistrationException("Device registration requires explicit confirmation")

internal class DeviceRegistration(
    private val preferences: Preferences,
    private val dfeApi: DfeApi,
    private val sdkInt: Int
) {
    companion object {
        internal const val DEVICE_CONFIG_REVISION = 1
    }

    suspend fun ensure(
        account: AuthAccount,
        allowCheckIn: Boolean
    ): AuthAccount {
        val persistedAccount = preferences.account
        check(
            persistedAccount == null ||
                (persistedAccount.name == account.name && persistedAccount.type == account.type)
        ) {
            "Active account changed during device registration"
        }
        val activeAccount = persistedAccount ?: account

        if (sdkInt < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return activeAccount
        }

        var registeredAccount = activeAccount
        if (registeredAccount.gfsId.isEmpty()) {
            if (!allowCheckIn) {
                if (preferences.isDeviceRegistrationPending) {
                    throw DeviceRegistrationPendingException()
                }
                throw DeviceRegistrationRequiredException()
            }
            registeredAccount = withContext(NonCancellable) {
                if (!preferences.isDeviceRegistrationPending) {
                    check(preferences.saveDeviceRegistrationPending(true)) {
                        "Unable to persist device registration state"
                    }
                }
                val response = dfeApi.checkIn()
                check(response.androidId != 0L) { "Incorrect androidId" }
                val checkedInAccount = registeredAccount.copy(
                    gfsId = java.lang.Long.toHexString(response.androidId),
                    gfsIdToken = response.deviceCheckinConsistencyToken.orEmpty(),
                    deviceConfig = ""
                )
                check(
                    preferences.saveAccount(
                        checkedInAccount,
                        deviceRegistrationPending = false,
                        deviceRegistrationAuthorized = null,
                        deviceConfigRevision = 0
                    )
                ) {
                    "Unable to persist device identity"
                }
                checkedInAccount
            }
        }

        if (
            registeredAccount.deviceConfig.isNotEmpty() &&
            preferences.deviceConfigRevision < DEVICE_CONFIG_REVISION
        ) {
            registeredAccount = registeredAccount.copy(deviceConfig = "")
            check(
                preferences.saveAccount(
                    registeredAccount,
                    deviceRegistrationPending = null,
                    deviceRegistrationAuthorized = null,
                    deviceConfigRevision = 0
                )
            ) {
                "Unable to invalidate legacy device config token"
            }
        }

        if (registeredAccount.deviceConfig.isEmpty()) {
            val identity = DfeDeviceIdentity(
                deviceId = registeredAccount.gfsId,
                deviceCheckinConsistencyToken = registeredAccount.gfsIdToken,
                deviceConfigToken = ""
            )
            val configToken = dfeApi.uploadDeviceConfig(identity).uploadDeviceConfigToken
            check(configToken.isNotEmpty()) { "Device config token is empty" }
            registeredAccount = registeredAccount.copy(deviceConfig = configToken)
            check(
                preferences.saveAccount(
                    registeredAccount,
                    deviceRegistrationPending = null,
                    deviceRegistrationAuthorized = null,
                    deviceConfigRevision = DEVICE_CONFIG_REVISION
                )
            ) {
                "Unable to persist device config token"
            }
        }

        return registeredAccount
    }
}