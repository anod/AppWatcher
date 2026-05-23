package com.anod.appwatcher.navigation

import androidx.compose.runtime.staticCompositionLocalOf

val LocalListDetailPaneScaffold = staticCompositionLocalOf { false }

fun shouldUpdateDetailSystemBars(isInListDetailPaneScaffold: Boolean): Boolean = !isInListDetailPaneScaffold
