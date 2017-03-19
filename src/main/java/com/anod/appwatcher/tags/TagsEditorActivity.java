package com.anod.appwatcher.tags;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.R;
import com.anod.appwatcher.content.DbContentProviderClient;
import com.anod.appwatcher.content.TagsContentProviderClient;
import com.anod.appwatcher.content.TagsCursor;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.TagsTable;
import com.anod.appwatcher.recyclerview.RecyclerViewCursorAdapter;
import com.anod.appwatcher.ui.ToolbarActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 10/03/2017.
 */

public class TagsEditorActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<Cursor>, TextView.OnEditorActionListener {

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

        mAddTag.setOnEditorActionListener(this);

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new TagsCursorLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapData(new TagsCursor(data));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapData(null);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String tag = v.getText().toString().trim();
        if (TextUtils.isEmpty(tag))
        {
            return true;
        }

        addNewTag(tag);

        return true;
    }

    private void addNewTag(@NonNull String tag) {
        DbContentProviderClient cr = new DbContentProviderClient(this);
        if (cr.queryTagByName(tag) == null)
        {
            cr.createTag(new Tag(tag));
        }
        cr.close();
    }

    static class TagsCursorLoader extends CursorLoader {
        private static final Uri CONTENT_URI = AppListContentProvider.TAGS_CONTENT_URI;
        private static final String ORDER_DEFAULT = TagsContentProviderClient.DEFAULT_SORT_ORDER;

        TagsCursorLoader(Context context) {
            super(context, CONTENT_URI, TagsTable.PROJECTION, null, null, ORDER_DEFAULT);
        }
    }

    static class TagAdapter extends RecyclerViewCursorAdapter<TagHolder, TagsCursor> {
        TagAdapter(Context context) {
            super(context, R.layout.list_item_tag);
        }

        @Override
        protected TagHolder onCreateViewHolder(View itemView) {
            return new TagHolder(itemView);
        }

        @Override
        protected void onBindViewHolder(TagHolder holder, int position, TagsCursor cursor) {
            holder.bindView(position, cursor.getTag());
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
