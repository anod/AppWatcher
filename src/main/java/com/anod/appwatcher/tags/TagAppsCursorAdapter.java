package com.anod.appwatcher.tags;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.anod.appwatcher.App;
import com.anod.appwatcher.R;
import com.anod.appwatcher.content.AppListCursor;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.recyclerview.RecyclerViewCursorAdapter;
import com.anod.appwatcher.utils.AppIconLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

class TagAppsCursorAdapter extends RecyclerViewCursorAdapter<TagAppsCursorAdapter.ItemViewHolder, AppListCursor> {
    private final AppIconLoader mIconLoader;
    private final TagAppsManager mManager;

    static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(android.R.id.title)
        public CheckedTextView title;
        @BindView(android.R.id.icon)
        public ImageView icon;

        private final AppIconLoader mIconLoader;
        private final TagAppsManager mManager;
        private AppInfo app;

        ItemViewHolder(View itemView, AppIconLoader iconLoader, TagAppsManager manager) {
            super(itemView);
            mIconLoader = iconLoader;
            mManager = manager;
            ButterKnife.bind(this, itemView);
        }

        void bindView(int position, AppInfo app) {
            this.app = app;
            this.title.setText(app.title);
            this.title.setChecked(mManager.isSelected(app.getAppId()));
            this.itemView.findViewById(android.R.id.content).setOnClickListener(this);
            mIconLoader.loadAppIntoImageView(app, this.icon, R.drawable.ic_android_black_24dp);
        }

        @Override
        public void onClick(View v) {
            this.title.toggle();
            mManager.updateApp(this.app.getAppId(), title.isChecked());
        }
    }

    TagAppsCursorAdapter(Context context, TagAppsManager manager) {
        super(context, R.layout.list_item_import_app);
        mIconLoader = App.provide(context).iconLoader();
        mManager = manager;
    }

    @Override
    protected ItemViewHolder onCreateViewHolder(View itemView) {
        return new ItemViewHolder(itemView, mIconLoader, mManager);
    }

    @Override
    protected void onBindViewHolder(ItemViewHolder holder, int position, AppListCursor cursor) {
        AppInfo app = cursor.getAppInfo();
        holder.bindView(cursor.getPosition(), app);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    void selectAllApps(boolean select) {
        mManager.selectAll(select);
        this.notifyDataSetChanged();
    }
}