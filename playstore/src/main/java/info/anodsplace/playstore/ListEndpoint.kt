package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.DfeApi
import finsky.api.DfeList
import finsky.api.DfeListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
abstract class ListEndpoint(
    context: Context,
    http: OkHttpClient,
    deviceInfoProvider: DfeDeviceInfoProvider,
    account: Account,
    private val authTokenProvider: DfeAuthTokenProvider
) {
    private val dfeApiProvider = DfeApiProvider(
        context = context,
        http = http,
        deviceInfoProvider = deviceInfoProvider,
        account = account
    )

    suspend fun execute(nextPageUrl: String): DfeListResponse = withContext(Dispatchers.Main) {
        val defApi = dfeApiProvider.provide(authToken = authTokenProvider.authToken)
        return@withContext createDfeList(defApi, nextPageUrl).execute()
    }

    abstract fun createDfeList(dfeApi: DfeApi, nextPageUrl: String): DfeList
}
