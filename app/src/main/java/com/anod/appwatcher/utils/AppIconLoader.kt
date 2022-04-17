package com.anod.appwatcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.ImageRequest
import coil.request.Options
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.loadIcon
import info.anodsplace.graphics.BitmapByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer

class AppIconLoader(context: Context) {
    private val context: Context = context.applicationContext
    private var iconPath: Path = AdaptiveIconTransformation.maskToPath(Application.provide(context).prefs.iconShape)
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
                .components {
                    add(PackageIconFetcher.Factory(context))
                }
                .build()
    }

    private val iconSize: Int by lazy {
        context.resources.getDimensionPixelSize(R.dimen.icon_size)
    }

    internal class PackageIconFetcher(private val context: Context, private val data: Uri, private val options: Options) : Fetcher {

        class Factory(private val context: Context) : Fetcher.Factory<Uri> {
            override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
                return if (data.scheme == SCHEME) PackageIconFetcher(context, data, options) else null
            }
        }

        private val packageManager: PackageManager = context.packageManager

        override suspend fun fetch(): FetchResult? {
            val part = data.schemeSpecificPart
            val cmp = ComponentName.unflattenFromString(part) ?: return null

            val icon = packageManager.loadIcon(cmp, context.resources.displayMetrics) ?: return null

            val source = BitmapByteArray.flatten(icon) ?: return  null
            return SourceResult(
                    source = ImageSource(Buffer().apply { write(source) }, options.context),
                    mimeType = null,
                    dataSource = DataSource.DISK
            )
        }
    }

    suspend fun get(imageUrl: String): Drawable? = withContext(Dispatchers.IO) {
        return@withContext try {
            imageLoader.execute(request(imageUrl)).drawable
        } catch (e: Exception) {
            AppLog.e(e)
            null
        }
    }

    fun retrieve(imageUrl: String, customize: (ImageRequest.Builder) -> Unit)  {
        imageLoader.enqueue(request(imageUrl, customize))
    }

    fun loadAppIntoImageView(app: App, iconView: ImageView, @DrawableRes defaultRes: Int) {
        val request = request(app.iconUrl) {
            it.placeholder(defaultRes)
            it.target(iconView)
        }
        imageLoader.enqueue(request)
    }

    private fun request(imageUrl: String, customize: (ImageRequest.Builder) -> Unit = {}): ImageRequest {
        return ImageRequest.Builder(context).apply {
            data(imageUrl.ifEmpty { null })
            transformations(listOf(AdaptiveIconTransformation(context, iconPath, imageUrl)))
            size(iconSize, iconSize)
            customize(this)
        }.build()
    }

    fun setIconShape(mask: String) {
        this.iconPath = AdaptiveIconTransformation.maskToPath(mask)
    }

    companion object {
        const val SCHEME = "application.icon"
    }
}