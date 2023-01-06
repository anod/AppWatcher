package com.anod.appwatcher.watchlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.details.DetailsPanel

@Composable
fun DetailContent(app: App?) {
    Surface {
        if (app == null) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(id = R.drawable.ic_empty_box_smile), contentDescription = null)
            }
        } else {
            DetailsPanel(appId = app.appId, rowId = app.rowId, detailsUrl = app.detailsUrl ?: "", onDismissRequest = {}, onCommonActivityAction = {})
        }
    }
}