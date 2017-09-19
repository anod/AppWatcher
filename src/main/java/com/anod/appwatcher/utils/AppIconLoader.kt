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
import java.io.IOException


class AppIconLoader(context: Context) {
    private val mContext: Context = context.applicationContext
    private var mPicasso: Picasso? = null
    private var mIconSize = -1

    internal class PackageIconRequestHandler(private val mContext: Context) : RequestHandler() {
        private val mPackageManager: PackageManager = mContext.packageManager

        override fun canHandleRequest(data: Request): Boolean {
            return SCHEME == data.uri.scheme
        }

        @Throws(IOException::class)
        override fun load(request: Request, networkPolicy: Int): RequestHandler.Result? {

            val part = request.uri.schemeSpecificPart
            val cmp = ComponentName.unflattenFromString(part)

            val icon = PackageManagerUtils.loadIcon(cmp, mContext.resources.displayMetrics, mPackageManager) ?: return null
            return RequestHandler.Result(icon, DISK)
        }

    }

    internal class IconDbRequestHandler(private val mContext: Context) : RequestHandler() {

        override fun canHandleRequest(data: Request): Boolean {
            return DbContentProvider.matchIconUri(data.uri)
        }

        @Throws(IOException::class)
        override fun load(request: Request, networkPolicy: Int): RequestHandler.Result? {
            val client = DbContentProviderClient(mContext)
            val icon = client.queryAppIcon(request.uri)
            client.close()
            if (icon == null) {
                return null
            }
            return RequestHandler.Result(icon, DISK)
        }
    }

    private fun picasso(): Picasso {
        if (mPicasso == null) {
            mPicasso = Picasso.Builder(mContext)
                    .addRequestHandler(PackageIconRequestHandler(mContext))
                    .addRequestHandler(IconDbRequestHandler(mContext))
                    .build()
        }
        return mPicasso!!
    }

    fun retrieve(uri: Uri): RequestCreator {
        if (mIconSize == -1) {
            mIconSize = mContext.resources.getDimensionPixelSize(R.dimen.icon_size)
        }
        return picasso().load(uri)
                .resize(mIconSize, mIconSize)
                .centerInside()
                .onlyScaleDown()
    }


    fun retrieve(imageUrl: String): RequestCreator {
        if (mIconSize == -1) {
            mIconSize = mContext.resources.getDimensionPixelSize(R.dimen.icon_size)
        }
        return picasso().load(imageUrl)
                .resize(mIconSize, mIconSize)
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
        mPicasso?.shutdown()
        mPicasso = null
    }

    companion object {
        const val SCHEME = "application.icon"
    }
}