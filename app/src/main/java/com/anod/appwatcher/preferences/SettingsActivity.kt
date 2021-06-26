package com.anod.appwatcher.preferences

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.*
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.Application
import com.anod.appwatcher.backup.ExportTask
import com.anod.appwatcher.backup.ImportTask
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.compose.UiAction
import com.anod.appwatcher.utils.Theme
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@SuppressLint("Registered")
open class SettingsActivity : ComponentActivity(), GDriveSignIn.Listener {

    private lateinit var gDriveErrorIntentRequest: ActivityResultLauncher<Intent>
    private val gDriveSignIn: GDriveSignIn by lazy { GDriveSignIn(this, this) }
    private val viewModel: SettingsViewModel by viewModels()
    private val appScope: CoroutineScope
        get() = Application.provide(this).appScope

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme(this).theme)
        super.onCreate(savedInstanceState)

        gDriveErrorIntentRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

        setContent {
            SettingsScreen(viewModel)
        }

        lifecycleScope.launchWhenResumed {
            viewModel.actions.collect { action ->
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
                    is UiAction.GDriveErrorIntent -> gDriveErrorIntentRequest.launch(action.intent)
                    UiAction.Recreate -> {
                        this@SettingsActivity.setResult(Activity.RESULT_OK, Intent().putExtra("recreateWatchlistOnBack", true))
                        this@SettingsActivity.recreate()
                        recreateWatchlist()
                    }
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
                ImportTask.showImportFinishToast(this@SettingsActivity, result)
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
                ExportTask.showFinishToast(this@SettingsActivity, result)
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
        viewModel.onGDriveLoginResult(true, 0)
    }
}