package com.anod.appwatcher.tags;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.R;
import com.anod.appwatcher.content.TagsContentProviderClient;
import com.anod.appwatcher.content.TagsCursor;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.model.schema.TagsTable;
import com.anod.appwatcher.recyclerview.RecyclerViewCursorAdapter;
import com.anod.appwatcher.ui.ToolbarActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 10/03/2017.
 */

public class TagsEditorActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(android.R.id.list)
    RecyclerView mListView;
    @BindView(android.R.id.edit)
    EditText mAddTag;

    private TagAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags_editor);
        ButterKnife.bind(this);
        setupToolbar();

        mAdapter = new TagAdapter(this);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new TagsCursorLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapData(null);
    }

    static class TagsCursorLoader extends CursorLoader {
        private static final Uri CONTENT_URI = AppListContentProvider.APPS_CONTENT_URI;
        private static final String ORDER_DEFAULT = TagsContentProviderClient.DEFAULT_SORT_ORDER;

        public TagsCursorLoader(Context context) {
            super(context, CONTENT_URI, TagsTable.PROJECTION, null, null, ORDER_DEFAULT);
        }
    }

    static class TagAdapter extends RecyclerViewCursorAdapter<TagHolder> {
        TagAdapter(Context context) {
            super(context, R.layout.list_item_tag);
        }

        @Override
        protected TagHolder onCreateViewHolder(View itemView) {
            return new TagHolder(itemView);
        }

        @Override
        protected void onBindViewHolder(TagHolder holder, int position, Cursor cursor) {
            holder.bindView(position, ((TagsCursor)cursor).getTag());
        }
    }

    static class TagHolder extends RecyclerView.ViewHolder {
        EditText mTagName;

        TagHolder(View itemView) {
            super(itemView);
            mTagName = (EditText)itemView;
        }

        void bindView(int position, Tag tag) {
            mTagName.setTag(tag.id);
            mTagName.setText(tag.name);
            mTagName.setCompoundDrawables(new ColorDrawable(tag.color), null, new ColorDrawable(tag.color), null);
        }
    }
}
