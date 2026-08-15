package finsky.api

import finsky.protos.Details

internal fun createBulkDetailsRequest(
    docIds: List<BulkDocId>,
    includeDetails: Boolean
): Details.BulkDetailsRequest = Details.BulkDetailsRequest.newBuilder()
    .setIncludeDetails(includeDetails)
    .addAllDocid(docIds.map { it.packageName }.sorted())
    .build()

internal fun bulkDetailsUrl(forUpdateCheck: Boolean): String =
    if (forUpdateCheck) "${DfeApi.BULK_DETAILS_URI}?au=1" else DfeApi.BULK_DETAILS_URI