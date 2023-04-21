package com.anod.appwatcher.history

import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.search.ListEndpointPagingSource
import finsky.api.DfeApi
import finsky.api.DfeListType
import finsky.protos.ResponseWrapper
import info.anodsplace.framework.content.InstalledApps
import java.text.DateFormat
import java.util.Date

class HistoryEndpointPagingSource(
    private val dfeApi: DfeApi,
    private val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM),
    installedApps: InstalledApps,
) : ListEndpointPagingSource(
    listType = DfeListType.ALL,
    installedApps = installedApps,
    appAdapter = { d ->
        App(
            rowId = -1,
            appId = d.docId,
            packageName = d.docId,
            versionNumber = 0,
            versionName = "",
            title = d.title,
            creator = "",
            iconUrl = d.iconUrl ?: "",
            status = App.STATUS_NORMAL,
            uploadDate = d.purchaseTimestampMillis?.let { timestamp ->
                dateFormat.format(Date(timestamp))
            } ?: "",
            price = d.purchaseOffer?.let { offer ->
                Price(
                    text = offer.formattedAmount ?: "",
                    cur = offer.currencyCode ?: "",
                    micros = offer.micros.toInt()
                )
            } ?: Price("", "", 0),
            detailsUrl = d.detailsUrl,
            uploadTime = d.purchaseTimestampMillis ?: 0L,
            appType = "",
            updateTime = System.currentTimeMillis(),
        )
    }
) {
    override suspend fun execute(nextPageUrl: String): ResponseWrapper {
        return dfeApi.purchaseHistory(nextPageUrl)
    }
}