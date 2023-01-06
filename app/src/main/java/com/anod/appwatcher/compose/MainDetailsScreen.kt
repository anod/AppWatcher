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
import info.anodsplace.framework.app.HingeDeviceLayout

@Composable
fun MainDetailScreen(
        wideLayout: HingeDeviceLayout,
        main: @Composable () -> Unit,
        detail: @Composable () -> Unit,
) {
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
fun MainDetailScreen() {
    AppTheme {
        MainDetailScreen(
                wideLayout = HingeDeviceLayout(isWideLayout = true, hinge = Rect(0, 0, 80, 0)),
                main = { DetailContent(app = null) },
                detail = { DetailContent(app = null) },
        )
    }
}