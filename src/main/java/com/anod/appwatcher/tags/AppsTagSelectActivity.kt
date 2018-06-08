package com.anod.appwatcher.tags

import android.app.Activity
import android.arch.lifecycle.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.os.BackgroundTask
import info.anodsplace.framework.view.Keyboard
import kotlinx.android.synthetic.main.activity_tag_select.*

/**
 * @author Alex Gavrishev
 * *
 * @date 19/04/2016.
 */

class AppsTagViewModel(application: android.app.Application): AndroidViewModel(application) {
    private val context = ApplicationContext(getApplication<AppWatcherApplication>())
    val titleFilter = MutableLiveData<String>()
    lateinit var tag: Tag

    fun loadTags(): LiveData<List<AppTag>> {
        return Application.provide(context).database.appTags().forTag(tag.id)
    }

    fun loadApps(): LiveData<List<AppListItem>> {
        val appsTable = Application.provide(context).database.apps()
        return AppListTable.Queries.loadAppList( Preferences.SORT_NAME_ASC,titleFilter.value ?: "", appsTable)
    }
}


class AppsTagSelectActivity : ToolbarActivity() {

    override val themeRes: Int
        get() =  Theme(this).themeDialog

    private var isAllSelected: Boolean = false
    private val tagAppsImport: TagAppsImport by lazy { TagAppsImport(viewModel.tag, this) }
    private val viewModel: AppsTagViewModel by lazy { ViewModelProviders.of(this).get(AppsTagViewModel::class.java) }
    private val adapter: TagAppsAdapter by lazy { TagAppsAdapter(this, tagAppsImport) }
    override val layoutResource: Int
        get() = R.layout.activity_tag_select

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.tag = intentExtras.getParcelable(EXTRA_TAG)!!

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(viewModel.tag.color))

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
            BackgroundTask(object : BackgroundTask.Worker<TagAppsImport, Boolean>(tagAppsImport) {
                override fun finished(result: Boolean) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                override fun run(param: TagAppsImport): Boolean {
                    return param.run()
                }
            }).execute()
        }

        viewModel.titleFilter.observe(this, Observer {
            loadApps()
        })

        viewModel.loadTags().observe(this, Observer {
            tagAppsImport.initSelected(it ?: emptyList())
            list.adapter = adapter
        })

        loadApps()
    }

    private fun loadApps() {
        viewModel.loadApps().observe(this, Observer {
            progress.visibility = View.GONE
            adapter.setData(it ?: emptyList())
        })
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
