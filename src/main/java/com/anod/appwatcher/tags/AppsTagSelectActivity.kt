package com.anod.appwatcher.tags

import android.app.Activity
import androidx.lifecycle.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.View
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.os.BackgroundTask
import info.anodsplace.framework.view.Keyboard
import kotlinx.android.synthetic.main.activity_tag_select.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author Alex Gavrishev
 * *
 * @date 19/04/2016.
 */

class AppsTagViewModel(application: android.app.Application): AndroidViewModel(application) {
    private val context = ApplicationContext(getApplication<AppWatcherApplication>())
    val titleFilter = MutableLiveData("")
    var tag = MutableLiveData<Tag>()

    private val database: AppsDatabase
        get() = Application.provide(context).database

    internal lateinit var tagAppsImport: TagAppsImport

    val apps = titleFilter.switchMap { titleFilter ->
        val appsTable = database.apps()
        AppListTable.Queries.loadAppList( Preferences.SORT_NAME_ASC,titleFilter, appsTable)
    }

    val tags = tag.switchMap { database.appTags().forTag(it.id) }

    fun import() {
        viewModelScope.launch {
            tagAppsImport.run()
        }
    }
}


class AppsTagSelectActivity : ToolbarActivity() {

    override val themeRes: Int
        get() =  Theme(this).themeDialogNoActionBar
    override val themeColors: CustomThemeColors
        get() = Theme(this).colors

    private var isAllSelected: Boolean = false
    private val viewModel: AppsTagViewModel by lazy { ViewModelProviders.of(this).get(AppsTagViewModel::class.java) }
    private val adapter: TagAppsAdapter by lazy { TagAppsAdapter(this, viewModel.tagAppsImport) }
    override val layoutResource: Int
        get() = R.layout.activity_tag_select

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tag: Tag = intentExtras.getParcelable(EXTRA_TAG)!!
        viewModel.tagAppsImport = TagAppsImport(tag, ApplicationContext(this))
        viewModel.tag.value = tag

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(tag.color))

        list.layoutManager = LinearLayoutManager(this)

        findViewById<View>(android.R.id.button3).setOnClickListener {
            val importAdapter = list.adapter as TagAppsAdapter
            isAllSelected = !isAllSelected
            importAdapter.selectAllApps(isAllSelected)
        }

        findViewById<View>(android.R.id.button2).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        findViewById<View>(android.R.id.button1).setOnClickListener {
            viewModel.import()
            GlobalScope.launch(Dispatchers.Main) {

                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        viewModel.tags.observe(this) {
            viewModel.tagAppsImport.initSelected(it)
            list.adapter = adapter
        }

        viewModel.apps.observe(this) {
            progress.visibility = View.GONE
            adapter.setData(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbox, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchItem.expandActionView()

        searchView.setQuery(viewModel.titleFilter.value, true)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Keyboard.hide(searchView, this@AppsTagSelectActivity)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                viewModel.titleFilter.value = query
                return true
            }
        })

        Keyboard.hide(searchView, this)
        return true
    }

    companion object {
        const val EXTRA_TAG = "extra_tag"

        fun createIntent(tag: Tag, context: Context): Intent {
            val intent = Intent(context, AppsTagSelectActivity::class.java)
            intent.putExtra(EXTRA_TAG, tag)
            return intent
        }
    }
}
