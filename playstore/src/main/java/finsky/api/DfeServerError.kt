package finsky.api

import com.android.volley.VolleyError

internal class DfeServerError(var displayErrorHtml: String) : VolleyError() {

    override fun toString(): String {
        return "DisplayErrorMessage[" + this.displayErrorHtml + "]"
    }
}