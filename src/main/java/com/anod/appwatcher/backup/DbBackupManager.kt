package com.anod.appwatcher.backup

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.anod.appwatcher.content.DbContentProviderClient
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.json.MalformedJsonException
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Serialize and deserialize app info list into/from JSON file

 * @author alex
 */
class DbBackupManager(private val context: ApplicationContext) {

    constructor(context: Context): this(ApplicationContext(context))
    /**
     * List of files in the backup directory
     */
    val fileList: Array<File>?
        get() {
            val saveDir = defaultBackupDir
            if (!saveDir.isDirectory) {
                return null
            }
            val filter = FilenameFilter { _, filename -> filename.endsWith(FILE_EXT_DAT) }
            return saveDir.listFiles(filter)
        }

    internal fun doExport(destUri: Uri): Int {
        val outputStream: OutputStream
        try {
            outputStream = context.contentResolver.openOutputStream(destUri)
        } catch (e: FileNotFoundException) {
            return ERROR_FILE_WRITE
        }

        if (!writeDb(outputStream)) {
            return ERROR_FILE_WRITE
        }
        return RESULT_OK
    }

    internal fun writeDb(outputStream: OutputStream): Boolean {
        AppLog.d("Write into: " + outputStream.toString())
        val writer = DbJsonWriter()
        val client = DbContentProviderClient(context)
        try {
            synchronized(DbBackupManager.sDataLock) {
                val buf = BufferedWriter(OutputStreamWriter(outputStream))
                writer.write(buf, client)
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return false
        } finally {
            client.close()
        }
        return true
    }

    internal fun doImport(uri: Uri): Int {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            return ERROR_FILE_READ
        }

        if (inputStream == null) {
            return ERROR_FILE_READ
        }
        val reader = DbJsonReader()
        var container: DbJsonReader.Container? = null
        try {
            synchronized(DbBackupManager.sDataLock) {
                val buf = BufferedReader(InputStreamReader(inputStream))
                container = reader.read(buf)
            }
        } catch (e: MalformedJsonException) {
            AppLog.e(e)
            return ERROR_DESERIALIZE
        } catch (e: IOException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        }

        container?.let {
            val cr = DbContentProviderClient(context)
            if (it.apps.isNotEmpty()) {
                cr.discardAll()
                cr.addApps(it.apps)
                cr.addTags(it.tags)
                cr.addAppTags(it.appTags)
            }
            cr.close()
        }
        return RESULT_OK
    }

    companion object {
        private const val DIR_BACKUP = "/data/com.anod.appwatcher/backup"

        const val FILE_EXT_DAT = ".json"
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
        internal val sDataLock = arrayOfNulls<Any>(0)
        internal val DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss"

        /**
         * @return Full path to Backup dir
         */
        val defaultBackupDir: File
            get() {
                val externalPath = Environment.getExternalStorageDirectory()
                return File(externalPath.absolutePath + DIR_BACKUP)
            }

        fun generateFileName(): String {
            val sdf = SimpleDateFormat(DbBackupManager.DATE_FORMAT_FILENAME, Locale.US)
            return sdf.format(Date(System.currentTimeMillis())) + FILE_EXT_DAT
        }

        fun generateBackupFile(): File {
            return File(defaultBackupDir, generateFileName())
        }

        fun getBackupFile(filename: String): File {
            return File(defaultBackupDir, filename + FILE_EXT_DAT)
        }
    }
}
