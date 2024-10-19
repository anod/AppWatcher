package finsky.api.utils

import finsky.api.DfeDeviceBuild
import finsky.api.DfeDeviceConfiguration
import finsky.api.DfeDeviceInfoProvider
import finsky.protos.AndroidBuildProto
import finsky.protos.AndroidCheckinProto
import finsky.protos.AndroidCheckinRequest
import finsky.protos.DeviceConfigurationProto
import finsky.protos.DeviceFeature

fun checkinRequest(timeToReport: Long, deviceInfo: DfeDeviceInfoProvider) = AndroidCheckinRequest.newBuilder().apply {
    id = 0
    checkin = deviceInfo.toCheckInProto(timeToReport)
    locale = ""
    timeZone = ""
    version = 3
    deviceConfiguration = deviceInfo.configuration.toProto(deviceInfo.build.abis)
}.build()

private fun DfeDeviceInfoProvider.toCheckInProto(timeToReport: Long) = AndroidCheckinProto.newBuilder().also {
    it.build = build.toProto(
        client = client,
        otaInstalled = otaInstalled,
        gsfVersion = gsfVersion.toInt(),
        timestamp = timeToReport
    )
    it.lastCheckinMsec = 0
    it.cellOperator = cellOperator
    it.simOperator = simOperator
    it.roaming = roaming
    it.userNumber = 0
}.build()

private fun DfeDeviceBuild.toProto(
    client: String,
    otaInstalled: Boolean,
    gsfVersion: Int,
    timestamp: Long
): AndroidBuildProto = AndroidBuildProto.newBuilder().also {
    it.id = fingerprint
    it.product = hardware
    it.carrier = brand
    it.radio = radio
    it.bootloader = bootloader
    it.device = device
    it.sdkVersion = sdkVersion
    it.model = model
    it.manufacturer = manifacturer
    it.buildProduct = product
    it.client = client
    it.otaInstalled = otaInstalled
    it.timestamp = timestamp
    it.googleServices = gsfVersion
}.build()

fun DfeDeviceConfiguration.toProto(abis: Array<String>): DeviceConfigurationProto = DeviceConfigurationProto.newBuilder().apply {
    setTouchScreen(touchScreen)
    setKeyboard(keyboard)
    setNavigation(navigation)
    setScreenLayout(screenLayout)
    setHasHardKeyboard(hasHardKeyboard)
    setHasFiveWayNavigation(hasFiveWayNavigation)
    setLowRamDevice(lowRamDevice)
    setMaxNumOfCPUCores(maxNumOfCPUCores)
    setTotalMemoryBytes(totalMemoryBytes)
    setDeviceClass(deviceClass)
    setScreenDensity(screenDensity)
    setScreenWidth(screenWidth)
    setScreenHeight(screenHeight)
    addAllNativePlatform(abis.toList())
    addAllSystemSharedLibrary(sharedLibraries)
    addAllSystemAvailableFeature(features)
    addAllSystemSupportedLocale(locales)
    setGlEsVersion(glEsVersion)
    addAllGlExtension(glExtensions)
    addAllDeviceFeature(features.map {
        DeviceFeature.newBuilder().apply {
            name = it
            value = 0
        }.build()
    })
}.build()