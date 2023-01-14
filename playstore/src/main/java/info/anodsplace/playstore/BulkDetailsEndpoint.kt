package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.BulkDocId
import finsky.api.Document
import finsky.api.FilterPredicate
import finsky.protos.Details.BulkDetailsResponse
import okhttp3.OkHttpClient

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class BulkDetailsEndpoint(
    context: Context,
    http: OkHttpClient,
    deviceInfoProvider: DfeDeviceInfoProvider,
    account: Account,
    private val authTokenProvider: DfeAuthTokenProvider,
    private var docIds: List<BulkDocId>
) {
    private val dfeApiProvider = DfeApiProvider(
        context = context,
        http = http,
        deviceInfoProvider = deviceInfoProvider,
        account = account
    )

    suspend fun execute(): List<Document> {
        val response = dfeApiProvider.provide(authToken = authTokenProvider.authToken).details(docIds, includeDetails = true)
        return filterDocuments(response, AppDetailsFilter.predicate)
    }
}

private fun filterDocuments(response: BulkDetailsResponse, filter: FilterPredicate): List<Document> {
    val list = mutableListOf<Document>()
    for (i in response.entryList.indices) {
        response.getEntry(i).doc?.let {
            list.add(Document(it))
        }
    }

    return if (list.isEmpty()) {
        list
    } else {
        list.filter(filter)
    }
}