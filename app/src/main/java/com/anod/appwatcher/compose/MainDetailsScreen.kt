package com.anod.appwatcher.compose

import android.graphics.Rect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.anod.appwatcher.watchlist.DetailContent
import info.anodsplace.framework.app.FoldableDeviceLayout

@Composable
fun MainDetailScreen(wideLayout: FoldableDeviceLayout, main: @Composable () -> Unit, detail: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            main()
        }
        val hingeWidth = wideLayout.hinge.width()
        if (hingeWidth > 0) {
            val widthInDp = with(LocalDensity.current) { hingeWidth.toDp() }
            Spacer(modifier = Modifier
                .width(widthInDp)
                .fillMaxHeight())
        }
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            detail()
        }
    }
}

@Preview(device = Devices.FOLDABLE)
@Composable
private fun MainDetailScreenPreview() {
    AppTheme {
        MainDetailScreen(
            wideLayout = FoldableDeviceLayout(isWideLayout = true, hinge = Rect(0, 0, 80, 0)),
            main = { DetailContent(app = null, onDismissRequest = {}) },
            detail = { DetailContent(app = null, onDismissRequest = {}) },
        )
    }
}