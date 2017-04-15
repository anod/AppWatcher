package com.anod.appwatcher.tags;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.R;
import com.anod.appwatcher.content.AppListCursor;
import com.anod.appwatcher.model.AppListCursorLoader;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.model.schema.AppTagsTable;
import com.anod.appwatcher.ui.ToolbarActivity;
import com.anod.appwatcher.utils.BackgroundTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author algavris
 * @date 19/04/2016.
 */
public class AppsTagSelectActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_TAG = "extra_tag";

    @BindView(android.R.id.list)
    RecyclerView mList;
    @BindView(android.R.id.progress)
    ProgressBar mProgress;

    private boolean mAllSelected;
    private Tag mTag;
    private TagAppsManager mManager;

    public static Intent createIntent(Tag tag, Context context) {
        Intent intent = new Intent(context, AppsTagSelectActivity.class);
        intent.putExtra(EXTRA_TAG, tag);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_installed);
        ButterKnife.bind(this);
        setupToolbar();

        mTag = getIntentExtras().getParcelable(EXTRA_TAG);
        mManager = new TagAppsManager(mTag, this);

        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(new TagAppsCursorAdapter(this, mManager));
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(android.R.id.button3)
    public void onAllButtonClick() {
        TagAppsCursorAdapter importAdapter = (TagAppsCursorAdapter) mList.getAdapter();
        mAllSelected = !mAllSelected;
        importAdapter.selectAllApps(mAllSelected);
    }

    @OnClick(android.R.id.button2)
    public void onCancelButtonClick() {
        finish();
    }

    @OnClick(android.R.id.button1)
    public void onImportButtonClick() {

        BackgroundTask.execute(new BackgroundTask.Worker<TagAppsManager, Boolean>(mManager, this) {
            @Override
            public Boolean run(TagAppsManager param, Context context) {
                return param.runImport();
            }

            @Override
            public void finished(Boolean result, Context context) {
                finish();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 0)
        {
            return new TagAppsCursorLoader(this, mTag);
        }
        return new AppListCursorLoader(this, "", AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC", null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        TagAppsCursorAdapter adapter = (TagAppsCursorAdapter) mList.getAdapter();
        if (loader.getId() == 0) {
            mManager.initSelected(data);
            getSupportLoaderManager().initLoader(1, null, this).forceLoad();
            return;
        }

        mProgress.setVisibility(View.GONE);
        adapter.swapData((AppListCursor) data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == 1) {
            TagAppsCursorAdapter adapter = (TagAppsCursorAdapter) mList.getAdapter();
            adapter.swapData(null);
        }
    }

    static class TagAppsCursorLoader extends CursorLoader {

        TagAppsCursorLoader(Context context,@NonNull Tag tag) {
            super(context,  getContentUri(tag), AppTagsTable.PROJECTION, null, null, null);
        }

        private static Uri getContentUri(Tag tag) {
            return AppListContentProvider.APPS_TAG_CONTENT_URI
                            .buildUpon()
                            .appendPath(String.valueOf(tag.id)).build();
        }

    }
}
