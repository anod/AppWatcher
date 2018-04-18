package info.anodsplace.framework.app

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import info.anodsplace.framework.view.MenuItemAnimation
import info.anodsplace.framework.R

abstract class SettingsActionBarActivity : ToolbarActivity(), AdapterView.OnItemClickListener {
    private val listView: ListView by lazy { findViewById<View>(android.R.id.list) as ListView }
    private val refreshAnim: MenuItemAnimation by lazy { MenuItemAnimation(this, R.anim.rotate) }
    private val preferenceAdapter: PreferenceAdapter by lazy { PreferenceAdapter(this, createPreferenceItems()) }

    var viewTypeCount = 4

    open class Preference(@StringRes val title: Int, @LayoutRes val layout: Int, val viewType: Int)
    class Category(@StringRes title: Int) : Preference(title, R.layout.preference_category, 0)

    open class Item(@StringRes title: Int, @StringRes var summaryRes: Int, internal val action: Int, val widget: Int, viewType: Int)
        : Preference(title, R.layout.preference_holo, viewType) {

        constructor(@StringRes title: Int, @StringRes summaryRes: Int, action: Int, viewType: Int)
                : this(title, summaryRes, action, 0, viewType)

        var summary = ""
        var enabled = true
    }

    class TextItem(@StringRes title: Int, @StringRes summaryRes: Int, action: Int)
        : Item(title, summaryRes, action, 0, 1)

    open class ToggleItem(@StringRes title: Int, @StringRes summaryRes: Int, action: Int, widget: Int, var checked: Boolean, viewType: Int)
        : Item(title, summaryRes, action, widget, viewType) {

        fun toggle() {
            this.checked = !this.checked
        }
    }

    class CheckboxItem(@StringRes title: Int, @StringRes summaryRes: Int, action: Int, checked: Boolean = false)
        : ToggleItem(title, summaryRes, action, R.layout.preference_widget_checkbox, checked, 2)

    class SwitchItem(@StringRes title: Int, @StringRes summaryRes: Int, action: Int, checked: Boolean = false)
        : ToggleItem(title, summaryRes, action, R.layout.preference_widget_switch, checked, 3)

    internal class PreferenceAdapter(activity: SettingsActionBarActivity, objects: List<Preference>) : ArrayAdapter<Preference>(activity, 0, objects) {
        private val viewTypeCount = activity.viewTypeCount

        override fun getItemViewType(position: Int): Int {
            return getItem(position).viewType
        }

        override fun getViewTypeCount(): Int {
            return viewTypeCount
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val pref = getItem(position)

            val view: View = convertView ?: LayoutInflater.from(parent.context).inflate(pref!!.layout, parent, false)

            val title = view.findViewById<View>(android.R.id.title) as TextView
            title.setText(pref!!.title)

            if (pref is Item) {
                val icon = view.findViewById<View>(android.R.id.icon)
                if (icon != null) {
                    icon.visibility = View.GONE
                }

                val summary = view.findViewById<View>(android.R.id.summary) as TextView
                if (pref.summaryRes > 0) {
                    summary.setText(pref.summaryRes)
                } else {
                    summary.text = pref.summary
                }

                val widgetFrame = view.findViewById<View>(android.R.id.widget_frame) as ViewGroup
                if (pref.widget > 0) {
                    var widgetView: View? = widgetFrame.findViewById(android.R.id.checkbox)
                    if (widgetView == null) {
                        LayoutInflater.from(parent.context).inflate(pref.widget, widgetFrame)
                        widgetView = widgetFrame.findViewById(android.R.id.checkbox)
                    }
                    if (pref is ToggleItem) {
                        val checkBox: CompoundButton = widgetView as CompoundButton
                        checkBox.isChecked = pref.checked
                    }
                } else {
                    widgetFrame.visibility = View.GONE
                }
            }

            return view
        }

        override fun isEnabled(position: Int): Boolean {
            val pref = getItem(position)
            if (pref is Category) {
                return false
            }
            if (pref is Item) {
                return pref.enabled
            }
            return true
        }
    }

    override val layoutResource: Int
        get() = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshAnim.isInvisibleMode = true

        listView.emptyView = findViewById(android.R.id.empty)
        listView.adapter = preferenceAdapter
        listView.onItemClickListener = this
    }

    protected abstract fun createPreferenceItems(): List<Preference>
    protected abstract fun onPreferenceItemClick(action: Int, pref: Item)

    var isProgressVisible: Boolean = false
        set(value) {
            if (value) {
                refreshAnim.start()
            } else {
                refreshAnim.stop()
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        menuInflater.inflate(R.menu.settings, menu)
        refreshAnim.menuItem = menu.findItem(R.id.menu_act_refresh)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val pref = listView.getItemAtPosition(position) as Preference
        if (pref is Item) {
            val action = pref.action
            if (pref is ToggleItem) {
                pref.toggle()
            }
            onPreferenceItemClick(action, pref)
        }
    }

    fun notifyDataSetChanged() {
        preferenceAdapter.notifyDataSetChanged()
    }
}
