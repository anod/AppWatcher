// Copyright (c) 2020. Alex Gavrishev
package info.anodsplace.framework.util

import android.util.LruCache

fun <K, V> createLruCache(): LruCache<K, V> {
    val maxMemory = (Runtime.getRuntime().maxMemory() / 1024)
    // Use 1/8th of the available memory for this memory cache.
    val cacheSize = maxMemory / 8
    return LruCache(cacheSize.toInt())
}