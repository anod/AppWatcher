package com.anod.appwatcher.history

import android.accounts.Account
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.utils.BaseFlowViewModel
import finsky.api.DfeApi
import finsky.api.FilterComposite
import finsky.api.FilterPredicate
import info.anodsplace.framework.app.HingeDeviceLayout
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.playstore.AppNameFilter
import info.anodsplace.playstore.PaidHistoryFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DateFormat
import java.util.Date

data class HistoryListState(
    val account: Account? = null,
    val authToken: String = "",
    val nameFilter: String = "",
    val watchingPackages: List<String> = emptyList(),
    val wideLayout: HingeDeviceLayout = HingeDeviceLayout(),
    val selectedApp: App? = null,
)

sealed interface HistoryListAction {
    class ShowTagSnackbar(val info: App) : HistoryListAction
    class ActivityAction(val action: CommonActivityAction) : HistoryListAction
}

sealed interface HistoryListEvent {
    object OnBackPress : HistoryListEvent
    class OnNameFilter(val query: String) : HistoryListEvent
    class SelectApp(val app: App?) : HistoryListEvent
    class SetWideLayout(val wideLayout: HingeDeviceLayout) : HistoryListEvent
}

class HistoryListViewModel(account: Account?, authToken: String, wideLayout: HingeDeviceLayout) : BaseFlowViewModel<HistoryListState, HistoryListEvent, HistoryListAction>(), KoinComponent {

    class Factory(
        private val account: Account?,
        private val authToken: String,
        private val wideLayout: HingeDeviceLayout
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return HistoryListViewModel(account, authToken, wideLayout) as T
        }
    }

    private val database: AppsDatabase by inject()
    private val dfeApi: DfeApi by inject()
    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    private val packageManager: PackageManager by inject()
    val installedApps by lazy { InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)) }

    init {
        viewState = HistoryListState(
            account = account,
            authToken = authToken,
            wideLayout = wideLayout
        )
        viewModelScope.launch {
            database.apps().observePackages().map { list ->
                list.map { it.packageName }
            }.collect {
                viewState = viewState.copy(watchingPackages = it)
            }
        }
    }

    private var _pagingData: Flow<PagingData<App>>? = null
    val pagingData: Flow<PagingData<App>>
        get() {
            if (_pagingData == null) {
                _pagingData = createPager()
            }
            return _pagingData!!
        }

    private fun createPager() = Pager(
        PagingConfig(
            pageSize = 17,
            enablePlaceholders = false,
            initialLoadSize = 17,
            prefetchDistance = 17 * 2,
            maxSize = 204
        )
    ) {
        HistoryEndpointPagingSource(dfeApi)
    }
        .flow
        .cachedIn(viewModelScope)
        .combine(viewStates.map { it.nameFilter }.distinctUntilChanged()) { pageData, nameFilter ->
            val predicate = predicate(nameFilter)
            pageData
                .filter { d -> predicate(d) }
                .map { d ->
                    App(
                        rowId = 0,
                        appId = d.docId,
                        packageName = d.docId,
                        versionNumber = 0,
                        versionName = "",
                        title = d.title,
                        creator = "",
                        iconUrl = d.iconUrl ?: "",
                        status = App.STATUS_NORMAL,
                        uploadDate = d.purchaseTimestampMillis?.let { timestamp ->
                            dateFormat.format(Date(timestamp))
                        } ?: "",
                        price = d.purchaseOffer?.let { offer ->
                            Price(
                                text = offer.formattedAmount ?: "",
                                cur = offer.currencyCode ?: "",
                                micros = offer.micros.toInt()
                            )
                        } ?: Price("", "", 0),
                        detailsUrl = d.detailsUrl,
                        uploadTime = d.purchaseTimestampMillis ?: 0L,
                        appType = "",
                        updateTime = System.currentTimeMillis(),
                    )
                }
        }

    override fun handleEvent(event: HistoryListEvent) {
        when (event) {
            HistoryListEvent.OnBackPress -> emitAction(HistoryListAction.ActivityAction(CommonActivityAction.Finish))
            is HistoryListEvent.OnNameFilter -> viewState = viewState.copy(nameFilter = event.query)
            is HistoryListEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }
            is HistoryListEvent.SetWideLayout -> {
                viewState = viewState.copy(wideLayout = event.wideLayout)
            }
        }
    }

    private fun predicate(nameFilter: String): FilterPredicate {
        if (nameFilter.isBlank()) {
            return FilterComposite(listOf(
                PaidHistoryFilter.hasPrice,
                PaidHistoryFilter.noPurchaseStatus
            )).predicate
        }
        return FilterComposite(listOf(
            PaidHistoryFilter.hasPrice,
            PaidHistoryFilter.noPurchaseStatus,
            AppNameFilter(nameFilter).containsQuery
        )).predicate
    }
}