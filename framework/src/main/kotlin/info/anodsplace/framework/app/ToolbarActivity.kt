package info.anodsplace.framework.app

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import info.anodsplace.framework.R

/**
 * @author Alex Gavrishev
 * @date 2015-06-20
 */
abstract class ToolbarActivity : AppCompatActivity(), CustomThemeActivity {

    override val themeRes = 0
    @get:LayoutRes abstract val layoutResource: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.nightMode)
        if (this.themeRes > 0) {
            this.setTheme(this.themeRes)
        }
        super.onCreate(savedInstanceState)
        setContentView(layoutResource)
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        //set the Toolbar as ActionBar
        setSupportActionBar(toolbar)

        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //if (!getSupportFragmentManager().popBackStackImmediate()) {
                //    NavUtils.navigateUpFromSameTask(this);
                //}
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected val intentExtras: Bundle
        get() {
            if (intent == null || intent.extras == null) {
                return Bundle()
            }
            return intent.extras
        }

}
