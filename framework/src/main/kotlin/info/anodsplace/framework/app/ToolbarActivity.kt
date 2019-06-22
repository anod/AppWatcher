package info.anodsplace.framework.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import info.anodsplace.framework.R

/**
 * @author Alex Gavrishev
 * @date 2015-06-20
 */
abstract class ToolbarActivity : AppCompatActivity(), CustomThemeActivity {

    override val themeRes = 0
    override val themeColors = CustomThemeColors.none
    @get:LayoutRes abstract val layoutResource: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.nightMode)
        if (this.themeRes > 0) {
            this.setTheme(this.themeRes)
        }
        if (themeColors.available) {
            WindowCustomTheme.apply(themeColors, window, this)
        }
        super.onCreate(savedInstanceState)
        setContentView(layoutResource)
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        //set the Toolbar as ActionBar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        if (!themeColors.statusBarColor.isLight) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                for (i in 0 until menu.size()) {
                    menu.getItem(i).iconTintList = ColorStateList.valueOf(Color.WHITE)
                    menu.getItem(i).iconTintMode = PorterDuff.Mode.SRC_IN
                }
            } else {
                for (i in 0 until menu.size()) {
                    val icon = DrawableCompat.wrap( menu.getItem(i).icon)
                    DrawableCompat.setTint(icon, Color.WHITE)
                    menu.getItem(i).icon = icon
                }
            }
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected val intentExtras: Bundle
        get() = intent?.extras ?: Bundle()

}
