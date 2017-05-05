package com.anod.appwatcher.tags;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.TagsTable;
import com.anod.appwatcher.recyclerview.RecyclerViewCursorAdapter;
import com.anod.appwatcher.ui.ToolbarActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 10/03/2017.
 */

public class TagsListActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    public static final String EXTRA_APP = "app";

    @BindView(android.R.id.list)
    RecyclerView mListView;

    private TagAdapter mAdapter;
    private AppInfo mAppInfo;

    public static Intent intent(Context context, @NonNull AppInfo app) {
        Intent intent = new Intent(context, TagsListActivity.class);
        intent.putExtra(EXTRA_APP, app);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags_editor);
        ButterKnife.bind(this);
        setupToolbar();

        mAppInfo = getIntentExtras().getParcelable(EXTRA_APP);
        if (mAppInfo != null) {
            setTitle(getString(R.string.tag_app, mAppInfo.title));
        }
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
        if (item.getItemId() == R.id.add_tag) {
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
        TagHolder holder = (TagHolder) v.getTag();
        if (mAppInfo == null) {
            EditTagDialog dialog = EditTagDialog.newInstance(holder.tag);
            dialog.show(getSupportFragmentManager(), "edit-tag-dialog");
        } else {
            DbContentProviderClient client = new DbContentProviderClient(this);
            List<Integer> tags = client.queryAppTags(mAppInfo.getRowId());
            if (tags.contains(holder.tag.id)) {
                if (client.removeAppFromTag(mAppInfo.getAppId(), holder.tag.id)) {
                    holder.name.setSelected(false);
                    holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            } else {
                if (client.addAppToTag(mAppInfo.getAppId(), holder.tag.id)) {
                    holder.name.setSelected(true);
                    holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
                }
            }
            client.close();
        }
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
        Tag tag;

        @BindView(android.R.id.text1) TextView name;
        @BindView(android.R.id.icon) ImageView color;

        TagHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            ButterKnife.bind(this, itemView);
        }

        void bindView(int position, Tag tag) {
            this.tag = tag;
            name.setText(tag.name);
            Drawable d = color.getDrawable().mutate();
            DrawableCompat.setTint(DrawableCompat.wrap(d), tag.color);
            color.setImageDrawable(d);
        }
   }
}
