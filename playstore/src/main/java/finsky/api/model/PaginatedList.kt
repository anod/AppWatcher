package finsky.api.model

import com.android.volley.Request
import com.android.volley.VolleyError
import finsky.protos.nano.Messages
import java.util.*
import kotlin.math.max

class UrlOffsetPair(val offset: Int, val url: String)

abstract class PaginatedList<T, D>(
        private val urlOffsetList: MutableList<UrlOffsetPair>,
        private val autoLoadNextPage: Boolean = true) : DfeModel() {
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
            this.currentRequest = this.makeRequest(urlOffsetPair.url)
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
    protected abstract fun getItemsFromResponse(wrapper: Messages.Response.ResponseWrapper): Array<D>

    override val isReady: Boolean
        get() = this.lastResponse != null || this.items.size > 0

    protected abstract fun makeRequest(url: String): Request<*>

    override fun onErrorResponse(error: VolleyError) {
        this.isInErrorState = true
        this.clearTransientState()
        super.onErrorResponse(error)
    }

    override fun onResponse(wrapper: Messages.Response.ResponseWrapper) {
        this.lastResponse = wrapper
        val size = this.items.size
        val itemsFromResponse = this.getItemsFromResponse(wrapper)
        this.updateItemsUntilEndCount(itemsFromResponse.size)
        for (i in itemsFromResponse.indices) {
            if (i + this.currentOffset < this.items.size) {
                this.items[i + this.currentOffset] = itemsFromResponse[i]
            } else {
                this.items.add(itemsFromResponse[i])
            }
        }
        val nextPageUrl = this.getNextPageUrl(wrapper)
        if (!nextPageUrl.isNullOrEmpty() && (this.currentOffset == size || this.itemsRemoved)) {
            this.urlOffsetList.add(UrlOffsetPair(this.items.size, nextPageUrl))
        }
        if (this.itemsRemoved) {
            this.itemsRemoved = false
        }
        val offset = this.urlOffsetList[-1 + this.urlOffsetList.size].offset
        var moreAvailable = false
        if (items.size == offset) {
            moreAvailable = itemsFromResponse.isNotEmpty()
        }
        this.isMoreAvailable = moreAvailable && autoLoadNextPage
        this.clearTransientState()
        this.notifyDataSetChanged()
    }

    fun resetItems() {
        this.isMoreAvailable = true
        this.items.clear()
        this.notifyDataSetChanged()
    }

    fun startLoadItems() {
        if (this.isMoreAvailable && this.count == 0) {
            this.isInErrorState = false
            this.requestMoreItemsIfNoRequestExists(this.urlOffsetList[0])
        }
    }
}
