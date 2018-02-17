package finsky.api.model

import com.android.volley.Response
import com.android.volley.VolleyError
import finsky.protos.nano.Messages

import java.util.HashSet

interface OnDataChangedListener {
    fun onDataChanged()
}

typealias FilterPredicate = ((Document?) -> Boolean)

abstract class DfeRequestModel: DfeModel() {
    open fun execute() {
        execute(this, this)
    }
    abstract fun execute(responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener)
}

abstract class DfeModel : Response.ErrorListener, Response.Listener<Messages.Response.ResponseWrapper> {
    private val errorListeners = HashSet<Response.ErrorListener>()
    private val listeners = HashSet<OnDataChangedListener>()

    fun addDataChangedListener(onDataChangedListener: OnDataChangedListener) {
        this.listeners.add(onDataChangedListener)
    }

    fun addErrorListener(errorListener: Response.ErrorListener) {
        this.errorListeners.add(errorListener)
    }

    abstract val isReady: Boolean

    protected fun notifyDataSetChanged() {
        val array = this.listeners.toTypedArray()
        for (i in array.indices) {
            array[i].onDataChanged()
        }
    }

    private fun notifyErrorOccurred(volleyError: VolleyError) {
        val array = this.errorListeners.toTypedArray()
        for (i in array.indices) {
            array[i].onErrorResponse(volleyError)
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        this.notifyErrorOccurred(error)
    }

    fun removeDataChangedListener(onDataChangedListener: OnDataChangedListener) {
        this.listeners.remove(onDataChangedListener)
    }

    fun removeErrorListener(errorListener: Response.ErrorListener) {
        this.errorListeners.remove(errorListener)
    }

    fun unregisterAll() {
        this.listeners.clear()
        this.errorListeners.clear()
    }
}
