package com.anod.appwatcher.installed

import android.content.Context
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import androidx.collection.SimpleArrayMap
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenAsync
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.content.WatchAppList
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.content.*
import kotlinx.android.synthetic.main.activity_import_installed.*
import java.util.*

/**
 * @author Alex Gavrishev
 * *
 * @date 19/04/2016.
 */
class ImportInstalledActivity : ToolbarActivity(), LoaderManager.LoaderCallbacks<List<InstalledPackage>>, ImportBulkManager.Listener {

    override val themeRes: Int
        get() = Theme(this).themeDialog

    private var allSelected: Boolean = false
    private lateinit var dataProvider: ImportResourceProvider
    private lateinit var importManager: ImportBulkManager

    override val layoutResource: Int
        get() = R.layout.activity_import_installed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        importManager = ImportBulkManager(this, this)
        dataProvider = ImportResourceProvider(this, InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)))

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = ImportAdapter(this, packageManager, dataProvider)
        list.itemAnimator = ImportItemAnimator()

        findViewById<View>(android.R.id.button3).setOnClickListener {
            val importAdapter = list.adapter as ImportAdapter
            allSelected = !allSelected
            dataProvider.selectAllPackages(allSelected)
            importAdapter.notifyDataSetChanged()
        }

        findViewById<View>(android.R.id.button2).setOnClickListener {
            if (dataProvider.isImportStarted) {
                importManager.stop()
            }
            finish()
        }

        findViewById<View>(android.R.id.button1).setOnClickListener {
            findViewById<View>(android.R.id.button3).visibility = View.GONE
            findViewById<View>(android.R.id.button1).visibility = View.GONE
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
                finish()
            } else {
                startImport()

            }
        }

        supportLoaderManager.initLoader(0, null, this).forceLoad()
    }

    private fun startImport() {
        val account = Application.provide(this).prefs.account
        if (account == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            return
        }

        AuthTokenAsync(this).request(this, account, object : AuthTokenAsync.Callback {
            override fun onToken(token: String) {
                importManager.start(account, token)
            }

            override fun onError(errorMessage: String) {
                if (Application.provide(this@ImportInstalledActivity).networkConnection.isNetworkAvailable) {
                    Toast.makeText(this@ImportInstalledActivity, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@ImportInstalledActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        })
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<InstalledPackage>> {
        return LocalPackageLoader(this)
    }

    override fun onLoadFinished(loader: Loader<List<InstalledPackage>>, data: List<InstalledPackage>) {
        progress.visibility = View.GONE
        val downloadedAdapter = list.adapter as ImportAdapter
        downloadedAdapter.installedPackages = data
    }

    override fun onLoaderReset(loader: Loader<List<InstalledPackage>>) {
        val downloadedAdapter = list.adapter as ImportAdapter
        downloadedAdapter.installedPackages = emptyList()
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
        findViewById<Button>(android.R.id.button2).setText(android.R.string.ok)
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

    private class LocalPackageLoader internal constructor(context: Context) : AsyncTaskLoader<List<InstalledPackage>>(context) {
        override fun loadInBackground(): List<InstalledPackage>? {
            val cr = DbContentProviderClient(context)
            val watchingPackages = cr.queryPackagesMap(false)
            cr.close()

            val pm = context.packageManager
            val list = pm.getInstalledPackages().filter { !watchingPackages.containsKey(it.packageName) }
            Collections.sort(list, AppTitleComparator(1))

            return list
        }
    }

}
