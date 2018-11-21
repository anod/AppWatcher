package info.anodsplace.framework.app

import android.app.Application
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import android.util.LruCache
import androidx.fragment.app.Fragment

/**
 * @author Alex Gavrishev
 * @date 25/10/2017
 */

interface ApplicationInstance {
    val notificationManager: NotificationManager
    val memoryCache: LruCache<String, Any?>
    val nightMode: Int

    fun sendBroadcast(intent: Intent)
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}

class ApplicationContext(context: Context) {

    constructor(application: Application) : this(application.applicationContext)

    val actual: Context = context.applicationContext as Context
    private val app: ApplicationInstance = context.applicationContext as ApplicationInstance

    val contentResolver: ContentResolver
        get() = actual.contentResolver
    val notificationManager: NotificationManager
        get() = app.notificationManager
    val memoryCache: LruCache<String, Any?>
        get() = app.memoryCache
    val nightMode: Int
        get() = app.nightMode
    val resources: Resources
        get() = actual.resources
    val packageManager: PackageManager
        get() = actual.packageManager

    fun getString(@StringRes resId: Int): String {
        return actual.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return actual.getString(resId, *formatArgs)
    }

    @ColorInt
    fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(actual, colorRes)
    }

    fun sendBroadcast(intent: Intent) {
        actual.sendBroadcast(intent)
    }
}

fun Fragment.applicationContext(): ApplicationContext {
    return ApplicationContext(this.activity!!)
}