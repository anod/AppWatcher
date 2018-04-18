package info.anodsplace.framework.database

import android.content.ContentResolver
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle

/**
 * @author Alex Gavrishev
 * @date 27/04/2017.
 */
class NullCursor : Cursor {
    override fun getCount(): Int {
        return 0
    }

    override fun getPosition(): Int {
        return 0
    }

    override fun move(offset: Int): Boolean {
        return false
    }

    override fun moveToPosition(position: Int): Boolean {
        return false
    }

    override fun moveToFirst(): Boolean {
        return false
    }

    override fun moveToLast(): Boolean {
        return false
    }

    override fun moveToNext(): Boolean {
        return false
    }

    override fun moveToPrevious(): Boolean {
        return false
    }

    override fun isFirst(): Boolean {
        return false
    }

    override fun isLast(): Boolean {
        return false
    }

    override fun isBeforeFirst(): Boolean {
        return false
    }

    override fun isAfterLast(): Boolean {
        return false
    }

    override fun getColumnIndex(columnName: String): Int {
        return 0
    }

    @Throws(IllegalArgumentException::class)
    override fun getColumnIndexOrThrow(columnName: String): Int {
        return 0
    }

    override fun getColumnName(columnIndex: Int): String? {
        return null
    }

    override fun getColumnNames(): Array<String> {
        return emptyArray()
    }

    override fun getColumnCount(): Int {
        return 0
    }

    override fun getBlob(columnIndex: Int): ByteArray {
        return ByteArray(0)
    }

    override fun getString(columnIndex: Int): String? {
        return null
    }

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer) {

    }

    override fun getShort(columnIndex: Int): Short {
        return 0
    }

    override fun getInt(columnIndex: Int): Int {
        return 0
    }

    override fun getLong(columnIndex: Int): Long {
        return 0
    }

    override fun getFloat(columnIndex: Int): Float {
        return 0f
    }

    override fun getDouble(columnIndex: Int): Double {
        return 0.0
    }

    override fun getType(columnIndex: Int): Int {
        return 0
    }

    override fun isNull(columnIndex: Int): Boolean {
        return false
    }

    override fun deactivate() {

    }

    override fun requery(): Boolean {
        return false
    }

    override fun close() {

    }

    override fun isClosed(): Boolean {
        return false
    }

    override fun registerContentObserver(observer: ContentObserver) {

    }

    override fun unregisterContentObserver(observer: ContentObserver) {

    }

    override fun registerDataSetObserver(observer: DataSetObserver) {

    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {

    }

    override fun setNotificationUri(cr: ContentResolver, uri: Uri) {

    }

    override fun getNotificationUri(): Uri? {
        return null
    }

    override fun getWantsAllOnMoveCalls(): Boolean {
        return false
    }

    override fun setExtras(extras: Bundle) {

    }

    override fun getExtras(): Bundle? {
        return null
    }

    override fun respond(extras: Bundle): Bundle? {
        return null
    }
}
