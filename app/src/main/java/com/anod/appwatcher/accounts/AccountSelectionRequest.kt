package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import info.anodsplace.framework.content.ScreenCommonAction
import info.anodsplace.framework.content.ShowDialogData

sealed interface AccountSelectionResult {
    class Success(val account: Account) : AccountSelectionResult
    data object Canceled : AccountSelectionResult
    class Error(val errorMessage: String) : AccountSelectionResult
}

data class AccountSelectionDialogData(
    val currentAccount: Account?
) : ShowDialogData

fun showAccountSelectionAction(
    currentAccount: Account?
): ScreenCommonAction = ScreenCommonAction.ShowDialog(
    AccountSelectionDialogData(currentAccount)
)

class AccountSelectionRequest : ActivityResultContract<Account?, AccountSelectionResult>() {

    override fun createIntent(context: Context, input: Account?): Intent {
        val intent = AccountManager.newChooseAccountIntent(
            input,
            null,
            arrayOf(AuthTokenBlocking.ACCOUNT_TYPE),
            null,
            null,
            null,
            null
        )
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AccountSelectionResult {
        if (resultCode == Activity.RESULT_CANCELED) {
            return AccountSelectionResult.Canceled
        }

        if (resultCode == Activity.RESULT_OK && intent != null) {
            val name = intent.extras?.getString(AccountManager.KEY_ACCOUNT_NAME, "") ?: ""
            val type = intent.extras?.getString(AccountManager.KEY_ACCOUNT_TYPE, "") ?: ""
            if (name.isNotBlank() && type.isNotBlank()) {
                val account = Account(name, type)
                return AccountSelectionResult.Success(account)
            }
        }
        val errorMessage = intent?.extras?.getString(AccountManager.KEY_ERROR_MESSAGE, null)
        return AccountSelectionResult.Error(errorMessage ?: "Cannot retrieve account")
    }
}