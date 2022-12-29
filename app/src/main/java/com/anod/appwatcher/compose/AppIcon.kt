package com.anod.appwatcher.compose

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.AppIconLoader

@Composable
fun AppIcon(
    app: App,
    contentDescription: String,
    appIconLoader: AppIconLoader
) {
    val imageRequest = remember {
        mutableStateOf(appIconLoader.request(app.iconUrl))
    }
    AsyncImage(
        model = imageRequest.value,
        contentDescription = contentDescription,
        imageLoader = appIconLoader.coilLoader,
        modifier = Modifier.size(40.dp),
        placeholder = painterResource(id = R.drawable.ic_app_icon_placeholder)
    )
}