package com.anod.appwatcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.support.annotation.DrawableRes
import android.text.TextUtils
import android.widget.ImageView
import com.anod.appwatcher.R
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppInfo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom.DISK
import com.squareup.picasso.Request
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.RequestHandler
import info.anodsplace.framework.content.loadIcon
import java.io.IOException

class PicassoAppIcon(context: Context) {
    private val context: Context = context.applicationContext
    private val picasso: Picasso by lazy {
        Picasso.Builder(this.context)
            .addRequestHandler(PackageIconRequestHandler(this.context))
            .addRequestHandler(IconDbRequestHandler(this.context))
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
        override fun load(request: Request, networkPolicy: Int): RequestHandler.Result? {

            val part = request.uri.schemeSpecificPart
            val cmp = ComponentName.unflattenFromString(part)

            val icon = packageManager.loadIcon(cmp, context.resources.displayMetrics) ?: return null
            return RequestHandler.Result(icon, DISK)
        }
    }

    internal class IconDbRequestHandler(private val context: Context) : RequestHandler() {

        override fun canHandleRequest(data: Request): Boolean {
            return DbContentProvider.matchIconUri(data.uri)
        }

        @Throws(IOException::class)
        override fun load(request: Request, networkPolicy: Int): RequestHandler.Result? {
            val client = DbContentProviderClient(context)
            val icon = client.queryAppIcon(request.uri)
            client.close()
            if (icon == null) {
                return null
            }
            return RequestHandler.Result(icon, DISK)
        }
    }

    fun retrieve(uri: Uri): RequestCreator {
        return picasso.load(uri)
                .resize(iconSize, iconSize)
                .centerInside()
                .onlyScaleDown()
    }

    fun retrieve(imageUrl: String): RequestCreator {
        return picasso.load(imageUrl)
                .resize(iconSize, iconSize)
                .centerInside()
                .onlyScaleDown()
    }

    fun loadAppIntoImageView(app: AppInfo, iconView: ImageView, @DrawableRes defaultRes: Int) {
        if (TextUtils.isEmpty(app.iconUrl)) {
            if (app.rowId > 0) {
                val dbImageUri = DbContentProvider.iconsUri.buildUpon().appendPath(app.rowId.toString()).build()
                this.retrieve(dbImageUri)
                        .placeholder(defaultRes)
                        .into(iconView)
            } else {
                iconView.setImageResource(defaultRes)
            }
        } else {
            this.retrieve(app.iconUrl)
                    .placeholder(defaultRes)
                    .into(iconView)
        }
    }

    fun shutdown() {
        picasso.shutdown()
    }

    companion object {
        const val SCHEME = "application.icon"
    }
}