package com.anod.appwatcher


import com.android.volley.Network
import com.android.volley.NetworkResponse
import com.android.volley.NoConnectionError
import com.android.volley.Request

class OnlineNetwork(private val connection: NetworkConnection,private val network: Network): Network {
    override fun performRequest(request: Request<*>?): NetworkResponse {
        if (connection.isNetworkAvailable) {
            return network.performRequest(request)
        }
        throw NoConnectionError()
    }
}
