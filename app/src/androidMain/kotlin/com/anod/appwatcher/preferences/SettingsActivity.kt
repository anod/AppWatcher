package com.anod.appwatcher.preferences

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.backup.ExportBackupTask
import com.anod.appwatcher.backup.ImportBackupTask
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.MainDetailScreen
import info.anodsplace.framework.content.onCommonActivityAction
import com.anod.appwatcher.utils.prefs
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.jakewharton.processphoenix.ProcessPhoenix
import info.anodsplace.applog.AppLog
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.permissions.toRequestInput
import kotlinx.coroutines.launch

@SuppressLint("Registered")
open class SettingsActivity : BaseComposeActivity(), GDriveSignIn.Listener {

    private lateinit var gDriveErrorIntentRequest: ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionRequest: ActivityResultLauncher<AppPermissions.Request.Input>
    private val gDriveSignIn: GDriveSignIn by lazy { GDriveSignIn(this, this) }
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.handleEvent(SettingsViewEvent.SetWideLayout(hingeDevice.layout.value))

        gDriveErrorIntentRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
        notificationPermissionRequest = registerForActivityResult(AppPermissions.Request()) {
            val enabled = it[AppPermission.PostNotification.value] ?: false
            viewModel.handleEvent(SettingsViewEvent.NotificationPermissionResult(enabled))
            if (!enabled) {
                viewModel.handleEvent(SettingsViewEvent.ShowAppSettings)
            }
        }

        setContent {
            AppTheme(
                theme = viewModel.prefs.theme
            ) {
                val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

                if (screenState.wideLayout.isWideLayout) {
                    MainDetailScreen(
                        wideLayout = screenState.wideLayout,
                        main = {
                            SettingsScreen(
                                screenState = screenState,
                                onEvent = viewModel::handleEvent
                            )
                        },
                        detail = {
                            Surface {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(128.dp),
                                    )
                                }
                            }
                        }
                    )
                } else {
                    SettingsScreen(
                        screenState = screenState,
                        onEvent = viewModel::handleEvent
                    )
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewActions.collect { action ->
                    AppLog.d("Action collected $action")
                    handleUiAction(action)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                hingeDevice.layout.collect {
                    viewModel.handleEvent(SettingsViewEvent.SetWideLayout(it))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.handleEvent(SettingsViewEvent.CheckNotificationPermission)
    }

    private fun handleUiAction(action: SettingsViewAction) {
        when (action) {
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
            SettingsViewAction.RequestNotificationPermission -> notificationPermissionRequest.launch(AppPermission.PostNotification.toRequestInput())
            is SettingsViewAction.ActivityAction -> onCommonActivityAction(action = action.action)
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (viewModel.viewState.recreateWatchlistOnBack) {
            this.recreateWatchlist()
        }
    }

    @Deprecated("Deprecated in Java")
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