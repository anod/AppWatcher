package finsky.api

import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.VolleyError

class DfeRetryPolicy : DefaultRetryPolicy {
    private val dfeApiContext: DfeApiContext
    private var hadAuthException: Boolean = false

    constructor(initialTimeoutMs: Int, maxNumRetries: Int, backoffMultiplier: Float, dfeApiContext1: DfeApiContext)
            : super(initialTimeoutMs, maxNumRetries, backoffMultiplier) {
        this.dfeApiContext = dfeApiContext1
    }

    constructor(dfeApiContext: DfeApiContext) : super() {
        this.dfeApiContext = dfeApiContext
    }

    @Throws(VolleyError::class)
    override fun retry(volleyError: VolleyError?) {
        if (volleyError is AuthFailureError) {
            if (this.hadAuthException) {
                throw volleyError
            }
            this.hadAuthException = true
        }
        super.retry(volleyError)
    }
}