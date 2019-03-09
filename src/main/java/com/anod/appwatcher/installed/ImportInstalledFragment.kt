package com.anod.appwatcher.installed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.collection.SimpleArrayMap
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenAsync
import com.anod.appwatcher.content.WatchAppList
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.map
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.framework.content.*
import kotlinx.android.synthetic.main.fragment_import_installed.*

/**
 * @author Alex Gavrishev
 * *
 * @date 19/04/2016.
 */

class ImportInstalledViewModel(application: android.app.Application) : AndroidViewModel(application) {
    val installedPackages = appComponent.database.apps().observePackages()
            .map { watchingPackages ->
                val map = watchingPackages.associateBy { it.packageName }
                application.packageManager.getInstalledPackages().filter {
                    !map.containsKey(it.packageName)
                }
            }

    private val appComponent: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

}

class ImportInstalledFragment : Fragment(), ImportBulkManager.Listener {

    companion object {
        private const val TAG = "import_installed"

        fun intent(context: Context, themeRes: Int, themeColors: CustomThemeColors) = FragmentToolbarActivity.intent(
                TAG,
                { ImportInstalledFragment() },
                themeRes,
                themeColors,
                Bundle(),
                context)
    }

    private var allSelected: Boolean = false
    private val dataProvider: ImportResourceProvider by lazy { ImportResourceProvider(context!!, InstalledApps.MemoryCache(InstalledApps.PackageManager(context!!.packageManager))) }
    private val importManager: ImportBulkManager by lazy { ImportBulkManager(context!!, this) }
    private val appComponent: AppComponent
        get() = Application.provide(context!!)
    private val viewModel: ImportInstalledViewModel by lazy { ViewModelProviders.of(this).get(ImportInstalledViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_import_installed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = LinearLayoutManager(context!!)
        list.adapter = ImportAdapter(context!!, context!!.packageManager, dataProvider)
        list.itemAnimator = ImportItemAnimator()

        button3.setOnClickListener {
            val importAdapter = list.adapter as ImportAdapter
            allSelected = !allSelected
            dataProvider.selectAllPackages(allSelected)
            importAdapter.notifyDataSetChanged()
        }

        button2.setOnClickListener {
            if (dataProvider.isImportStarted) {
                importManager.stop()
            }
            activity?.finish()
        }

        button1.setOnClickListener {
            button3.visibility = View.GONE
            button1.visibility = View.GONE
            val adapter = list.adapter as ImportAdapter

            importManager.init()
            adapter.clearPackageIndex()
            for (idx in 0 until adapter.itemCount) {
                val installedPackage = adapter.installedPackages[idx]
                if (dataProvider.isPackageSelected(installedPackage.packageName)) {
                    importManager.addPackage(installedPackage.packageName, installedPackage.versionCode)
                    adapter.storePackageIndex(installedPackage.packageName, idx)
                }
            }
            if (importManager.isEmpty) {
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
    }

    private fun startImport() {
        val account = appComponent.prefs.account
        if (account == null) {
            Toast.makeText(context, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            return
        }

        AuthTokenAsync(context!!).request(activity, account, object : AuthTokenAsync.Callback {
            override fun onToken(token: String) {
                importManager.start(account, token)
            }

            override fun onError(errorMessage: String) {
                if (appComponent.networkConnection.isNetworkAvailable) {
                    Toast.makeText(context, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.check_connection, Toast.LENGTH_SHORT).show()
                }
                activity?.finish()
            }
        })
    }

    override fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>) {
        val adapter = list.adapter as ImportAdapter
        for (packageName in docIds) {
            val resultCode = result.get(packageName)
            val status: Int
            if (resultCode == null) {
                status = ImportResourceProvider.STATUS_ERROR
            } else {
                status = if (resultCode == WatchAppList.RESULT_OK) ImportResourceProvider.STATUS_DONE else ImportResourceProvider.STATUS_ERROR
            }
            dataProvider.setPackageStatus(packageName, status)
            adapter.notifyPackageStatusChanged(packageName)
        }
    }

    override fun onImportFinish() {
        button2.setText(android.R.string.ok)
    }

    override fun onImportStart(docIds: List<String>) {
        val adapter = list.adapter as ImportAdapter
        dataProvider.isImportStarted = true
        for (packageName in docIds) {
            AppLog.d(packageName)
            dataProvider.setPackageStatus(packageName, ImportResourceProvider.STATUS_IMPORTING)
            adapter.notifyPackageStatusChanged(packageName)
        }
    }

}
