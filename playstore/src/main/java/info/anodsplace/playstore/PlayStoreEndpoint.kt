package info.anodsplace.playstore

import com.android.volley.VolleyError
import finsky.api.model.DfeModel

/**
 * @author algavris
 * *
 * @date 27/08/2016.
 */
interface PlayStoreEndpoint {
    var listener: Listener?
    var authToken: String

    fun startAsync()
    fun startSync()
    fun reset()

    interface Listener {
        fun onDataChanged(data: DfeModel)
        fun onErrorResponse(error: VolleyError)
    }
}
