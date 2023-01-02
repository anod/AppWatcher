package com.anod.appwatcher

import android.accounts.Account
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.preferences.Preferences
import finsky.api.BulkDocId
import info.anodsplace.playstore.BulkDetailsEndpoint
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.SearchEndpoint
import info.anodsplace.playstore.WishListEndpoint
import org.koin.core.module.Module
import org.koin.dsl.module

fun createPlayStoreModule(): Module = module {
    factory { (docIds: List<BulkDocId>) ->
        BulkDetailsEndpoint(get(), get(), get(), get<Preferences>().account!!, docIds = docIds).also { it.authToken = get<AuthTokenBlocking>().token }
    }

    factory { (detailsUrl: String) ->
        val account: Account = get<Preferences>().account ?: Account("empty", "empty")
        DetailsEndpoint(get(), get(), get(), account, detailsUrl).also {
            it.authToken = get<AuthTokenBlocking>().token
        }
    }

    factory { (query: String) ->
        SearchEndpoint(get(), get(), get(), get<Preferences>().account!!, query).also {
            it.authToken = get<AuthTokenBlocking>().token
        }
    }

    factory {
        WishListEndpoint(get(), get(), get(), get<Preferences>().account!!).also {
            it.authToken = get<AuthTokenBlocking>().token
        }
    }
}