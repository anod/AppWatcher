package info.anodsplace.framework.app

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import info.anodsplace.framework.R

/**
 * @author Alex Gavrishev
 * @date 2015-06-20
 */
abstract class ToolbarActivity : AppCompatActivity(), CustomThemeActivity {

    override val themeRes = 0
    override val themeColors = CustomThemeColors.none

    @get:LayoutRes
    abstract val layoutResource: Int

    @get:IdRes
    open val detailsLayoutId = 0

    @get:IdRes
    open val hingLayoutId = 0

    private lateinit var duoDevice: HingeDevice

    private var hinge: View? = null
    private var details: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.nightMode)
        if (this.themeRes > 0) {
            this.setTheme(this.themeRes)
        }
        if (themeColors.available) {
            WindowCustomTheme.apply(themeColors, window, this)
        }
        duoDevice = HingeDevice.create(this)
        super.onCreate(savedInstanceState)
        setContentView(layoutResource)
        setupToolbar()
        updateWideLayout(resources.getBoolean(R.bool.wide_layout), duoDevice)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        duoDevice.attachedToWindow = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        duoDevice.attachedToWindow = false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateWideLayout(resources.getBoolean(R.bool.wide_layout), duoDevice)
    }

    protected open fun updateWideLayout(isWideLayout: Boolean, duoDevice: HingeDevice) {
        if (hingLayoutId != 0) {
            if (hinge == null) {
                hinge = findViewById(hingLayoutId)
            }
            hinge!!.isVisible = isWideLayout && duoDevice.hinge.width() > 0
            hinge!!.layoutParams.width = duoDevice.hinge.width()
        }
        if (detailsLayoutId != 0) {
            if (details == null) {
                details = findViewById(detailsLayoutId)
            }
            details!!.isVisible = isWideLayout
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            //set the Toolbar as ActionBar
            setSupportActionBar(toolbar)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
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
                    menu.getItem(i).icon?.let {
                        val icon = DrawableCompat.wrap(it)
                        DrawableCompat.setTint(icon, Color.WHITE)
                        menu.getItem(i).icon = icon
                    }
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
