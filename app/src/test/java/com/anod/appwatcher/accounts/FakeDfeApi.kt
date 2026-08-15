package com.anod.appwatcher.accounts

import finsky.api.BulkDocId
import finsky.api.DfeApi
import finsky.api.DfeDeviceIdentity
import finsky.api.PatchFormat
import finsky.protos.AndroidCheckinResponse
import finsky.protos.DeliveryResponse
import finsky.protos.Details
import finsky.protos.ResponseWrapper
import finsky.protos.UploadDeviceConfigResponse
import java.util.ArrayDeque

internal class FakeDfeApi : DfeApi {
    override val authenticated = true
    var checkInCalls = 0
    var uploadCalls = 0
    var detailsCalls = 0
    val uploadIdentities = mutableListOf<DfeDeviceIdentity>()
    val detailsFailures = ArrayDeque<Throwable>()
    val checkInFailures = ArrayDeque<Throwable>()
    val uploadFailures = ArrayDeque<Throwable>()
    var uploadFailure: Throwable? = null
    var detailsResponse: Details.DetailsResponse? = null
    var beforeCheckIn: suspend () -> Unit = {}
    var checkInResponse: AndroidCheckinResponse = AndroidCheckinResponse.newBuilder()
        .setAndroidId(1234L)
        .setDeviceCheckinConsistencyToken("checkin-token")
        .build()
    var uploadResponse: UploadDeviceConfigResponse = UploadDeviceConfigResponse.newBuilder()
        .setUploadDeviceConfigToken("config-token")
        .build()

    override suspend fun search(initialQuery: String, nextPageUrl: String): ResponseWrapper =
        error("Unused")

    override suspend fun details(appDetailsUrl: String): Details.DetailsResponse {
        detailsCalls++
        if (detailsFailures.isNotEmpty()) {
            throw detailsFailures.removeFirst()
        }
        return detailsResponse ?: error("Details response not configured")
    }

    override suspend fun details(
        docIds: List<BulkDocId>,
        includeDetails: Boolean,
        forUpdateCheck: Boolean
    ): Details.BulkDetailsResponse =
        error("Unused")

    override suspend fun delivery(
        docId: String,
        installedVersionCode: Int,
        updateVersionCode: Int,
        offerType: Int,
        patchFormats: Array<PatchFormat>
    ): DeliveryResponse = error("Unused")

    override suspend fun wishlist(nextPageUrl: String): ResponseWrapper = error("Unused")

    override suspend fun purchaseHistory(nextPageUrl: String): ResponseWrapper = error("Unused")

    override suspend fun checkIn(): AndroidCheckinResponse {
        beforeCheckIn()
        checkInCalls++
        if (checkInFailures.isNotEmpty()) {
            throw checkInFailures.removeFirst()
        }
        return checkInResponse
    }

    override suspend fun uploadDeviceConfig(identity: DfeDeviceIdentity): UploadDeviceConfigResponse {
        uploadCalls++
        uploadIdentities.add(identity)
        if (uploadFailures.isNotEmpty()) {
            throw uploadFailures.removeFirst()
        }
        uploadFailure?.let { throw it }
        return uploadResponse
    }
}