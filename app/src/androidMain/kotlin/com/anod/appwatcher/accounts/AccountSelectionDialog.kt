package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.preferences.PersistedAccount
import com.anod.appwatcher.preferences.toAndroidAccount
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

sealed interface AccountSelectionResult {
    class Success(val account: Account) : AccountSelectionResult
    object Canceled : AccountSelectionResult
    class Error(val errorMessage: String) : AccountSelectionResult
}

/**
 * @author alex
 * *
 * @date 9/17/13
 */
class AccountSelectionDialog(
        private val activity: AppCompatActivity,
        private val preferences: Preferences
) {

    private val chooseAccount = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onActivityResult(result.resultCode, result.data)
    }

    var account: Account?
        get() = preferences.account?.toAndroidAccount()
        set(value) {
            preferences.account = value?.let { PersistedAccount(it) }
        }

    val accountSelected = MutableSharedFlow<AccountSelectionResult>()

    fun show() {
        val intent = AccountManager.newChooseAccountIntent(
                account,
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
                val account = PersistedAccount(name, type)
                this.preferences.account = account
                activity.lifecycleScope.launch {
                    accountSelected.emit(AccountSelectionResult.Success(account.toAndroidAccount()))
                }
                return
            }
        }
        if (this.preferences.account == null) {
            val errorMessage = data?.extras?.getString(AccountManager.KEY_ERROR_MESSAGE, "") ?: ""
            activity.lifecycleScope.launch {
                accountSelected.emit(AccountSelectionResult.Error(errorMessage))
            }
        }
    }
}