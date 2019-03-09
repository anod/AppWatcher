package info.anodsplace.framework.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import info.anodsplace.framework.R

import info.anodsplace.framework.AppLog

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
typealias FragmentCreator = () -> Fragment

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
            val fragmentTag = intent.getStringExtra(EXTRA_FRAGMENT)
            val f = createFragment(fragmentTag)
            if (f == null) {
                AppLog.e("Missing fragment for tag: $fragmentTag")
                finish()
                return
            }
            f.arguments = intent.getBundleExtra(EXTRA_ARGUMENTS)

            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_content, f, fragmentTag)
                    .commit()
        }
    }

    private fun createFragment(fragmentTag: String): Fragment? {
        val creator = fragments[fragmentTag]
        if (creator != null) {
            return creator()
        }
        return null
    }

    companion object {
        private const val EXTRA_FRAGMENT = "extra_fragment"
        private const val EXTRA_ARGUMENTS = "extra_arguments"
        private var fragments: MutableMap<String, FragmentCreator> = mutableMapOf()

        fun register(tag: String, creator: FragmentCreator) {
            fragments[tag] = creator
        }

        fun intent(tag: String, creator: FragmentCreator, themeRes: Int, themeColors: CustomThemeColors, args: Bundle, context: Context): Intent {
            register(tag, creator)
            return Intent(context, FragmentToolbarActivity::class.java).apply {
                putExtra(EXTRA_FRAGMENT, tag)
                putExtra(EXTRA_ARGUMENTS, args)
                putExtra("themeRes", themeRes)
                putExtra("themeColors", themeColors)
            }
        }
    }
}
