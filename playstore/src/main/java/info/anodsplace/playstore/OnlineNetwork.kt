package info.anodsplace.playstore


import com.android.volley.Network
import com.android.volley.NetworkResponse
import com.android.volley.NoConnectionError
import com.android.volley.Request
import info.anodsplace.framework.net.NetworkConnectivity

class OnlineNetwork(private val connection: NetworkConnectivity,private val network: Network): Network {
    override fun performRequest(request: Request<*>?): NetworkResponse {
        if (connection.isNetworkAvailable) {
            return network.performRequest(request)
        }
        throw NoConnectionError()
    }
}
