package info.anodsplace.framework.content

import android.content.Context
import android.support.v4.content.Loader

class EmptyLoader<D>(context: Context,private val data: D): Loader<D>(context) {

    override fun onForceLoad() {
        deliverResult(this.data)
    }

    override fun onReset() {
        deliverCancellation()
    }

    override fun onStartLoading() {
        deliverResult(this.data)
    }

    override fun onStopLoading() {
        deliverCancellation()
    }

}