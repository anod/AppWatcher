package com.anod.appwatcher.accounts

import com.anod.appwatcher.preferences.Preferences
import finsky.api.BulkDocId
import finsky.api.DfeApi
import finsky.api.DfeDeviceIdentity
import finsky.api.DfeServerError
import finsky.api.PatchFormat
import finsky.protos.AndroidCheckinResponse
import finsky.protos.DeliveryResponse
import finsky.protos.Details
import finsky.protos.ResponseWrapper
import finsky.protos.UploadDeviceConfigResponse

internal class AuthRecoveringDfeApi(
    private val delegate: DfeApi,
    private val authToken: AuthTokenBlocking,
    private val preferences: Preferences
) : DfeApi {
    override val authenticated: Boolean
        get() = delegate.authenticated

    override suspend fun search(initialQuery: String, nextPageUrl: String): ResponseWrapper =
        withAuthenticationRecovery { delegate.search(initialQuery, nextPageUrl) }

    override suspend fun details(appDetailsUrl: String): Details.DetailsResponse =
        withAuthenticationRecovery { delegate.details(appDetailsUrl) }

    override suspend fun details(
        docIds: List<BulkDocId>,
        includeDetails: Boolean,
        forUpdateCheck: Boolean
    ): Details.BulkDetailsResponse =
        withAuthenticationRecovery {
            delegate.details(docIds, includeDetails, forUpdateCheck)
        }

    override suspend fun delivery(
        docId: String,
        installedVersionCode: Int,
        updateVersionCode: Int,
        offerType: Int,
        patchFormats: Array<PatchFormat>
    ): DeliveryResponse = withAuthenticationRecovery {
        delegate.delivery(
            docId,
            installedVersionCode,
            updateVersionCode,
            offerType,
            patchFormats
        )
    }

    override suspend fun wishlist(nextPageUrl: String): ResponseWrapper =
        withAuthenticationRecovery { delegate.wishlist(nextPageUrl) }

    override suspend fun purchaseHistory(nextPageUrl: String): ResponseWrapper =
        withAuthenticationRecovery { delegate.purchaseHistory(nextPageUrl) }

    override suspend fun checkIn(): AndroidCheckinResponse = delegate.checkIn()

    override suspend fun uploadDeviceConfig(identity: DfeDeviceIdentity): UploadDeviceConfigResponse =
        withAuthenticationRecovery { delegate.uploadDeviceConfig(identity) }

    private suspend fun <T> withAuthenticationRecovery(request: suspend () -> T): T {
        val account = preferences.account
        val rejectedToken = authToken.tokenFor(account)
        try {
            return request()
        } catch (e: DfeServerError) {
            if (
                !e.isAuthenticationError ||
                account == null ||
                rejectedToken.isEmpty() ||
                !isActive(account)
            ) {
                throw e
            }
            val result = authToken.refreshAfterAuthenticationFailure(
                account.toAndroidAccount(),
                rejectedToken
            )
            if (result !is CheckTokenResult.Success || !isActive(account)) {
                throw e
            }
            return request()
        }
    }

    private fun isActive(account: AuthAccount): Boolean {
        val activeAccount = preferences.account
        return activeAccount?.name == account.name && activeAccount.type == account.type
    }
}