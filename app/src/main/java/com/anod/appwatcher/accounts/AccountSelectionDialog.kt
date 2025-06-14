package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.preferences.Preferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

sealed interface AccountSelectionResult {
    class Success(val account: Account) : AccountSelectionResult
    data object Canceled : AccountSelectionResult
    class Error(val errorMessage: String) : AccountSelectionResult
}

/**
 * @author alex
 * *
 * @date 9/17/13
 */
class AccountSelectionDialog(
    private val activity: ComponentActivity,
    private val preferences: Preferences
) {

    private val chooseAccount = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onActivityResult(result.resultCode, result.data)
    }

    val accountSelected = MutableSharedFlow<AccountSelectionResult>()

    fun show() {
        val intent = AccountManager.newChooseAccountIntent(
            preferences.account?.toAndroidAccount(),
            null,
            arrayOf(AuthTokenBlocking.ACCOUNT_TYPE),
            null,
            null,
            null,
            null)
        chooseAccount.launch(intent)
    }

    private fun onActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED) {
            activity.lifecycleScope.launch {
                accountSelected.emit(AccountSelectionResult.Canceled)
            }
            return
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            val name = data.extras?.getString(AccountManager.KEY_ACCOUNT_NAME, "") ?: ""
            val type = data.extras?.getString(AccountManager.KEY_ACCOUNT_TYPE, "") ?: ""
            if (name.isNotBlank() && type.isNotBlank()) {
                val account = Account(name, type)
                activity.lifecycleScope.launch {
                    accountSelected.emit(AccountSelectionResult.Success(account))
                }
                return
            }
        }
        val errorMessage = data?.extras?.getString(AccountManager.KEY_ERROR_MESSAGE, null)
        activity.lifecycleScope.launch {
            accountSelected.emit(AccountSelectionResult.Error(errorMessage ?: "Cannot retrieve account"))
        }
    }
}