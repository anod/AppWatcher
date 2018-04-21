package finsky.api

import com.android.volley.VolleyError

internal class DfeServerError(message: String) : VolleyError(message) {

    override fun toString(): String {
        return "DisplayErrorMessage[$message]"
    }

}