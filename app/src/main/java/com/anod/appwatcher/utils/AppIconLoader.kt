package com.anod.appwatcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Path
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil3.Image
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.placeholder
import coil3.request.target
import coil3.request.transformations
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.loadIcon
import info.anodsplace.graphics.AdaptiveIcon
import info.anodsplace.graphics.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source

interface AppIconLoader {
    val coilLoader: ImageLoader
    suspend fun get(imageUrl: String): Image?
    fun retrieve(imageUrl: String, customize: (ImageRequest.Builder) -> Unit)
    fun loadAppIntoImageView(app: App, iconView: ImageView, @DrawableRes defaultRes: Int)
    fun request(imageUrl: String, customize: (ImageRequest.Builder) -> Unit = {}): ImageRequest

    class Simple(context: Context, override val coilLoader: ImageLoader) : AppIconLoader {
        private val context: Context = context.applicationContext
        override suspend fun get(imageUrl: String): Image? = null

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
    private var iconPathPair: Pair<Int, Path> = Pair(0, Path())
    private val iconPath: Path
        get() {
            val shapeHashCode = prefs.iconShape.hashCode()
            if (shapeHashCode != iconPathPair.first) {
                iconPathPair = Pair(shapeHashCode, AdaptiveIcon.maskToPath(prefs.iconShape))
            }
            return iconPathPair.second
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
            return SourceFetchResult(
                source = ImageSource(
                    source = source.inputStream().source().buffer(),
                    fileSystem = options.fileSystem,
                    metadata = null
                ),
                mimeType = null,
                dataSource = DataSource.DISK
            )
        }
    }

    override suspend fun get(imageUrl: String): Image? = withContext(Dispatchers.IO) {
        return@withContext try {
            coilLoader.execute(request(imageUrl)).image
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