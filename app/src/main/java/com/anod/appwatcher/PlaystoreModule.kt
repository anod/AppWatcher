package com.anod.appwatcher

import android.accounts.Account
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PlaystoreAuthTokenProvider
import finsky.api.BulkDocId
import info.anodsplace.playstore.BulkDetailsEndpoint
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.DfeAuthTokenProvider
import info.anodsplace.playstore.SearchEndpoint
import info.anodsplace.playstore.WishListEndpoint
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun createPlayStoreModule(): Module = module {
    factoryOf(::PlaystoreAuthTokenProvider) {
        bind<DfeAuthTokenProvider>()
    }
    factory { (docIds: List<BulkDocId>) ->
        BulkDetailsEndpoint(
            context = get(),
            http = get(),
            deviceInfoProvider = get(),
            account = get<Preferences>().account!!,
            authTokenProvider = get(),
            docIds = docIds,
        )
    }

    factory { (detailsUrl: String) ->
        val account: Account = get<Preferences>().account ?: Account("empty", "empty")
        DetailsEndpoint(
            context = get(),
            http = get(),
            deviceInfoProvider = get(),
            account = account,
            authTokenProvider = get(),
            detailsUrl = detailsUrl
        )
    }

    factory { (query: String) ->
        SearchEndpoint(
            context = get(),
            http = get(),
            deviceInfoProvider = get(),
            account = get<Preferences>().account!!,
            authTokenProvider = get(),
            initialQuery = query
        )
    }

    factory {
        WishListEndpoint(
            context = get(),
            http = get(),
            deviceInfoProvider = get(),
            account = get<Preferences>().account!!,
            authTokenProvider = get(),
        )
    }
}