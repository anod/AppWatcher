package info.anodsplace.framework.content

import android.content.ContentProviderClient
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import info.anodsplace.framework.database.NullCursor

/**
 * @author Alex Gavrishev
 * @date 14/09/2017
 */

fun ContentProviderClient.update(uri: Uri, values: ContentValues): Int {
    return update(uri, values, null, null)
}

fun ContentProviderClient.delete(uri: Uri) {
    delete(uri, null, null)
}

fun ContentProviderClient.query(uri: Uri): Cursor {
    return query(uri, null, null, null, null) ?: return NullCursor()
}

fun ContentProviderClient.query(uri: Uri, sortOrder: String): Cursor {
    return query(uri, null, null, null, sortOrder) ?: return NullCursor()
}

fun ContentProviderClient.query(uri: Uri, projection: Array<String>): Cursor {
    return query(uri, projection, null, null, null) ?: return NullCursor()
}
