package finsky.api.utils

import finsky.api.DfeDeviceBuild
import finsky.api.DfeDeviceConfiguration
import finsky.api.DfeDeviceInfoProvider
import finsky.protos.AndroidBuildProto
import finsky.protos.AndroidCheckinProto
import finsky.protos.AndroidCheckinRequest
import finsky.protos.DeviceConfigurationProto
import finsky.protos.DeviceFeature

fun checkinRequest(timeToReport: Long, deviceInfo: DfeDeviceInfoProvider): AndroidCheckinRequest = AndroidCheckinRequest.newBuilder().also {
    it.id = 0
    it.checkin = deviceInfo.toCheckInProto(timeToReport)
    it.locale = deviceInfo.locale.description
    it.timeZone = deviceInfo.timeZone
    it.version = 3
    it.deviceConfiguration = deviceInfo.configuration.toProto(deviceInfo.build.abis)
    it.fragment = 0
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

fun DfeDeviceConfiguration.toProto(abis: Array<String>): DeviceConfigurationProto = DeviceConfigurationProto.newBuilder().also {
    it.setTouchScreen(touchScreen)
    it.setKeyboard(keyboard)
    it.setNavigation(navigation)
    it.setScreenLayout(screenLayout)
    it.setHasHardKeyboard(hasHardKeyboard)
    it.setHasFiveWayNavigation(hasFiveWayNavigation)
    it.setLowRamDevice(lowRamDevice)
    it.setMaxNumOfCPUCores(maxNumOfCPUCores)
    it.setTotalMemoryBytes(totalMemoryBytes)
    it.setDeviceClass(deviceClass)
    it.setScreenDensity(screenDensity)
    it.setScreenWidth(screenWidth)
    it.setScreenHeight(screenHeight)
    it.addAllNativePlatform(abis.toList())
    it.addAllSystemSharedLibrary(sharedLibraries)
    it.addAllSystemAvailableFeature(features)
    it.addAllSystemSupportedLocale(locales)
    it.setGlEsVersion(glEsVersion)
    it.addAllGlExtension(glExtensions)
    it. addAllDeviceFeature(features.map { feature ->
        DeviceFeature.newBuilder().apply {
            name = feature
            value = 0
        }.build()
    })
}.build()