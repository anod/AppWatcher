package finsky.api.utils

import android.net.Uri
import android.text.TextUtils
import finsky.api.DfeDeviceInfoProvider

const val GFS_VERSION_CODE = 203019037L
const val PLAY_VERSION_CODE = 82201710L
const val PLAY_VERSION_NAME = "22.0.17-21 [0] [PR] 332555730"
const val CLIENT_ID = "am-android-google"

fun DfeDeviceInfoProvider.makeUserAgentString(): String {

    val params: List<Pair<String, String>> = listOf(
        "api" to "3",
        "versionCode" to playVersionCode.toString(),
        "sdk" to build.sdkVersion.toString(),
        "device" to a(build.device),
        "hardware" to a(build.hardware),
        "product" to a(build.product),
        "platformVersionRelease" to a(build.releaseVersion),
        "model" to a(build.model),
        "buildId" to a(build.id),
        "isWideScreen" to configuration.isWideScreen.toString(),
        "supportedAbis" to a(build.abis)
    )

    return "Android-Finsky/$playVersionName (${params.joinToString(separator = ",") { "${it.first}=${it.second}" }})"

}

private fun a(replace: String?): String {
    if (replace.isNullOrBlank()) {
        return ""
    }
    return Uri.encode(replace).replace("(", "%28").replace(")", "%29")
}

private fun a(array: Array<String>): String {
    val array2 = arrayOfNulls<String>(array.size)
    for (i in array.indices) {
        array2[i] = a(array[i])
    }
    return TextUtils.join(";", array2)
}
