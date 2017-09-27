package com.anod.appwatcher.ui

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.MenuItemAnimation

abstract class SettingsActionBarActivity : ToolbarActivity(), AdapterView.OnItemClickListener {
    lateinit private var mListView: ListView
    lateinit private var mRefreshAnim: MenuItemAnimation
    lateinit private var mPreferenceAdapter: PreferenceAdapter

    open class Preference(@StringRes val title: Int, @LayoutRes val layout: Int)
    class Category(@StringRes title: Int) : Preference(title, R.layout.preference_category)

    open class Item(@StringRes title: Int, @StringRes var summaryRes: Int, internal val action: Int)
        : Preference(title, R.layout.preference_holo) {
        var summary: String? = null
        var widget: Int = 0
        var enabled = true
    }

    class CheckboxItem(@StringRes title: Int, @StringRes summaryRes: Int, action: Int)
        : Item(title, summaryRes, action) {
        var checked = false

        init {
            this.widget = R.layout.preference_widget_checkbox
        }

        constructor(title: Int, summaryRes: Int, action: Int, checked: Boolean) : this(title, summaryRes, action) {
            this.checked = checked
        }

        fun switchState() {
            this.checked = !this.checked
        }
    }

    internal class PreferenceAdapter(activity: SettingsActionBarActivity, objects: List<Preference>) : ArrayAdapter<Preference>(activity, 0, objects) {
        private val mInflater: LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getItemViewType(position: Int): Int {
            val pref = getItem(position)
            if (pref is CheckboxItem) {
                return 0
            }
            if (pref is Category) {
                return 1
            }
            return 2
        }

        override fun getViewTypeCount(): Int {
            return 3
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val pref = getItem(position)

            val view: View
            if (convertView == null) {
                view = mInflater.inflate(pref!!.layout, parent, false)
            } else {
                view = convertView
            }

            val title = view.findViewById<View>(android.R.id.title) as TextView
            title.setText(pref!!.title)

            if (pref is Item) {
                val item = pref
                val icon = view.findViewById<View>(android.R.id.icon)
                if (icon != null) {
                    icon.visibility = View.GONE
                }

                val summary = view.findViewById<View>(android.R.id.summary) as TextView
                if (item.summaryRes > 0) {
                    summary.setText(item.summaryRes)
                } else if (!TextUtils.isEmpty(item.summary)) {
                    summary.text = item.summary
                }

                val widgetFrame = view.findViewById<View>(android.R.id.widget_frame) as ViewGroup
                if (item.widget > 0) {

                    if (item is CheckboxItem) {
                        var checkBox: CheckBox? = widgetFrame.findViewById<View>(android.R.id.checkbox) as? CheckBox
                        if (checkBox == null) {
                            mInflater.inflate(item.widget, widgetFrame)
                            checkBox = widgetFrame.findViewById<View>(android.R.id.checkbox) as CheckBox
                        }
                        checkBox.isChecked = item.checked
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupToolbar()

        mRefreshAnim = MenuItemAnimation(this, R.anim.rotate)
        mRefreshAnim.isInvisibleMode = true
        init()

        val preferences = initPreferenceItems()

        mPreferenceAdapter = PreferenceAdapter(this, preferences)
        mListView = findViewById<View>(android.R.id.list) as ListView
        mListView.emptyView = findViewById(android.R.id.empty)
        mListView.adapter = mPreferenceAdapter
        mListView.onItemClickListener = this
    }

    protected abstract fun init()
    protected abstract fun initPreferenceItems(): List<Preference>
    protected abstract fun onPreferenceItemClick(action: Int, pref: Item)

    protected fun setProgressVisibility(visible: Boolean) {
        if (visible) {
            mRefreshAnim.start()
        } else {
            mRefreshAnim.stop()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        menuInflater.inflate(R.menu.settings, menu)
        mRefreshAnim.menuItem = menu.findItem(R.id.menu_act_refresh)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val pref = mListView.getItemAtPosition(position) as Preference
        if (pref is Item) {
            val action = pref.action
            if (pref is CheckboxItem) {
                pref.switchState()
            }
            onPreferenceItemClick(action, pref)
        }
    }

    protected fun notifyDataSetChanged() {
        mPreferenceAdapter.notifyDataSetChanged()
    }

}
