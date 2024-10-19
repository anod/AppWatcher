package com.anod.appwatcher.accounts

import android.accounts.Account
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthAccount(
    val name: String,
    val type: String,
    val gfsId: String,
    val deviceCheckInConsistencyToken: String
) : Parcelable

data class GfsIdResult(val gfsId: String, val token: String)

fun AuthAccount(androidAccount: Account, gfsIdResult: GfsIdResult) = AuthAccount(
    name = androidAccount.name,
    type = androidAccount.type,
    gfsId = gfsIdResult.gfsId,
    deviceCheckInConsistencyToken = gfsIdResult.token
)

fun AuthAccount.toAndroidAccount() = Account(name, type)