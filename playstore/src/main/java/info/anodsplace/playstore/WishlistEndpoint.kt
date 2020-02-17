package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import finsky.api.model.DfeList
import finsky.api.model.FilterComposite
import finsky.api.model.FilterPredicate
import finsky.api.model.ListAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.withContext

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
class WishListEndpoint(
        context: Context,
        requestQueue: RequestQueue,
        deviceInfoProvider: DeviceInfoProvider,
        account: Account,
        private val autoloadNext: Boolean) : PlayStoreEndpoint {
    override var authToken = ""

    private val dfeApi: DfeApi by lazy {
        DfeApiImpl(requestQueue, context, account, authToken, deviceInfoProvider)
    }

    var data: DfeList? = null
        internal set

    override fun reset() {
        data?.resetItems()
        _updates = null
        data = null
    }

    val count: Int
        get() = data?.count ?: 0

    var nameFilter = ""
        set(value) {
            field = value
            data = null
            reset()
        }

    private val predicate: FilterPredicate
        get() {
            if (nameFilter.isBlank()) {
                return AppDetailsFilter.predicate
            }
            return FilterComposite(listOf(
                    AppDetailsFilter.predicate,
                    AppNameFilter(nameFilter).predicate
            )).predicate
        }

    private var _updates: Flow<ListAvailable>? = null
    val updates: Flow<ListAvailable>
        get() {
            if (_updates == null) {
                _updates = data!!.updates.consumeAsFlow()
            }
            return _updates!!
        }

    override suspend fun start(): DfeList = withContext(Dispatchers.Main) {
        data = DfeList(dfeApi, dfeApi.createLibraryUrl(backendId, libraryId, 7, null), autoloadNext, predicate).also {
            it.startLoadItems()
        }
        return@withContext data!!
    }

    companion object {
        private const val libraryId = "u-wl"
        private const val backendId = 0
    }
}
