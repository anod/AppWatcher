package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import finsky.protos.Messages
import kotlinx.coroutines.channels.Channel
import java.util.*
import kotlin.math.max

class UrlOffsetPair(val offset: Int, val url: String)

class ListAvailable(val isFirst: Boolean, val requestedMoreItems: Boolean, val error: VolleyError?)

typealias FilteredDocumentList<D> = Pair<Array<D>, Int>

abstract class PaginatedList<T, D>(
        private val urlOffsetList: MutableList<UrlOffsetPair>,
        private val autoLoadNextPage: Boolean = true) : DfeModel(), Response.ErrorListener, Response.Listener<Messages.Response.ResponseWrapper> {
    private var currentOffset: Int = 0
    private var currentRequest: Request<*>? = null
    private val items = mutableListOf<D>()
    private var itemsRemoved: Boolean = false
    private var itemsUntilEndCount: Int = 4
    private var lastPositionRequested: Int = 0
    private var lastResponse: Messages.Response.ResponseWrapper? = null
    private var isMoreAvailable: Boolean = true
    private val windowDistance: Int = 12
    private var isInErrorState = false

    val updates = Channel<ListAvailable>(Channel.CONFLATED)

    constructor(url: String, autoLoadNextPage: Boolean)
            : this(mutableListOf(UrlOffsetPair(0, url)), autoLoadNextPage)

    private fun requestMoreItemsIfNoRequestExists(urlOffsetPair: UrlOffsetPair?) {
        if (!this.isInErrorState) {
            if (this.currentRequest != null && !this.currentRequest!!.isCanceled) {
                if (this.currentRequest!!.url.endsWith(urlOffsetPair!!.url)) {
                    return
                }
                this.currentRequest!!.cancel()
            }
            this.currentOffset = urlOffsetPair!!.offset
            this.currentRequest = this.makeRequest(urlOffsetPair.url, this, this)
        }
    }

    private fun updateItemsUntilEndCount(n: Int) {
        if (this.itemsUntilEndCount <= 0) {
            this.itemsUntilEndCount = 4
            return
        }
        this.itemsUntilEndCount = max(1, n / 4)
    }

    private fun clearTransientState() {
        this.currentRequest = null
    }

    val count: Int
        get() = this.items.size

    fun getItem(pos: Int, isLastPosition: Boolean): D? {
        if (isLastPosition) {
            this.lastPositionRequested = pos
        }
        require(pos >= 0) { "Can't return an item with a negative index: $pos" }
        val count = this.count
        var value: D? = null
        if (pos < count) {
            value = this.items[pos]
            if (this.autoLoadNextPage && this.isMoreAvailable && pos >= this.count - this.itemsUntilEndCount) {
                if (this.itemsRemoved) {
                    for (i in this.urlOffsetList.indices) {
                        if (this.urlOffsetList[i].offset > this.items.size) {
                            while (this.urlOffsetList.size > max(1, i)) {
                                this.urlOffsetList.removeAt(-1 + this.urlOffsetList.size)
                            }
                            val urlOffsetPair = this.urlOffsetList[-1 + this.urlOffsetList.size]
                            if (isLastPosition) {
                                this.requestMoreItemsIfNoRequestExists(urlOffsetPair)
                            }
                        }
                    }
                } else {
                    val urlOffsetPair2 = this.urlOffsetList[-1 + this.urlOffsetList.size]
                    if (isLastPosition) {
                        this.requestMoreItemsIfNoRequestExists(urlOffsetPair2)
                    }
                }
            }
            if (value == null) {
                var urlOffsetPair3: UrlOffsetPair? = null
                for (urlOffsetPair4 in this.urlOffsetList) {
                    if (urlOffsetPair4.offset > pos) {
                        break
                    }
                    urlOffsetPair3 = urlOffsetPair4
                }
                this.requestMoreItemsIfNoRequestExists(urlOffsetPair3)
            }
        }
        return value
    }

    val listPageUrls: List<String>
        get() {
            val list = ArrayList<String>(this.urlOffsetList.size)
            val iterator = this.urlOffsetList.iterator()
            while (iterator.hasNext()) {
                list.add(iterator.next().url)
            }
            return list
        }

    protected abstract fun getNextPageUrl(wrapper: Messages.Response.ResponseWrapper): String?
    protected abstract fun getItemsFromResponse(wrapper: Messages.Response.ResponseWrapper): FilteredDocumentList<D>

    override val isReady: Boolean
        get() = this.lastResponse != null || this.items.size > 0

    override fun onErrorResponse(error: VolleyError) {
        this.isInErrorState = true
        this.clearTransientState()
        val isFirst = this.urlOffsetList.size == 1
        updates.offer(ListAvailable(isFirst, false, error))
    }

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        this.lastResponse = responseWrapper
        val size = this.items.size
        val itemsFromResponse = this.getItemsFromResponse(responseWrapper)
        this.updateItemsUntilEndCount(itemsFromResponse.first.size)
        val isFirst = this.urlOffsetList.size == 1
        for (i in itemsFromResponse.first.indices) {
            if (i + this.currentOffset < this.items.size) {
                this.items[i + this.currentOffset] = itemsFromResponse.first[i]
            } else {
                this.items.add(itemsFromResponse.first[i])
            }
        }
        val nextPageUrl = this.getNextPageUrl(responseWrapper)
        var urlOffsetPair2: UrlOffsetPair? = null
        if (!nextPageUrl.isNullOrEmpty() && (this.currentOffset == size || this.itemsRemoved)) {
            urlOffsetPair2 = UrlOffsetPair(this.items.size, nextPageUrl)
            this.urlOffsetList.add(urlOffsetPair2)
        }
        if (this.itemsRemoved) {
            this.itemsRemoved = false
        }
        val offset = this.urlOffsetList[-1 + this.urlOffsetList.size].offset
        var moreAvailable = false
        if (items.size == offset) {
            moreAvailable = itemsFromResponse.second > 0
        }
        this.isMoreAvailable = moreAvailable && autoLoadNextPage
        this.clearTransientState()
        val requestMoreItems = moreAvailable && urlOffsetPair2 != null
        if (requestMoreItems) {
            this.requestMoreItemsIfNoRequestExists(urlOffsetPair2)
        }
        updates.offer(ListAvailable(isFirst, requestMoreItems, null))
    }

    fun resetItems() {
        this.isMoreAvailable = true
        this.items.clear()
        val isFirst = this.urlOffsetList.size == 1
        updates.offer(ListAvailable(isFirst, false, null))
    }

    fun startLoadItems() {
        if (this.isMoreAvailable && this.count == 0) {
            this.isInErrorState = false
            this.requestMoreItemsIfNoRequestExists(this.urlOffsetList[0])
        }
    }
}
