package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import finsky.api.model.DfeModel
import finsky.api.model.DfeSearch
import finsky.api.model.ListAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2015-02-21
 */
class SearchEndpoint(
        context: Context,
        requestQueue: RequestQueue,
        deviceInfoProvider: DeviceInfoProvider,
        account: Account,
        val query: String,
        private val autoLoadNextPage: Boolean
) : PlayStoreEndpoint {
    override var authToken = ""

    var data: DfeSearch? = null
        internal set

    private val dfeApi: DfeApi by lazy {
        DfeApiImpl(requestQueue, context, account, authToken, deviceInfoProvider)
    }

    override fun reset() {
        data?.resetItems()
        data = null
        _updates = null
    }

    val count: Int
        get() = data?.count ?: 0

    private var _updates: Flow<ListAvailable>? = null
    val updates: Flow<ListAvailable>
        get() {
            if (_updates == null) {
                _updates = data!!.updates.consumeAsFlow()
            }
            return _updates!!
        }

    override suspend fun start(): DfeModel = withContext(Dispatchers.Main) {
        data = DfeSearch(dfeApi, query, autoLoadNextPage, AppDetailsFilter.predicate).also {
            it.startLoadItems()
        }
        return@withContext data!!
    }
}
