package com.anod.appwatcher.watchlist;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 2015-06-20
 */
public class ListCursorAdapterWrapper extends RecyclerView.Adapter<AppViewHolder> {
    private final ListCursorAdapter mCursorAdapter;
    private Context mContext;
    private AppViewHolder.OnClickListener mListener;

    public ListCursorAdapterWrapper(Context context, PackageManagerUtils pmutils, AppViewHolder.OnClickListener listener) {
        mCursorAdapter = new ListCursorAdapter(context, pmutils);
        mContext = context;
        mListener = listener;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        AppViewHolder holder = new AppViewHolder(v, mListener);
        v.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        if (!mCursorAdapter.getCursor().moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public void swapCursor(Cursor data) {
        mCursorAdapter.swapCursor(data);
        notifyDataSetChanged();
    }

    public void setNewAppsCount(int newAppsCount) {
        mCursorAdapter.setNewAppsCount(newAppsCount);
    }
}
