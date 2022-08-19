package com.anod.appwatcher.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.AppIconLoader
import finsky.api.model.Document
import info.anodsplace.framework.content.InstalledApps
import org.koin.java.KoinJavaComponent

@Composable
fun MarketAppItem(document: Document, onClick: () -> Unit, isWatched: Boolean, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    val app = document.appDetails
    val packageName = app.packageName

    val packageInfo by remember {
        mutableStateOf(installedApps.packageInfo(packageName))
    }

    Row(
            modifier = Modifier
                    .clickable(enabled = isWatched, onClick = onClick)
                    .background(color = if (isWatched) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .heightIn(min = 68.dp)
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ) {

        val imageRequest = remember {
            mutableStateOf(appIconLoader.request(document.iconUrl ?: ""))
        }
        AsyncImage(
                model = imageRequest.value,
                contentDescription = document.title,
                imageLoader = appIconLoader.coilLoader,
                modifier = Modifier.size(40.dp),
                placeholder = painterResource(id = R.drawable.ic_app_icon_placeholder)
        )

        Column(
                modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(text = document.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                    text = document.creator,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall
            )
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
                if (packageInfo.isInstalled) {
                    Text(
                            text = stringResource(id = R.string.installed),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    val context = LocalContext.current
                    val offerText by remember(document.offer) {
                        mutableStateOf(
                                when {
                                    document.offer.offerType == 0 -> ""
                                    document.offer.micros.toInt() == 0 -> context.getString(R.string.free)
                                    else -> document.offer.formattedAmount
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
            Divider(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}