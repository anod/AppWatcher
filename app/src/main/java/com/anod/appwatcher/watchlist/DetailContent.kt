package com.anod.appwatcher.watchlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.details.DetailsPanel
import com.anod.appwatcher.navigation.LocalListDetailPaneScaffold
import com.anod.appwatcher.navigation.shouldUpdateDetailSystemBars
import com.anod.appwatcher.preferences.SelectedTheme

@Composable
fun DetailContent(app: App?, theme: SelectedTheme, onDismissRequest: () -> Unit) {
    if (app == null) {
        DetailPlaceholder(theme = theme)
    } else {
        DetailsPanel(
            app = app,
            onDismissRequest = onDismissRequest,
            updateSystemBars = shouldUpdateDetailSystemBars(LocalListDetailPaneScaffold.current),
        )
    }
}

@Composable
fun DetailPlaceholder(theme: SelectedTheme) {
    AppTheme(
        theme = theme,
        updateSystemBars = false
    ) {
        Surface {
            EmptyBoxSmile()
        }
    }
}

@Composable
fun EmptyBoxSmile() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = R.drawable.ic_empty_box_smile), contentDescription = null)
    }
}