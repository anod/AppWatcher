package com.anod.appwatcher.wishlist

import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.search.ListEndpointPagingSource
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.DfeApi
import finsky.api.DfeListType
import finsky.protos.ResponseWrapper
import info.anodsplace.framework.content.InstalledApps

class WishListEndpointPagingSource(
    private val dfeApi: DfeApi,
    installedApps: InstalledApps,
    uploadDateParserCache: UploadDateParserCache
) : ListEndpointPagingSource(
    listType = DfeListType.ALL,
    installedApps = installedApps,
    appAdapter = { document -> App(document, uploadDateParserCache) }
) {
    override suspend fun execute(nextPageUrl: String): ResponseWrapper {
        return dfeApi.wishlist(nextPageUrl)
    }
}