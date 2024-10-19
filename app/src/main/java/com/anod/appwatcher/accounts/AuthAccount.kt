package com.anod.appwatcher.accounts

import android.accounts.Account
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthAccount(
    val name: String,
    val type: String,
    val gfsId: String,
    val gfsIdToken: String,
    val deviceConfig: String
) : Parcelable

data class GfsIdResult(val gfsId: String, val token: String)

fun AuthAccount(androidAccount: Account, gfsIdResult: GfsIdResult, deviceConfig: String) = AuthAccount(
    name = androidAccount.name,
    type = androidAccount.type,
    gfsId = gfsIdResult.gfsId,
    gfsIdToken = gfsIdResult.token,
    deviceConfig = deviceConfig
)

fun AuthAccount.toAndroidAccount() = Account(name, type)