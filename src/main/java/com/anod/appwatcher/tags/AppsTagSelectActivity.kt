package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import android.widget.ProgressBar

import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.AppListCursorLoader
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.framework.ToolbarActivity
import com.anod.appwatcher.framework.BackgroundTask
import com.anod.appwatcher.framework.Keyboard
import kotterknife.bindView


/**
 * @author algavris
 * *
 * @date 19/04/2016.
 */
class AppsTagSelectActivity : ToolbarActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    val listView: RecyclerView by bindView(android.R.id.list)
    val progressBar: ProgressBar by bindView(android.R.id.progress)

    private var isAllSelected: Boolean = false
    private lateinit var tag: Tag
    private lateinit var tagAppsImport: TagAppsImport
    private var titleFilter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_select)
        setupToolbar()

        tag = intentExtras.getParcelable<Tag>(EXTRA_TAG)!!
        titleFilter = savedInstanceState?.getString("title_filter") ?: ""
        tagAppsImport = TagAppsImport(tag, this)

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = TagAppsCursorAdapter(this, tagAppsImport)

        findViewById<View>(android.R.id.button3).setOnClickListener {
            val importAdapter = listView.adapter as TagAppsCursorAdapter
            isAllSelected = !isAllSelected
            importAdapter.selectAllApps(isAllSelected)
        }

        findViewById<View>(android.R.id.button2).setOnClickListener {
            finish()
        }

        findViewById<View>(android.R.id.button1).setOnClickListener {
            BackgroundTask(object : BackgroundTask.Worker<TagAppsImport, Boolean>(tagAppsImport) {
                override fun finished(result: Boolean) {
                    finish()
                }

                override fun run(param: TagAppsImport): Boolean {
                    return param.run()
                }
            }).execute()
        }

        supportLoaderManager.initLoader(0, null, this).forceLoad()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putString("title_filter", titleFilter)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbox, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchItem.expandActionView()

        searchView.setQuery(titleFilter, true)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Keyboard.hide(searchView, this@AppsTagSelectActivity)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterList(query)
                return true
            }
        })

        Keyboard.hide(searchView, this)
        return true
    }

    private fun filterList(query: String) {
        titleFilter = query
        supportLoaderManager.restartLoader(1, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == 0) {
            return TagAppsCursorLoader(this, tag)
        }
        return AppListCursorLoader(this, titleFilter, AppListTable.Columns.title + " COLLATE LOCALIZED ASC", null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        val adapter = listView.adapter as TagAppsCursorAdapter
        if (loader.id == 0) {
            tagAppsImport.initSelected(data)
            supportLoaderManager.initLoader(1, null, this).forceLoad()
            return
        }

        progressBar.visibility = View.GONE
        adapter.swapData(data as AppListCursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == 1) {
            val adapter = listView.adapter as TagAppsCursorAdapter
            adapter.swapData(null)
        }
    }

    class TagAppsCursorLoader(context: Context, tag: Tag)
        : CursorLoader(context, TagAppsCursorLoader.getContentUri(tag), AppTagsTable.projection, null, null, null) {

        companion object {
            private fun getContentUri(tag: Tag): Uri {
                return DbContentProvider.appsTagUri
                        .buildUpon()
                        .appendPath(tag.id.toString()).build()
            }
        }
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
