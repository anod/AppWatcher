package com.anod.appwatcher.utils


import java.util.*

/**
 * @author algavris
 * *
 * @date 08/10/2016.
 */

object CollectionsUtils {

    interface Predicate<T> {
        fun test(t: T): Boolean
    }

    fun <T> filter(source: List<T>, predicate: Predicate<in T>): List<T> {
        val result = ArrayList<T>(source.size)

        for (el in source) {
            if (!predicate.test(el)) {
                result.add(el)
            }
        }

        return result
    }
}
