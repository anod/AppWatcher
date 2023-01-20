package com.anod.appwatcher.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.Document
import finsky.protos.AppDetails
import finsky.protos.DocDetails
import finsky.protos.DocV2
import org.koin.java.KoinJavaComponent

@Composable
fun MarketAppItem(app: App, onClick: () -> Unit, isWatched: Boolean, isInstalled: Boolean, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    Box()
    {
        Row(
                modifier = Modifier
                        .clickable(enabled = true, onClick = onClick)
                        .background(color = if (isWatched) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f) else MaterialTheme.colorScheme.surface)
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                        .heightIn(min = 68.dp)
        ) {

            val imageRequest = remember(app.iconUrl) {
                appIconLoader.request(app.iconUrl)
            }
            AsyncImage(
                    model = imageRequest,
                    contentDescription = app.title,
                    imageLoader = appIconLoader.coilLoader,
                    modifier = Modifier
                            .size(40.dp)
                            .padding(top = 8.dp),
                    placeholder = painterResource(id = R.drawable.ic_app_icon_placeholder)
            )

            Column(
                    modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(text = app.title, style = MaterialTheme.typography.bodyLarge)
                if (app.creator.isNotEmpty()) {
                    Text(
                        text = app.creator,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = app.uploadDate,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                    )
                    if (isInstalled) {
                        Text(
                                text = stringResource(id = R.string.installed).toUpperCase(Locale.current),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        val context = LocalContext.current
                        val offerText by remember(app.price) {
                            mutableStateOf(
                                    when {
                                        app.price.micros == null -> ""
                                        app.price.micros == 0 -> context.getString(R.string.free)
                                        else -> app.price.text
                                    })
                        }
                        if (offerText.isNotEmpty()) {
                            Text(
                                    text = offerText,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp)
                .align(alignment = Alignment.BottomEnd),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}


@Composable
@Preview(showBackground = true, showSystemUi = true)
fun MarketAppItemPreview() {
    val appIconLoader = AppIconLoader.Simple(
            LocalContext.current,
            ImageLoader.Builder(LocalContext.current).build()
    )
    val doc = Document(
            doc = DocV2.newBuilder().run {
                title = "App Watcher"
                creator = "Me"
                details = DocDetails.newBuilder().run {
                    appDetails = AppDetails.newBuilder().run {
                        uploadDate = "25 Aug 2022"
                        packageName = "info.anodsplace.appwatcher"
                        build()
                    }
                    build()
                }
                build()
            }
    )
    val app = App(doc, UploadDateParserCache())
    AppTheme {
        Column {
            MarketAppItem(app = app, onClick = { }, isWatched = true, isInstalled = true, appIconLoader = appIconLoader)
            MarketAppItem(app = app, onClick = { }, isWatched = false, isInstalled = true, appIconLoader = appIconLoader)
            MarketAppItem(app = app, onClick = { }, isWatched = true, isInstalled = false, appIconLoader = appIconLoader)
            MarketAppItem(app = app, onClick = { }, isWatched = false, isInstalled = false, appIconLoader = appIconLoader)
        }
    }
}