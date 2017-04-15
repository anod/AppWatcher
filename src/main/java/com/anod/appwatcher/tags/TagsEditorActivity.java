package com.anod.appwatcher.tags;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

public class TagsEditorActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    @BindView(android.R.id.list)
    RecyclerView mListView;

    private TagAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags_editor);
        ButterKnife.bind(this);
        setupToolbar();

        mAdapter = new TagAdapter(this, this);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(mAdapter);
        mListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tags_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_tag)
        {
            EditTagDialog dialog = EditTagDialog.newInstance(null);
            dialog.show(getSupportFragmentManager(), "edit-tag-dialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void saveTag(Tag tag) {
        DbContentProviderClient cr = new DbContentProviderClient(this);
        if (tag.id == -1) {
            cr.createTag(tag);
        } else {
            cr.saveTag(tag);
        }
        cr.close();
    }

    public void deleteTag(Tag tag) {
        DbContentProviderClient cr = new DbContentProviderClient(this);
        cr.deleteTag(tag);
        cr.close();
    }

    @Override
    public void onClick(View v) {
        Tag tag = (Tag)v.getTag();
        EditTagDialog dialog = EditTagDialog.newInstance(tag);
        dialog.show(getSupportFragmentManager(), "edit-tag-dialog");
    }

    private static class TagsCursorLoader extends CursorLoader {
        private static final Uri CONTENT_URI = AppListContentProvider.TAGS_CONTENT_URI;
        private static final String ORDER_DEFAULT = TagsContentProviderClient.DEFAULT_SORT_ORDER;

        TagsCursorLoader(Context context) {
            super(context, CONTENT_URI, TagsTable.PROJECTION, null, null, ORDER_DEFAULT);
        }
    }

    private static class TagAdapter extends RecyclerViewCursorAdapter<TagHolder, TagsCursor> {
        private final View.OnClickListener mListener;

        TagAdapter(Context context, View.OnClickListener listener) {
            super(context, R.layout.list_item_tag);
            mListener = listener;
        }

        @Override
        protected TagHolder onCreateViewHolder(View itemView) {
            return new TagHolder(itemView, mListener);
        }

        @Override
        protected void onBindViewHolder(TagHolder holder, int position, TagsCursor cursor) {
            holder.bindView(position, cursor.getTag());
        }
    }

    static class TagHolder extends RecyclerView.ViewHolder {
        private final View.OnClickListener listener;
        @BindView(android.R.id.text1) TextView mTagName;
        @BindView(android.R.id.icon) ImageView mTagColor;


        TagHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        void bindView(int position, Tag tag) {
            mTagName.setText(tag.name);
            mTagName.setTag(tag);
            Drawable d = mTagColor.getDrawable().mutate();
            DrawableCompat.setTint(d, tag.color);
            mTagColor.setImageDrawable(d);
            mTagName.setOnClickListener(this.listener);
        }
   }
}
