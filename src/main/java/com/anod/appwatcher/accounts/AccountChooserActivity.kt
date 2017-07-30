package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.bindView

import com.anod.appwatcher.R

/**
 * @author alex
 * *
 * @date 8/24/13
 */
class AccountChooserActivity : AppCompatActivity() {

    companion object {
        fun intent(selected: Account?, context: Context): Intent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return android.accounts.AccountManager.newChooseAccountIntent(
                        selected,
                        null,
                        arrayOf(AuthTokenProvider.ACCOUNT_TYPE),
                        null,
                        null,
                        null,
                        null)
            }

            val intent = Intent(context, AccountChooserActivity::class.java)
            intent.putExtra("account", selected)
            return intent
        }
    }

    val accountManager: AccountManager by lazy {
        AccountManager.get(this@AccountChooserActivity)
    }

    val listView: ListView by bindView(android.R.id.list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_account)

        val accounts = accountManager.getAccountsByType(AuthTokenProvider.ACCOUNT_TYPE)
        if (accounts.isEmpty())
        {
            setResult(Activity.RESULT_CANCELED)
            Toast.makeText(this, R.string.no_registered_google_accounts, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.description).setText(R.string.choose_an_account)
        findViewById<Button>(android.R.id.button1).setOnClickListener {

            val account = (listView.adapter as AccountsAdapter).selectedAccount
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

        findViewById<Button>(android.R.id.button2).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        val adapter = AccountsAdapter(this, accounts)
        adapter.selectedAccount = intent.extras.get("account") as Account
        listView.adapter = adapter
    }

    class AccountsAdapter(context: Context,accounts: Array<Account>): ArrayAdapter<Account>(context, R.layout.list_item_radio, accounts) {

        var selectedAccount: Account? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent) as RadioButton

            val account = getItem(position)
            view.isSelected = account.name == selectedAccount?.name
            view.tag = position
            view.setOnClickListener { if (it.isSelected) {
                selectedAccount = getItem(it.tag as Int)
            } }

            return view
        }
    }

}
