package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import okhttp3.OkHttpClient

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
interface DfeDeviceInfoProvider {
    val deviceId: String
    val simOperator: String
}

interface DfeAuthTokenProvider {
    val authToken: String
}

class DfeApiProvider(
    context: Context,
    private val http: OkHttpClient,
    private val deviceInfoProvider: DfeDeviceInfoProvider,
    private val account: Account
)  {
    private val context: Context = context.applicationContext

    fun provide(authToken: String): DfeApi {
        if (authToken.isBlank()) {
            throw IllegalStateException("Auth token is empty")
        }
        return DfeApiImpl(http, context, account, authToken, deviceInfoProvider)
    }
}
