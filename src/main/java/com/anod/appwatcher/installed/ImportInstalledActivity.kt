package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v4.util.SimpleArrayMap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.anod.appwatcher.App
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountChooser
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.fragments.AccountChooserFragment
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.ui.ToolbarActivity
import com.anod.appwatcher.utils.InstalledAppsProvider
import com.anod.appwatcher.utils.PackageManagerUtils
import info.anodsplace.android.log.AppLog
import java.util.*


/**
 * @author algavris
 * *
 * @date 19/04/2016.
 */
class ImportInstalledActivity : ToolbarActivity(), LoaderManager.LoaderCallbacks<List<String>>, AccountChooser.OnAccountSelectionListener, ImportBulkManager.Listener {
    @BindView(android.R.id.list)
    lateinit var mList: RecyclerView
    @BindView(android.R.id.progress)
    lateinit var mProgress: ProgressBar

    private var mAllSelected: Boolean = false
    private lateinit var mDataProvider: ImportDataProvider
    private lateinit var mImportManager: ImportBulkManager
    private var mAccountChooser: AccountChooser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_installed)
        ButterKnife.bind(this)
        setupToolbar()

        mImportManager = ImportBulkManager(this, this)
        mDataProvider = ImportDataProvider(this, InstalledAppsProvider.MemoryCache(InstalledAppsProvider.PackageManager(packageManager)))

        mList.layoutManager = LinearLayoutManager(this)
        mList.adapter = ImportAdapter(this, packageManager, mDataProvider)
        mList.itemAnimator = ImportItemAnimator()
        supportLoaderManager.initLoader(0, null, this).forceLoad()
    }

    override fun onResume() {
        super.onResume()

        mAccountChooser = AccountChooser(this, App.provide(this).prefs, this)
        mAccountChooser!!.init()
    }

    @OnClick(android.R.id.button3)
    fun onAllButtonClick() {
        val importAdapter = mList.adapter as ImportAdapter
        mAllSelected = !mAllSelected
        mDataProvider.selectAllPackages(mAllSelected)
        importAdapter.notifyDataSetChanged()
    }

    @OnClick(android.R.id.button2)
    fun onCancelButtonClick() {
        if (mDataProvider.isImportStarted) {
            mImportManager.stop()
        }
        finish()
    }

    @OnClick(android.R.id.button1)
    fun onImportButtonClick() {
        ButterKnife.findById<View>(this, android.R.id.button3).visibility = View.GONE
        ButterKnife.findById<View>(this, android.R.id.button1).visibility = View.GONE
        val adapter = mList.adapter as ImportAdapter

        mImportManager.init()
        adapter.clearPackageIndex()
        for (idx in 0..adapter.itemCount - 1) {
            val packageName = adapter.getItem(idx)
            if (mDataProvider.isPackageSelected(packageName)) {
                mImportManager.addPackage(packageName)
                adapter.storePackageIndex(packageName, idx)
            }
        }
        if (mImportManager.isEmpty) {
            finish()
            return
        }
        mImportManager.start()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<String>> {
        return LocalPackageLoader(this)
    }

    override fun onLoadFinished(loader: Loader<List<String>>, data: List<String>) {
        mProgress.visibility = View.GONE
        val downloadedAdapter = mList.adapter as ImportAdapter
        downloadedAdapter.clear()
        downloadedAdapter.addAll(data)
    }

    override fun onLoaderReset(loader: Loader<List<String>>) {
        val downloadedAdapter = mList.adapter as ImportAdapter
        downloadedAdapter.clear()
    }

    override fun onAccountSelected(account: Account, authSubToken: String?) {
        if (authSubToken == null) {
            if (App.with(this).isNetworkAvailable) {
                Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            }
            finish()
            return
        }
        mImportManager.setAccount(account, authSubToken)
    }

    override fun onAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        finish()
    }

    override val accountSelectionListener: AccountChooserFragment.OnAccountSelectionListener
        get() = mAccountChooser!!

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mAccountChooser!!.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>) {
        val adapter = mList.adapter as ImportAdapter
        for (packageName in docIds) {
            val resultCode = result.get(packageName)
            val status: Int
            if (resultCode == null) {
                status = ImportDataProvider.STATUS_ERROR
            } else {
                status = if (resultCode == WatchAppList.RESULT_OK) ImportDataProvider.STATUS_DONE else ImportDataProvider.STATUS_ERROR
            }
            mDataProvider.setPackageStatus(packageName, status)
            adapter.notifyPackageStatusChanged(packageName)
        }
    }

    override fun onImportFinish() {
        (ButterKnife.findById<View>(this, android.R.id.button2) as Button).setText(android.R.string.ok)
    }

    override fun onImportStart(docIds: List<String>) {
        val adapter = mList.adapter as ImportAdapter
        mDataProvider.isImportStarted = true
        for (packageName in docIds) {
            AppLog.d(packageName)
            mDataProvider.setPackageStatus(packageName, ImportDataProvider.STATUS_IMPORTING)
            adapter.notifyPackageStatusChanged(packageName)
        }
    }


    private class LocalPackageLoader internal constructor(context: Context) : AsyncTaskLoader<List<String>>(context) {
        override fun loadInBackground(): List<String> {
            val cr = DbContentProviderClient(context)
            val watchingPackages = cr.queryPackagesMap(false)
            cr.close()

            val pm = context.packageManager
            val list = PackageManagerUtils.getDownloadedApps(watchingPackages, pm)
            Collections.sort(list) { lPackageName, rPackageName -> PackageManagerUtils.getAppTitle(lPackageName, pm).compareTo(PackageManagerUtils.getAppTitle(rPackageName, pm)) }

            return list
        }
    }

}
