package com.anod.appwatcher.compose

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.AppIconLoader

@Composable
fun AppIconImage(
    app: App,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    appIconLoader: AppIconLoader
) {
    val imageRequest by remember(app.iconUrl) {
        derivedStateOf {
            appIconLoader.request(app.iconUrl)
        }
    }
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        imageLoader = appIconLoader.coilLoader,
        modifier = modifier.size(size),
        placeholder = painterResource(id = R.drawable.ic_app_icon_placeholder),
        error = painterResource(id = R.drawable.ic_app_icon_placeholder)
    )
}