package com.anod.appwatcher.utils

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val isBuildDebug: Boolean = Platform.isDebugBinary