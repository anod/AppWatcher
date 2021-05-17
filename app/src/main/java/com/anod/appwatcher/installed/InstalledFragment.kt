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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.provide
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.watchlist.*
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.DialogSingleChoice
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InstalledFragment : WatchListFragment(), ActionMode.Callback {
    private val menuAction = EventFlow<MenuAction>()
    private val search = SearchMenu(menuAction)
    private val importViewModel: ImportInstalledViewModel by viewModels()
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun switchImportMode(selectionMode: Boolean, animated: Boolean) {
        importViewModel.selectionMode = selectionMode
        if (importViewModel.selectionMode) {
            if (animated) {
                binding.actionButton.show()
            } else {
                binding.actionButton.isVisible = true
            }
            binding.actionButton.isEnabled = importViewModel.hasSelection
            actionMode = (activity as? ToolbarActivity)?.startActionMode(this)
            updateTitle()
        } else {
            binding.actionButton.hide()
            importViewModel.clearSelection()
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
                    menuAction.tryEmit(SortMenuAction(index))
                    dialog.dismiss()
                }.show()
                return true
            }
            R.id.menu_act_select -> {
                switchImportMode(!importViewModel.selectionMode, animated = true)
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
            switchImportMode(!importViewModel.selectionMode, animated = false)
        }

        binding.actionButton.setOnClickListener { startImport() }

        importViewModel.selectionChange.observe(viewLifecycleOwner) { change ->
            binding.actionButton.isEnabled = change.extras.getBoolean("hasSelection")
            updateTitle()
            viewModel.updateSelection(change) { key -> importViewModel.getPackageSelection(key) }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            menuAction.collectLatest {
                when (it) {
                    is SearchQueryAction -> {
                        viewModel.titleFilter = it.query
                        reload()
                    }
                    is SortMenuAction -> {
                        viewModel.sortId = it.sortId
                        reload()
                    }
                    is FilterMenuAction -> {
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            importViewModel.progress.collect { status ->
                when (status) {
                    is ImportNotStarted -> {
                    }
                    is ImportStarted -> {
                        binding.actionButton.isEnabled = false
                    }
                    is ImportProgress -> {
                    }
                    is ImportFinished -> {
                        Toast.makeText(context, R.string.import_done, Toast.LENGTH_LONG).show()
                        importViewModel.clearSelection()
                        reload()
                    }
                }
            }
        }

        val vm = viewModel as InstalledViewModel
        lifecycleScope.launch {
            vm.changelogAdapter.updated.collect {
                AppLog.d("InstalledFragment changelog update collected")
                reload()
            }
        }

        lifecycleScope.launch {
            provide.packageRemoved.collect {
                AppLog.d("Package removed: $it")
                if (importViewModel.progress.value !is ImportProgress) {
                    reload()
                }
            }
        }
    }

    override suspend fun onReload() {
        val vm = viewModel as InstalledViewModel
        if (vm.changelogAdapter.authToken.isNotEmpty()) {
            return
        }
        val account = vm.account ?: return
        try {
            val token = AuthTokenBlocking(requireContext().applicationContext).retrieve(account)
            if (token.isNotBlank()) {
                vm.changelogAdapter.authToken = token
            } else {
                AppLog.e("Error retrieving token")
            }
        } catch (e: AuthTokenStartIntent) {
            startActivity(e.intent)
        } catch (e: Exception) {
            AppLog.e("onResume", e)
        }
    }

    private fun startImport() {
        val account = provide.prefs.account
        if (account == null) {
            Toast.makeText(context, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            return
        }

        binding.actionButton.isEnabled = false

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
                    selectPackage(app, action.index)
                } else {
                    openAppDetails(app)
                }
            }
            is ItemLongClick -> {
                if (!importViewModel.selectionMode) {
                    switchImportMode(true, animated = true)
                    selectPackage(action.app, action.index)
                    reload()
                }
            }
            else -> {
            }
        }
    }

    private fun selectPackage(app: App, index: Int) {
        if (app.rowId < 0) {
            importViewModel.toggle(app.packageName, index)
        } else {
            Toast.makeText(activity, R.string.app_already_added, Toast.LENGTH_SHORT).show()
        }
    }

    override fun config() = WatchListPagingSource.Config(
            showRecentlyUpdated = false,
            showOnDevice = true,
            showRecentlyInstalled = false,
            selectionMode = importViewModel.selectionMode
    )

    class Factory(
            private val sortId: Int,
            private val showImportAction: Boolean
    ) : FragmentFactory("recently-installed-$sortId-$showImportAction") {

        override fun create(): Fragment = InstalledFragment().also {
            it.arguments = Bundle().apply {
                putInt(ARG_FILTER, Filters.TAB_ALL)
                putInt(ARG_SORT, sortId)
                putBoolean(ARG_SHOW_ACTION, showImportAction)
            }
        }
    }

    companion object {
        fun intent(importMode: Boolean, context: Context, themeRes: Int, themeColors: CustomThemeColors): Intent {
            return intent(
                    if (importMode) Preferences.SORT_NAME_ASC else Preferences.SORT_DATE_DESC,
                    importMode,
                    context, themeRes, themeColors
            )
        }

        private fun intent(sortId: Int, showImportAction: Boolean, context: Context, themeRes: Int, themeColors: CustomThemeColors) = InstalledActivity.intent(
                context,
                Factory(sortId, showImportAction),
                themeRes,
                themeColors
        )
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
        switchImportMode(false, animated = true)
        reload()
    }
}