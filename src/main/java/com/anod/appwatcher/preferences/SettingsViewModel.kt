package com.anod.appwatcher.preferences

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.anod.appwatcher.backup.ExportTask
import com.anod.appwatcher.backup.ImportTask
import info.anodsplace.framework.app.ApplicationContext

class SettingsViewModel(application: Application): AndroidViewModel(application) {

    fun import(srcUri: Uri): LiveData<Int> {
        return liveData {
            emit(-1)
            emit(ImportTask(ApplicationContext(getApplication())).execute(srcUri))
        }
    }

    fun export(dstUri: Uri): LiveData<Int> {
        return liveData {
            emit(-1)
            emit(ExportTask(ApplicationContext(getApplication())).execute(dstUri))
        }
    }

}