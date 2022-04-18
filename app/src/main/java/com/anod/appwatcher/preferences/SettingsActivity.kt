package com.anod.appwatcher.preferences

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.backup.ExportBackupTask
import com.anod.appwatcher.backup.ImportBackupTask
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.compose.UiAction
import com.anod.appwatcher.sync.SchedulesHistoryActivity
import com.anod.appwatcher.userLog.UserLogActivity
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.prefs
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.WindowCustomTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

@SuppressLint("Registered")
open class SettingsActivity : AppCompatActivity(), GDriveSignIn.Listener, KoinComponent {

    private lateinit var gDriveErrorIntentRequest: ActivityResultLauncher<Intent>
    private val gDriveSignIn: GDriveSignIn by lazy { GDriveSignIn(this, this) }
    private val viewModel: SettingsViewModel by viewModels()
    private val appScope: CoroutineScope
        get() = getKoin().get()

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = Theme(this, prefs)
        AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
        setTheme(theme.theme)
        if (theme.colors.available) {
            WindowCustomTheme.apply(theme.colors, window, this)
        }
        super.onCreate(savedInstanceState)

        gDriveErrorIntentRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

        setContent {
            SettingsScreen(viewModel)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actions.collect { action ->
                    AppLog.d("Action collected $action")
                    handleUiAction(action)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.reload.collect {
                AppLog.d("Reloaded")
            }
        }
    }

    private fun handleUiAction(action: UiAction) {
        when (action) {
            UiAction.OnBackNav -> finish()
            UiAction.OssLicenses -> startActivity(Intent(applicationContext, OssLicensesMenuActivity::class.java))
            is UiAction.Export -> {
                appScope.launch {
                    viewModel.export(action.uri).collect { result -> onExportResult(result) }
                }
            }
            is UiAction.Import -> {
                appScope.launch {
                    viewModel.import(action.uri).collect { result -> onImportResult(result) }
                }
            }
            UiAction.GDriveSignIn -> gDriveSignIn.signIn()
            UiAction.GDriveSignOut -> {
                lifecycleScope.launch { gDriveSignIn.signOut() }
            }
            is UiAction.GDriveErrorIntent -> gDriveErrorIntentRequest.launch(action.intent)
            UiAction.Recreate -> {
                this@SettingsActivity.setResult(RESULT_OK, Intent().putExtra("recreateWatchlistOnBack", true))
                this@SettingsActivity.recreate()
                recreateWatchlist()
            }
            UiAction.OpenRefreshHistory -> {
                startActivity(Intent(applicationContext, SchedulesHistoryActivity::class.java))
            }
            UiAction.OpenUserLog -> {
                startActivity(Intent(applicationContext, UserLogActivity::class.java))
            }
            UiAction.Rebirth -> {
                ProcessPhoenix.triggerRebirth(applicationContext, Intent(applicationContext, AppWatcherActivity::class.java))
            }
            is UiAction.ShowToast -> {
                if (action.resId == 0) {
                    Toast.makeText(this@SettingsActivity, action.text, action.length).show()
                } else {
                    Toast.makeText(this@SettingsActivity, action.resId, action.length).show()
                }
            }
        }
    }

    private fun onImportResult(result: Int) {
        when (result) {
            -1 -> {
                AppLog.d("Importing...")
                viewModel.isProgressVisible.value = true
            }
            else -> {
                AppLog.d("Import finished with code: $result")
                viewModel.isProgressVisible.value = false
                ImportBackupTask.showImportFinishToast(this@SettingsActivity, result)
            }
        }
    }

    private fun onExportResult(result: Int) {
        when (result) {
            -1 -> {
                AppLog.d("Exporting...")
                viewModel.isProgressVisible.value = true
            }
            else -> {
                AppLog.d("Export finished with code: $result")
                viewModel.isProgressVisible.value = false
                ExportBackupTask.showFinishToast(this@SettingsActivity, result)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (viewModel.recreateWatchlistOnBack) {
            this.recreateWatchlist()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        gDriveSignIn.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun recreateWatchlist() {
        val i = Intent(this@SettingsActivity, AppWatcherActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
    }

    override fun onGDriveLoginSuccess(googleSignInAccount: GoogleSignInAccount) {
        viewModel.onGDriveLoginResult(true, 0)
    }

    override fun onGDriveLoginError(errorCode: Int) {
        viewModel.onGDriveLoginResult(false, errorCode)
    }
}