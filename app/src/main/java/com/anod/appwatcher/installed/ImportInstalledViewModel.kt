// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import androidx.lifecycle.*
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.getInstalledPackages


class ImportInstalledViewModel(application: android.app.Application) : AndroidViewModel(application), Observer<ImportStatus> {

    val isEmpty: Boolean
        get() = importManager!!.isEmpty

    val installedPackages = appComponent.database.apps().observePackages()
            .map { watchingPackages ->
                val map = watchingPackages.associateBy { it.packageName }
                application.packageManager.getInstalledPackages().filter {
                    !map.containsKey(it.packageName)
                }
            }

    val progress: MutableLiveData<ImportStatus>
        get() = importManager!!.status

    private val appComponent: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

    private var importManager: ImportBulkManager? = ImportBulkManager(application, viewModelScope)
    internal val dataProvider: ImportResourceProvider by lazy { ImportResourceProvider(application, InstalledApps.MemoryCache(InstalledApps.PackageManager(application.packageManager))) }

    init {
        importManager!!.status.observeForever(this)
    }

    override fun onCleared() {
        importManager!!.status.removeObserver(this)
        importManager = null
    }

    fun selectAllPackages(allSelected: Boolean) {
        dataProvider.selectAllPackages(allSelected)
    }

    fun reset() {
        importManager!!.reset()
    }

    fun import(account: Account, token: String) {
        importManager!!.start(account, token)
    }

    fun addPackage(packageName: String, versionCode: Int): Boolean {
        if (dataProvider.isPackageSelected(packageName)) {
            importManager!!.addPackage(packageName, versionCode)
            return true
        }
        return false
    }

    override fun onChanged(status: ImportStatus) {
        when (status) {
            is ImportStarted -> {
                dataProvider.isImportStarted = true
                status.docIds.forEach { packageName ->
                    dataProvider.setPackageStatus(packageName, ImportResourceProvider.STATUS_IMPORTING)
                }
            }
            is ImportProgress -> {
                status.docIds.forEach { packageName ->
                    val resultCode = status.result.get(packageName)
                    val packageStatus = if (resultCode == null) {
                        ImportResourceProvider.STATUS_ERROR
                    } else {
                        if (resultCode == ImportTask.RESULT_OK) ImportResourceProvider.STATUS_DONE else ImportResourceProvider.STATUS_ERROR
                    }
                    dataProvider.setPackageStatus(packageName, packageStatus)
                }
            }
        }
    }
}