package com.anod.appwatcher.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.PackageManagerUtils;

/**
 * @author alex
 * @date 2015-06-20
 */
public class ListCursorAdapterWrapper extends RecyclerView.Adapter<AppViewHolder> {
    private final ListCursorAdapter mCursorAdapter;
    private final AppViewHolderDataProvider mDataProvider;
    private Context mContext;
    private AppViewHolder.OnClickListener mListener;

    public ListCursorAdapterWrapper(Context context, PackageManagerUtils pmutils, AppViewHolder.OnClickListener listener) {
        mCursorAdapter = new ListCursorAdapter(context);
        mContext = context;
        mListener = listener;

        mDataProvider = new AppViewHolderDataProvider(context, pmutils);
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        AppViewHolder holder = new AppViewHolder(v, mDataProvider, mListener);
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

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void swapData(Cursor data) {
        int totalCount = (data == null) ? 0 : data.getCount();
        mDataProvider.setTotalCount(totalCount);
        mCursorAdapter.swapCursor(data);
        notifyDataSetChanged();
    }

}
