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
fun DetailContent(app: App?, onDismissRequest: () -> Unit) {
    Surface {
        if (app == null) {
            EmptyBoxSmile()
        } else {
            DetailsPanel(
                app = app,
                onDismissRequest = onDismissRequest,
            )
        }
    }
}

@Composable
fun EmptyBoxSmile() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = R.drawable.ic_empty_box_smile), contentDescription = null)
    }
}