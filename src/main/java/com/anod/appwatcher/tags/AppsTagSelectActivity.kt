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
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.AppListCursorLoader
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.ui.ToolbarActivity
import com.anod.appwatcher.utils.BackgroundTask
import com.anod.appwatcher.utils.Keyboard


/**
 * @author algavris
 * *
 * @date 19/04/2016.
 */
class AppsTagSelectActivity : ToolbarActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(android.R.id.list)
    lateinit var mList: RecyclerView
    @BindView(android.R.id.progress)
    lateinit var mProgress: ProgressBar

    private var mAllSelected: Boolean = false
    private lateinit var mTag: Tag
    private lateinit var mManager: TagAppsManager
    private var mTitleFilter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_select)
        ButterKnife.bind(this)
        setupToolbar()

        mTag = intentExtras.getParcelable<Tag>(EXTRA_TAG)!!
        mTitleFilter = savedInstanceState?.getString("title_filter") ?: ""
        mManager = TagAppsManager(mTag, this)

        mList.layoutManager = LinearLayoutManager(this)
        mList.adapter = TagAppsCursorAdapter(this, mManager)
        supportLoaderManager.initLoader(0, null, this).forceLoad()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putString("title_filter", mTitleFilter)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchbox, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setIconifiedByDefault(false)
        MenuItemCompat.expandActionView(searchItem)

        searchView.setQuery(mTitleFilter, true)
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
        mTitleFilter = query
        supportLoaderManager.restartLoader(1, null, this)
    }


    @OnClick(android.R.id.button3)
    fun onAllButtonClick() {
        val importAdapter = mList.adapter as TagAppsCursorAdapter
        mAllSelected = !mAllSelected
        importAdapter.selectAllApps(mAllSelected)
    }

    @OnClick(android.R.id.button2)
    fun onCancelButtonClick() {
        finish()
    }

    @OnClick(android.R.id.button1)
    fun onImportButtonClick() {

        BackgroundTask.execute(object : BackgroundTask.Worker<TagAppsManager, Boolean>(mManager, this) {
            override fun finished(result: Boolean, context: Context) {
                finish()
            }

            override fun run(param: TagAppsManager, context: Context): Boolean {
                return param.runImport()
            }
        })
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == 0) {
            return TagAppsCursorLoader(this, mTag)
        }
        return AppListCursorLoader(this, mTitleFilter, AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC", null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        val adapter = mList.adapter as TagAppsCursorAdapter
        if (loader.id == 0) {
            mManager.initSelected(data)
            supportLoaderManager.initLoader(1, null, this).forceLoad()
            return
        }

        mProgress.visibility = View.GONE
        adapter.swapData(data as AppListCursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == 1) {
            val adapter = mList.adapter as TagAppsCursorAdapter
            adapter.swapData(null)
        }
    }

    class TagAppsCursorLoader(context: Context, tag: Tag)
        : CursorLoader(context, TagAppsCursorLoader.getContentUri(tag), AppTagsTable.PROJECTION, null, null, null) {

        companion object {
            private fun getContentUri(tag: Tag): Uri {
                return DbContentProvider.APPS_TAG_CONTENT_URI
                        .buildUpon()
                        .appendPath(tag.id.toString()).build()
            }
        }
    }

    companion object {
        val EXTRA_TAG = "extra_tag"

        fun createIntent(tag: Tag, context: Context): Intent {
            val intent = Intent(context, AppsTagSelectActivity::class.java)
            intent.putExtra(EXTRA_TAG, tag)
            return intent
        }
    }
}
