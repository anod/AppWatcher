package com.anod.appwatcher.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner


class ComposableViewModelStoreOwner : ViewModelStoreOwner {
    private var store: ViewModelStore? = null
    override val viewModelStore: ViewModelStore
        get() {
            if (store == null) {
                store = ViewModelStore()
            }
            return store!!
        }
}

@Composable
fun rememberViwModeStoreOwner() : ViewModelStoreOwner {
    val owner = remember { ComposableViewModelStoreOwner() }
    DisposableEffect(key1 = owner) {
        onDispose {
            owner.viewModelStore.clear()
        }
    }
    return owner
}