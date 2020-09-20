// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.anod.appwatcher.ChangelogActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.details.DetailsActivity
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.provide
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.watchlist.*
import info.anodsplace.framework.app.*
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.android.synthetic.main.fragment_applist.*
import kotlinx.coroutines.launch

class InstalledFragment : WatchListFragment(), ActionMode.Callback {
    private val menuAction = SingleLiveEvent<MenuAction>()
    private val search = SearchMenu(menuAction)
    private val importViewModel: ImportInstalledViewModel by viewModels()
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        menuAction.observe(this) {
            when (it) {
                is SearchQueryAction -> {
                    viewModel.titleFilter = it.query
                    reload()
                }
                is SortMenuAction -> {
                    viewModel.sortId = it.sortId
                    reload()
                }
            }
        }

        importViewModel.progress.observe(this, Observer { status ->
            when (status) {
                is ImportStarted -> {
                    actionButton.isEnabled = false
                }
                is ImportProgress -> {
                }
                is ImportFinished -> {
                    actionButton.isEnabled = true
                }
            }
        })
    }

    private fun toggleImportMode(animated: Boolean) {
        importViewModel.selectionMode = !importViewModel.selectionMode
        if (importViewModel.selectionMode) {
            if (animated) {
                actionButton.show()
            } else {
                actionButton.isVisible = true
            }
            actionButton.isEnabled = importViewModel.hasSelection
            actionMode = (activity as? ToolbarActivity)?.startActionMode(this)
            updateTitle()
        } else {
            actionButton.hide()
        }
    }

    private fun updateTitle() {
        val selectedCount = importViewModel.selectedCount
        if (selectedCount == -1) {
            actionMode?.title = getString(R.string.number_selected, adapter.itemCount)
        } else {
            actionMode?.title = getString(R.string.number_selected, importViewModel.selectedCount)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.installed, menu)
        search.init(menu.findItem(R.id.menu_act_search), requireContext())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_act_sort -> {
                DialogSingleChoice(requireContext(), R.style.AlertDialog, R.array.sort_titles, viewModel.sortId) { dialog, index ->
                    menuAction.value = SortMenuAction(index)
                    dialog.dismiss()
                }.show()
                return true
            }
            R.id.menu_act_select -> {
                toggleImportMode(true)
                reload()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = InstalledViewModel(application) as T
        }
    }

    override fun getItemSelection(appItem: AppListItem): AppViewHolder.Selection {
        if (appItem.app.rowId < 0) {
            return importViewModel.getPackageSelection(appItem.app.packageName)
        }
        return AppViewHolder.Selection.Disabled
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.installed)

        val showImportAction = requireArguments().getBoolean(ARG_SHOW_ACTION)
        if (showImportAction) {
            toggleImportMode(false)
        }

        actionButton.setOnClickListener {
            startImport()
        }

        importViewModel.selectionChange.observe(viewLifecycleOwner) { change ->
            actionButton.isEnabled = change.hasSelection
            updateTitle()
            if (change.key == null) {
                viewModel.selection.value = Pair(null, if (change.defaultSelected) AppViewHolder.Selection.Selected else AppViewHolder.Selection.NotSelected)
            } else {
                viewModel.selection.value = Pair(change.key, importViewModel.getPackageSelection(change.key))
            }
        }
    }

    private fun startImport() {
        val account = provide.prefs.account
        if (account == null) {
            Toast.makeText(context, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            return
        }

        actionButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val token = AuthTokenBlocking(requireContext()).retrieve(account)
                if (token.isNotBlank()) {
                    importViewModel.import(account, token)
                } else {
                    if (context == null) {
                        activity?.finish()
                        return@launch
                    }
                    if (provide.networkConnection.isNetworkAvailable) {
                        Toast.makeText(context, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, R.string.check_connection, Toast.LENGTH_SHORT).show()
                    }
                    activity?.finish()
                }
            } catch (e: AuthTokenStartIntent) {
                startActivitySafely(e.intent)
                activity?.finish()
            }
        }
    }

    override fun onListAction(action: WishListAction) {
        when (action) {
            is ItemClick -> {
                val app = action.app
                if (importViewModel.selectionMode) {
                    selectPackage(app)
                } else {
                    openAppDetails(app)
                }
            }
            is ItemLongClick -> {
                if (!importViewModel.selectionMode) {
                    toggleImportMode(true)
                    selectPackage(action.app)
                    reload()
                }
            }
        }
    }

    private fun selectPackage(app: App) {
        if (app.rowId < 0) {
            importViewModel.toggle(app.packageName)
        } else {
            Toast.makeText(activity, R.string.app_already_added, Toast.LENGTH_SHORT).show()
        }
    }

    override fun openAppDetails(app: App) {
        val intent = Intent(requireContext(), ChangelogActivity::class.java).apply {
            putExtra(DetailsActivity.EXTRA_APP_ID, app.appId)
            putExtra(DetailsActivity.EXTRA_ROW_ID, app.rowId)
            putExtra(DetailsActivity.EXTRA_DETAILS_URL, app.detailsUrl)
        }
        startActivity(intent)
    }

    override fun config(filterId: Int) = WatchListPagingSource.Config(
            showRecentlyUpdated = false,
            showOnDevice = true,
            showRecentlyInstalled = false,
            selectionMode = importViewModel.selectionMode
    )

    class Factory(
            private val sortId: Int,
            private val showImportAction: Boolean
    ) : FragmentFactory("recently-installed-$sortId-$showImportAction") {

        override fun create(): Fragment? = InstalledFragment().also {
            it.arguments = Bundle().apply {
                putInt(ARG_FILTER, Filters.TAB_ALL)
                putInt(ARG_SORT, sortId)
                putBoolean(ARG_SHOW_ACTION, showImportAction)
            }
        }
    }

    companion object {
        fun intent(sortId: Int, showImportAction: Boolean, context: Context, themeRes: Int, themeColors: CustomThemeColors) = FragmentToolbarActivity.intent(
                Factory(sortId, showImportAction),
                Bundle.EMPTY,
                themeRes,
                themeColors,
                context)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        (activity as? ToolbarActivity)?.menuInflater?.inflate(R.menu.selection, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_act_select_all -> {
                importViewModel.selectAll(true)
                return true
            }
            R.id.menu_act_select_none -> {
                importViewModel.selectAll(false)
                return true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        toggleImportMode(true)
        reload()
    }
}