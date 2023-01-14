package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.Document
import okhttp3.OkHttpClient

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class DetailsEndpoint(
    context: Context,
    http: OkHttpClient,
    deviceInfoProvider: DfeDeviceInfoProvider,
    account: Account,
    private val authTokenProvider: DfeAuthTokenProvider,
    private val detailsUrl: String
) {
    private val dfeApiProvider = DfeApiProvider(
        context = context,
        http = http,
        deviceInfoProvider = deviceInfoProvider,
        account = account
    )

    suspend fun execute(): Document? {
        val response = dfeApiProvider.provide(authToken = authTokenProvider.authToken).details(detailsUrl)
        return response.docV2?.let { Document(it) }
    }
}
