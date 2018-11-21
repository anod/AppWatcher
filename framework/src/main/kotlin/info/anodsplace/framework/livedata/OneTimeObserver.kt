package info.anodsplace.framework.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

class OneTimeObserver<T>(private val liveData: LiveData<T>, private val observer: Observer<T>): Observer<T> {
    override fun onChanged(t: T?) {
        observer.onChanged(t)
        liveData.removeObserver(this)
    }
}