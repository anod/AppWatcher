package com.anod.appwatcher.preferences

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.backup.ExportBackupTask
import com.anod.appwatcher.backup.ImportBackupTask
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.compose.BaseComposeActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.jakewharton.processphoenix.ProcessPhoenix
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.launch

@SuppressLint("Registered")
open class SettingsActivity : BaseComposeActivity(), GDriveSignIn.Listener {

    private lateinit var gDriveErrorIntentRequest: ActivityResultLauncher<Intent>
    private val gDriveSignIn: GDriveSignIn by lazy { GDriveSignIn(this, this) }
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gDriveErrorIntentRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

        setContent {
            val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

            SettingsScreen(
                    screenState = screenState,
                    onEvent = { viewModel.handleEvent(it) }
            )
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewActions.collect { action ->
                    AppLog.d("Action collected $action")
                    handleUiAction(action)
                }
            }
        }
    }

    private fun handleUiAction(action: SettingsViewAction) {
        when (action) {
            SettingsViewAction.OnBackNav -> finish()
            is SettingsViewAction.ExportResult -> onExportResult(action.result)
            is SettingsViewAction.ImportResult -> onImportResult(action.result)
            SettingsViewAction.GDriveSignIn -> gDriveSignIn.signIn()
            SettingsViewAction.GDriveSignOut -> {
                lifecycleScope.launch { gDriveSignIn.signOut() }
            }
            is SettingsViewAction.GDriveErrorIntent -> gDriveErrorIntentRequest.launch(action.intent)
            SettingsViewAction.Recreate -> {
                this@SettingsActivity.setResult(RESULT_OK, Intent().putExtra("recreateWatchlistOnBack", true))
                this@SettingsActivity.recreate()
                recreateWatchlist()
            }
            SettingsViewAction.Rebirth -> {
                ProcessPhoenix.triggerRebirth(applicationContext, Intent(applicationContext, AppWatcherActivity::class.java))
            }
            is SettingsViewAction.ShowToast -> {
                if (action.resId == 0) {
                    Toast.makeText(this@SettingsActivity, action.text, action.length).show()
                } else {
                    Toast.makeText(this@SettingsActivity, action.resId, action.length).show()
                }
            }
            is SettingsViewAction.StartActivity -> startActivity(action.intent)
        }
    }

    private fun onImportResult(result: Int) {
        when (result) {
            -1 -> {
                AppLog.d("Importing...")
            }
            else -> {
                AppLog.d("Import finished with code: $result")
                ImportBackupTask.showImportFinishToast(this@SettingsActivity, result)
            }
        }
    }

    private fun onExportResult(result: Int) {
        when (result) {
            -1 -> {
                AppLog.d("Exporting...")
            }
            else -> {
                AppLog.d("Export finished with code: $result")
                ExportBackupTask.showFinishToast(this@SettingsActivity, result)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (viewModel.viewState.recreateWatchlistOnBack) {
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
        viewModel.handleEvent(SettingsViewEvent.GDriveLoginResult(true, 0))
    }

    override fun onGDriveLoginError(errorCode: Int) {
        viewModel.handleEvent(SettingsViewEvent.GDriveLoginResult(false, errorCode))
    }
}