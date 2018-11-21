package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import com.anod.appwatcher.R
import kotlinx.android.synthetic.main.activity_choose_account.*

/**
 * @author alex
 * *
 * @date 8/24/13
 */
class AccountSelectionDialogActivity : AppCompatActivity() {

    companion object {
        fun intent(selected: Account?, context: Context): Intent {
            val intent = Intent(context, AccountSelectionDialogActivity::class.java)
            intent.putExtra("account", selected)
            return intent
        }
    }

    val accountManager: AccountManager by lazy {
        AccountManager.get(this@AccountSelectionDialogActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_account)
        setTitle(R.string.choose_an_account)

        val accounts = accountManager.getAccountsByType(AuthTokenBlocking.ACCOUNT_TYPE)
        if (accounts.isEmpty())
        {
            val data = Intent()
            data.putExtra(AccountManager.KEY_ERROR_MESSAGE, getString(R.string.no_registered_google_accounts))
            setResult(Activity.RESULT_CANCELED, data)
            finish()
            return
        }

        findViewById<Button>(android.R.id.button2).setOnClickListener {

            val account = (list.adapter as AccountsAdapter).selectedAccount
            if (account == null) {
                setResult(Activity.RESULT_CANCELED)
            } else {
                val data = Intent()
                data.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name)
                data.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                setResult(Activity.RESULT_OK, data)
            }
            finish()
        }

        findViewById<Button>(android.R.id.button1).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        val adapter = AccountsAdapter(this, accounts)
        adapter.selectedAccount = intent.extras.get("account") as? Account
        list.adapter = adapter
    }

    class AccountsAdapter(context: Context,accounts: Array<Account>): ArrayAdapter<Account>(context, R.layout.list_item_radio, accounts) {

        var selectedAccount: Account? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent) as RadioButton

            val account = getItem(position)
            view.tag = position
            view.text = account.name
            view.isChecked = account.name == selectedAccount?.name

            view.setOnClickListener {
                val radio = it as RadioButton
                if (radio.isChecked) {
                    selectedAccount = getItem(it.tag as Int)
                }
            }

            return view
        }
    }

}
