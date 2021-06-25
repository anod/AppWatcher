package com.anod.appwatcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Path
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom.DISK
import com.squareup.picasso.Request
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.RequestHandler
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.loadIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class PicassoAppIcon(context: Context) {
    private val context: Context = context.applicationContext
    private var iconPath: Path = AdaptiveIconTransformation.maskToPath(Application.provide(context).prefs.iconShape)
    private val picasso: Picasso by lazy {
        Picasso.Builder(this.context)
                .addRequestHandler(PackageIconRequestHandler(this.context))
                .build()
    }

    private val iconSize: Int by lazy {
        context.resources.getDimensionPixelSize(R.dimen.icon_size)
    }

    internal class PackageIconRequestHandler(private val context: Context) : RequestHandler() {
        private val packageManager: PackageManager = context.packageManager

        override fun canHandleRequest(data: Request): Boolean {
            return SCHEME == data.uri.scheme
        }

        @Throws(IOException::class)
        override fun load(request: Request, networkPolicy: Int): Result? {

            val part = request.uri.schemeSpecificPart
            val cmp = ComponentName.unflattenFromString(part) ?: return null

            val icon = packageManager.loadIcon(cmp, context.resources.displayMetrics) ?: return null
            return Result(icon, DISK)
        }
    }

    suspend fun get(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            retrieve(imageUrl).get()
        } catch (e: Exception) {
            AppLog.e(e)
            null
        }
    }

    fun retrieve(imageUrl: String): RequestCreator {
        return picasso.load(if (imageUrl.isEmpty()) null else imageUrl)
                .transform(AdaptiveIconTransformation(context, iconPath, iconSize, imageUrl))
                .resize(iconSize, iconSize)
                .centerInside()
                .onlyScaleDown()
    }

    fun loadAppIntoImageView(app: App, iconView: ImageView, @DrawableRes defaultRes: Int) {
        this.retrieve(app.iconUrl)
                .transform(AdaptiveIconTransformation(context, iconPath, iconSize, app.iconUrl))
                .resize(iconSize, iconSize)
                .placeholder(defaultRes)
                .into(iconView)
    }

    fun setIconShape(mask: String) {
        this.iconPath = AdaptiveIconTransformation.maskToPath(mask)
    }

    companion object {
        const val SCHEME = "application.icon"
    }
}