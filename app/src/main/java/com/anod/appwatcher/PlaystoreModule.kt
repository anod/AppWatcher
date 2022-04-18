package com.anod.appwatcher

import android.accounts.Account
import com.anod.appwatcher.accounts.AuthTokenBlocking
import finsky.api.BulkDocId
import info.anodsplace.playstore.BulkDetailsEndpoint
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.SearchEndpoint
import info.anodsplace.playstore.WishListEndpoint
import org.koin.core.module.Module
import org.koin.dsl.module

fun createPlayStoreModule(): Module = module {
    factory { (docIds: List<BulkDocId>) ->
        BulkDetailsEndpoint(get(), get(), get(), getOrNull()!!, docIds = docIds).also { it.authToken = get<AuthTokenBlocking>().token }
    }

    factory { (detailsUrl: String) ->
        val account: Account = getOrNull() ?: Account("empty", "empty")
        DetailsEndpoint(get(), get(), get(), account, detailsUrl).also {
            it.authToken = get<AuthTokenBlocking>().token
        }
    }

    factory { (query: String) ->
        SearchEndpoint(get(), get(), get(), getOrNull()!!, query).also {
            it.authToken = get<AuthTokenBlocking>().token
        }
    }

    factory {
        WishListEndpoint(get(), get(), get(), getOrNull()!!).also {
            it.authToken = get<AuthTokenBlocking>().token
        }
    }
}