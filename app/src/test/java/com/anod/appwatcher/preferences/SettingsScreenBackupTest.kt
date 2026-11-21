package com.anod.appwatcher.preferences

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.performClick
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.DbBackupManager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@Config(application = Application::class, sdk = [35], qualifiers = "w411dp-h891dp")
class SettingsScreenBackupTest : SettingsScreenTestBase() {
    private val chosenFile = Uri.parse("content://com.android.providers.downloads.documents/document/appwatcher.json")

    private fun launchedPicker(): Intent {
        val launched = shadowOf(compose.activity).peekNextStartedActivityForResult()
        assertNotNull("No document picker was opened", launched)
        return launched.intent
    }

    private fun closePicker(resultCode: Int, data: Intent?) {
        shadowOf(compose.activity).receiveResult(launchedPicker(), resultCode, data)
        compose.waitForIdle()
    }

    private fun withoutDocumentProvider() {
        shadowOf(RuntimeEnvironment.getApplication()).checkActivities(true)
    }

    @Test
    fun exportOpensTheDocumentCreatorWithATimestampedBackupName() {
        render()

        row("export").performClick()

        val picker = launchedPicker()
        val title = picker.getStringExtra(Intent.EXTRA_TITLE).orEmpty()
        assertEquals(Intent.ACTION_CREATE_DOCUMENT, picker.action)
        assertEquals("application/json", picker.type)
        assertTrue(title, Regex("appwatcher-\\d{8}_\\d{6}\\.json").matches(title))
        assertEquals(Uri.parse(DbBackupManager.defaultBackupDir.absolutePath), picker.data)
        assertTrue(events.isEmpty())
    }

    @Test
    fun exportWritesToTheDocumentTheUserCreated() {
        render()
        row("export").performClick()

        closePicker(Activity.RESULT_OK, Intent().setData(chosenFile))

        val export = onlyEvent<SettingsViewEvent.Export>()
        assertEquals(chosenFile, export.uri)
    }

    @Test
    fun exportDoesNothingWhenTheUserBacksOutOfTheCreator() {
        render()
        row("export").performClick()

        closePicker(Activity.RESULT_CANCELED, null)

        assertTrue(events.toString(), events.isEmpty())
    }

    @Test
    fun importOpensTheDocumentPickerForBackupFiles() {
        render()

        row("import").performClick()

        val picker = launchedPicker()
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, picker.action)
        assertEquals("*/*", picker.type)
        assertArrayEquals(
            arrayOf("application/json", "text/plain", "*/*"),
            picker.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
        )
        assertTrue(events.isEmpty())
    }

    @Test
    fun importReadsTheDocumentTheUserPicked() {
        render()
        row("import").performClick()

        closePicker(Activity.RESULT_OK, Intent().setData(chosenFile))

        val import = onlyEvent<SettingsViewEvent.Import>()
        assertEquals(chosenFile, import.uri)
    }

    @Test
    fun importDoesNothingWhenTheUserBacksOutOfThePicker() {
        render()
        row("import").performClick()

        closePicker(Activity.RESULT_CANCELED, null)

        assertTrue(events.toString(), events.isEmpty())
    }

    @Test
    fun exportExplainsWhenTheDeviceHasNoDocumentProvider() {
        render()
        withoutDocumentProvider()

        row("export").performClick()
        compose.waitForIdle()

        assertEquals(context.getString(R.string.no_document_provider), ShadowToast.getTextOfLatestToast())
        assertTrue(events.isEmpty())
    }

    @Test
    fun importExplainsWhenTheDeviceHasNoDocumentProvider() {
        render()
        withoutDocumentProvider()

        row("import").performClick()
        compose.waitForIdle()

        assertEquals(context.getString(R.string.no_document_provider), ShadowToast.getTextOfLatestToast())
        assertTrue(events.isEmpty())
    }
}