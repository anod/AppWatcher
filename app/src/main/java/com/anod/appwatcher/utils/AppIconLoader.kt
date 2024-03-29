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
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.loadIcon
import info.anodsplace.graphics.AdaptiveIcon
import info.anodsplace.graphics.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer

interface AppIconLoader {
    val coilLoader: ImageLoader
    suspend fun get(imageUrl: String): Drawable?
    fun retrieve(imageUrl: String, customize: (ImageRequest.Builder) -> Unit)
    fun loadAppIntoImageView(app: App, iconView: ImageView, @DrawableRes defaultRes: Int)
    fun request(imageUrl: String, customize: (ImageRequest.Builder) -> Unit = {}): ImageRequest

    class Simple(context: Context, override val coilLoader: ImageLoader) : AppIconLoader {
        private val context: Context = context.applicationContext
        override suspend fun get(imageUrl: String): Drawable? = null

        override fun retrieve(imageUrl: String, customize: (ImageRequest.Builder) -> Unit) {
        }

        override fun loadAppIntoImageView(app: App, iconView: ImageView, defaultRes: Int) {
        }

        override fun request(imageUrl: String, customize: (ImageRequest.Builder) -> Unit): ImageRequest {
            return ImageRequest.Builder(context).data(imageUrl).build()
        }
    }
}

class RealAppIconLoader(context: Context, private val prefs: Preferences, override val coilLoader: ImageLoader) : AppIconLoader {
    private val context: Context = context.applicationContext
    private var _iconPathPair: Pair<Int, Path> = Pair(0, Path())
    private val iconPath: Path
        get() {
            val shapeHashCode = prefs.iconShape.hashCode()
            if (shapeHashCode != _iconPathPair.first) {
                _iconPathPair = Pair(shapeHashCode, AdaptiveIcon.maskToPath(prefs.iconShape))
            }
            return _iconPathPair.second
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

            val source = icon.toByteArray() ?: return null
            return SourceResult(
                    source = ImageSource(Buffer().apply { write(source) }, options.context),
                    mimeType = null,
                    dataSource = DataSource.DISK
            )
        }
    }

    override suspend fun get(imageUrl: String): Drawable? = withContext(Dispatchers.IO) {
        return@withContext try {
            coilLoader.execute(request(imageUrl)).drawable
        } catch (e: Exception) {
            AppLog.e(e)
            null
        }
    }

    override fun retrieve(imageUrl: String, customize: (ImageRequest.Builder) -> Unit) {
        coilLoader.enqueue(request(imageUrl, customize))
    }

    override fun loadAppIntoImageView(app: App, iconView: ImageView, @DrawableRes defaultRes: Int) {
        val request = request(app.iconUrl) {
            it.placeholder(defaultRes)
            it.target(iconView)
        }
        coilLoader.enqueue(request)
    }

    override fun request(imageUrl: String, customize: (ImageRequest.Builder) -> Unit): ImageRequest {
        return ImageRequest.Builder(context).apply {
            data(imageUrl.ifEmpty { null })
            transformations(listOf(AdaptiveIconTransformation(context, iconPath, imageUrl)))
            size(iconSize, iconSize)
            customize(this)
        }.build()
    }

    companion object {
        const val SCHEME = "application.icon"
    }
}