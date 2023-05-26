// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.backup.gdrive

import android.content.Intent
import androidx.core.util.Pair
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors


sealed class GDriveSpace(val name: String) {
    object Drive : GDriveSpace("drive")
    object AppData : GDriveSpace("appDataFolder")
    object Photos : GDriveSpace("photos")
}

class DriveService(private val service: Drive) {
    constructor(credential: HttpRequestInitializer, appName: String)
            : this(createService(credential, appName))

    companion object {
        private fun createService(credential: HttpRequestInitializer, appName: String): Drive {
            return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName(appName)
                    .build()
        }

        fun extractUserRecoverableException(e: Exception): UserRecoverableAuthException? {
            return if (e is UserRecoverableAuthIOException) {
                e.cause
            } else {
                e as? UserRecoverableAuthException
            }
        }
    }

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    suspend fun createFile(name: String, mimeType: String, space: GDriveSpace): String = withContext(dispatcher) {
        val metadata = File()
//                .setSpaces(listOf(space.name))
                .setParents(listOf(space.name))
                .setMimeType(mimeType)
                .setName(name)
        val googleFile = service.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
        return@withContext googleFile.id
    }

    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * contents.
     */
    suspend fun readFile(fileId: String, outputStream: OutputStream) = withContext(dispatcher) {
        service.files()[fileId].executeMediaAsInputStream().use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                val writer = outputStream.bufferedWriter()
                val buffer = CharArray(8192)
                var n: Int
                while (reader.read(buffer).also { n = it } != -1) {
                    writer.write(buffer, 0, n)
                }
                writer.flush()
            }
        }
    }

    /**
     * Updates the file identified by `fileId` with the given `name` and `content`.
     */
    suspend fun saveFile(fileId: String, contentType: String, content: InputStream): Any? = withContext(dispatcher) {
        service.files()
                .update(fileId, File(), InputStreamContent(contentType, content))
                .execute()
    }

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
     * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    suspend fun queryAppDataFiles(orderBy: String, mimeType: String, name: String, space: GDriveSpace): FileList = withContext(dispatcher) {
        val query = "mimeType = '$mimeType' and name = '$name'"
        return@withContext service
                .files()
                .list()
                .setOrderBy(orderBy)
                .setQ(query)
                .setSpaces(space.name)
                .execute()
    }

    /**
     * Returns an [Intent] for opening the Storage Access Framework file picker.
     */
    fun createFilePickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        return intent
    }

    /**
     * Opens the file at the `uri` returned by a Storage Access Framework [Intent]
     * created by [.createFilePickerIntent] using the given `contentResolver`.
     */
//    fun openFileUsingStorageAccessFramework(contentResolver: ContentResolver, uri: Uri?): Task<Pair<String?, String?>> {
//        return Tasks.call(executor, Callable {
//            // Retrieve the document's display name from its metadata.
//            var name: String? = null
//            contentResolver.query(uri!!, null, null, null, null).use { cursor ->
//                name = if (cursor != null && cursor.moveToFirst()) {
//                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                    cursor.getString(nameIndex)
//                } else {
//                    throw IOException("Empty cursor returned for file.")
//                }
//            }
//            // Read the document's contents as a String.
//            var content: String? = null
//            contentResolver.openInputStream(uri).use { `is` ->
//                BufferedReader(InputStreamReader(`is`)).use { reader ->
//                    val stringBuilder = StringBuilder()
//                    var line: String?
//                    while (reader.readLine().also { line = it } != null) {
//                        stringBuilder.append(line)
//                    }
//                    content = stringBuilder.toString()
//                }
//            }
//            Pair.create<String?, String?>(name, content)
//        })
//    }

}