package info.anodsplace.framework.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import info.anodsplace.framework.R

import info.anodsplace.framework.AppLog
import java.io.Serializable

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
open class FragmentFactory(val tag: String): Serializable {

    open fun create(): Fragment? {
        return null
    }
}

class FragmentToolbarActivity : ToolbarActivity() {

    override val themeRes: Int
        get() = intentExtras.getInt("themeRes", 0)
    override val themeColors: CustomThemeColors
        get() = intentExtras.getParcelable<CustomThemeColors?>("themeColors") ?: CustomThemeColors.none

    override val layoutResource: Int
        get() = R.layout.activity_fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val factory: FragmentFactory = intent.getSerializableExtra(EXTRA_FACTORY) as FragmentFactory
            val f = factory.create()
            if (f == null) {
                AppLog.e("Missing fragment for tag: ${factory.tag}")
                finish()
                return
            }
            f.arguments = intent.getBundleExtra(EXTRA_ARGUMENTS)

            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_content, f, factory.tag)
                    .commit()
        }
    }

    companion object {
        private const val EXTRA_FACTORY = "extra_factory"
        private const val EXTRA_ARGUMENTS = "extra_arguments"

        fun intent(factory: FragmentFactory, arguments: Bundle, themeRes: Int, themeColors: CustomThemeColors, context: Context): Intent {
            return Intent(context, FragmentToolbarActivity::class.java).apply {
                putExtra(EXTRA_FACTORY, factory)
                putExtra(EXTRA_ARGUMENTS, arguments)
                putExtra("themeRes", themeRes)
                putExtra("themeColors", themeColors)
            }
        }
    }
}
