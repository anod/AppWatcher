package com.anod.appwatcher.installed

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v4.util.SimpleArrayMap
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenAsync
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.framework.content.getInstalledPackagesCompat
import kotlinx.android.synthetic.main.activity_import_installed.*
import java.util.*


/**
 * @author algavris
 * *
 * @date 19/04/2016.
 */
class ImportInstalledActivity : ToolbarActivity(), LoaderManager.LoaderCallbacks<List<String>>, ImportBulkManager.Listener {

    override val themeRes: Int
        get() = Theme(this).themeDialog

    private var allSelected: Boolean = false
    private lateinit var dataProvider: ImportDataProvider
    private lateinit var importManager: ImportBulkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_installed)
        setupToolbar()

        importManager = ImportBulkManager(this, this)
        dataProvider = ImportDataProvider(this, InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)))

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
                val packageName = adapter.getItem(idx)
                if (dataProvider.isPackageSelected(packageName)) {
                    importManager.addPackage(packageName)
                    adapter.storePackageIndex(packageName, idx)
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
        val account = App.provide(this).prefs.account
        if (account == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            return
        }

        AuthTokenAsync(this).request(this, account, object : AuthTokenAsync.Callback {
            override fun onToken(token: String) {
                importManager.start(account, token)
            }

            override fun onError(errorMessage: String) {
                if (App.with(this@ImportInstalledActivity).isNetworkAvailable) {
                    Toast.makeText(this@ImportInstalledActivity, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@ImportInstalledActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        })
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<String>> {
        return LocalPackageLoader(this)
    }

    override fun onLoadFinished(loader: Loader<List<String>>, data: List<String>) {
        progress.visibility = View.GONE
        val downloadedAdapter = list.adapter as ImportAdapter
        downloadedAdapter.clear()
        downloadedAdapter.addAll(data)
    }

    override fun onLoaderReset(loader: Loader<List<String>>) {
        val downloadedAdapter = list.adapter as ImportAdapter
        downloadedAdapter.clear()
    }

    override fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>) {
        val adapter = list.adapter as ImportAdapter
        for (packageName in docIds) {
            val resultCode = result.get(packageName)
            val status: Int
            if (resultCode == null) {
                status = ImportDataProvider.STATUS_ERROR
            } else {
                status = if (resultCode == WatchAppList.RESULT_OK) ImportDataProvider.STATUS_DONE else ImportDataProvider.STATUS_ERROR
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
            dataProvider.setPackageStatus(packageName, ImportDataProvider.STATUS_IMPORTING)
            adapter.notifyPackageStatusChanged(packageName)
        }
    }

    private class LocalPackageLoader internal constructor(context: Context) : AsyncTaskLoader<List<String>>(context) {
        override fun loadInBackground(): List<String> {
            val cr = DbContentProviderClient(context)
            val watchingPackages = cr.queryPackagesMap(false)
            cr.close()

            val pm = context.packageManager
            val list = pm.getInstalledPackagesCompat().filter { !watchingPackages.containsKey(it) }
            Collections.sort(list) { lPackageName, rPackageName -> pm.getAppTitle(lPackageName).compareTo(pm.getAppTitle(rPackageName)) }

            return list
        }
    }

}
