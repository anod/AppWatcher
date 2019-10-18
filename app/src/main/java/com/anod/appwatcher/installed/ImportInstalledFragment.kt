package com.anod.appwatcher.installed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenAsync
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.framework.content.AppTitleComparator
import kotlinx.android.synthetic.main.fragment_import_installed.*


/**
 * @author Alex Gavrishev
 * *
 * @date 19/04/2016.
 */
class ImportInstalledFragment : Fragment() {

    companion object {
        private class Factory : FragmentFactory("import_installed") {
            override fun create() = ImportInstalledFragment()
        }

        fun intent(context: Context, themeRes: Int, themeColors: CustomThemeColors) = FragmentToolbarActivity.intent(
                Factory(),
                Bundle.EMPTY,
                themeRes,
                themeColors,
                context)
    }

    private var allSelected: Boolean = false
    private val appComponent: AppComponent?
        get() = if (context == null) null else Application.provide(context!!)
    private val viewModel: ImportInstalledViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_import_installed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = LinearLayoutManager(context!!)
        list.adapter = ImportAdapter(context!!, context!!.packageManager, viewModel.dataProvider, this)
        list.itemAnimator = ImportItemAnimator()

        activity?.title = getString(R.string.import_installed)

        button3.setOnClickListener {
            val importAdapter = list.adapter as ImportAdapter
            allSelected = !allSelected
            viewModel.selectAllPackages(allSelected)
            importAdapter.notifyDataSetChanged()
        }

        button2.setOnClickListener {
            activity?.finish()
        }

        button1.setOnClickListener {
            button3.visibility = View.GONE
            button1.visibility = View.GONE
            val adapter = list.adapter as ImportAdapter

            viewModel.reset()
            adapter.clearPackageIndex()
            for (idx in 0 until adapter.itemCount) {
                val installedPackage = adapter.installedPackages[idx]
                if (viewModel.addPackage(installedPackage.packageName, installedPackage.versionCode)) {
                    adapter.storePackageIndex(installedPackage.packageName, idx)
                }
            }
            if (viewModel.isEmpty) {
                activity?.finish()
            } else {
                startImport()

            }
        }

        viewModel.installedPackages.observe(this, Observer { data ->
            progressBar.visibility = View.GONE
            val downloadedAdapter = list.adapter as ImportAdapter
            downloadedAdapter.installedPackages = data.sortedWith(AppTitleComparator(1))
        })

        viewModel.progress.observe(this) {
            when (it) {
                is ImportFinished -> button2.setText(android.R.string.ok)
            }
        }
    }

    private fun startImport() {
        val account = appComponent?.prefs?.account
        if (account == null) {
            Toast.makeText(context, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            return
        }

        AuthTokenAsync(context!!).request(activity, account) { token ->
            if (token.isNotBlank()) {
                viewModel.import(account, token)
            } else {
                if (context == null) {
                    activity?.finish()
                    return@request
                }
                if (appComponent?.networkConnection?.isNetworkAvailable == true) {
                    Toast.makeText(context, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.check_connection, Toast.LENGTH_SHORT).show()
                }
                activity?.finish()
            }
        }
    }
}
