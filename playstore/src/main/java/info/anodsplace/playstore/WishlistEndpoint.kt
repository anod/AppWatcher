package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import finsky.api.model.DfeList
import finsky.api.model.FilterComposite
import finsky.api.model.FilterPredicate
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

    override suspend fun start(): DfeList = suspendCancellableCoroutine { continuation ->
        data = DfeList(dfeApi, dfeApi.createLibraryUrl(backendId, libraryId, 7, null), autoloadNext, predicate).also {
            it.onFirstResponse = { error ->
                if (error == null) {
                    continuation.resume(it)
                } else {
                    continuation.resumeWithException(error)
                }
            }
            it.startLoadItems()
        }
    }

    companion object {
        private const val libraryId = "u-wl"
        private const val backendId = 0
    }
}
