package com.anod.appwatcher.backup

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.room.withTransaction
import com.anod.appwatcher.Application
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.json.MalformedJsonException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Serialize and deserialize app info list into/from JSON file

 * @author alex
 */
class DbBackupManager(private val context: ApplicationContext) {

    constructor(context: Context) : this(ApplicationContext(context))

    internal suspend fun doExport(destUri: Uri): Int = withContext(Dispatchers.IO) {
        val outputStream: OutputStream?
        try {
            outputStream = context.contentResolver.openOutputStream(destUri)
                    ?: return@withContext ERROR_FILE_WRITE
            if (!writeDb(outputStream)) {
                return@withContext ERROR_FILE_WRITE
            }
        } catch (e: FileNotFoundException) {
            return@withContext ERROR_FILE_WRITE
        }
        return@withContext RESULT_OK
    }

    private suspend fun writeDb(outputStream: OutputStream): Boolean {
        AppLog.d("Write into: $outputStream")
        val writer = DbJsonWriter()
        return sDataLock.withLock {
            return try {
                val buf = BufferedWriter(OutputStreamWriter(outputStream))
                writer.write(buf, Application.provide(context).database)
                true
            } catch (e: IOException) {
                AppLog.e(e)
                false
            }
        }
    }

    internal suspend fun doImport(uri: Uri): Int = withContext(Dispatchers.IO) {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            return@withContext ERROR_FILE_READ
        }

        if (inputStream == null) {
            return@withContext ERROR_FILE_READ
        }
        val reader = DbJsonReader()
        var container: DbJsonReader.Container?
        try {
            val buf = BufferedReader(InputStreamReader(inputStream))
            sDataLock.withLock {
                container = reader.read(buf)
            }
        } catch (e: MalformedJsonException) {
            AppLog.e(e)
            return@withContext ERROR_DESERIALIZE
        } catch (e: IOException) {
            AppLog.e(e)
            return@withContext ERROR_FILE_READ
        }

        val result = container ?: return@withContext ERROR_FILE_READ

        if (result.apps.isEmpty()) {
            return@withContext RESULT_OK
        }

        val db = Application.provide(context).database
        return@withContext db.withTransaction {
            db.apps().delete()
            db.tags().delete()
            db.appTags().delete()

            result.apps.forEach { app ->
                db.apps().insert(
                        app.appId, app.packageName, app.versionNumber, app.versionName, app.title,
                        app.creator, app.iconUrl, app.status, app.uploadDate,
                        app.priceText, app.priceCur, app.priceMicros,
                        app.detailsUrl, app.uploadTime, app.appType, app.updateTime
                )
            }

            result.tags.forEach { tag ->
                db.tags().insert(tag.name, tag.color)
            }

            result.appTags.forEach { appTag ->
                db.appTags().insert(appTag.appId, appTag.tagId)
            }
            return@withTransaction RESULT_OK
        }
    }

    companion object {
        private const val DIR_BACKUP = "/data/com.anod.appwatcher/backup"

        private const val FILE_EXT_DAT = ".json"
        const val RESULT_OK = 0
        const val ERROR_STORAGE_NOT_AVAILABLE = 1
        const val ERROR_FILE_NOT_EXIST = 2
        const val ERROR_FILE_READ = 3
        const val ERROR_FILE_WRITE = 4
        const val ERROR_DESERIALIZE = 5

        /**
         * We serialize access to our persistent data through a global static
         * object. This ensures that in the unlikely event of the our backup/restore
         * agent running to perform a backup while our UI is updating the file, the
         * agent will not accidentally read partially-written data.
         *
         *
         *
         *
         * Curious but true: a zero-length array is slightly lighter-weight than
         * merely allocating an Object, and can still be synchronized on.
         */
        internal val sDataLock = Mutex()
        private const val DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss"

        /**
         * @return Full path to Backup dir
         */
        val defaultBackupDir: File
            get() {
                val externalPath = Environment.getExternalStorageDirectory()
                return File(externalPath.absolutePath + DIR_BACKUP)
            }

        fun generateFileName(): String {
            val sdf = SimpleDateFormat(DATE_FORMAT_FILENAME, Locale.US)
            return sdf.format(Date(System.currentTimeMillis())) + FILE_EXT_DAT
        }
    }
}
