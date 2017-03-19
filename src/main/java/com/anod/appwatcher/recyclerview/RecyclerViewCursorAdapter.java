package com.anod.appwatcher.recyclerview;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author alex
 * @date 2015-06-20
 */
public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder, CR extends CursorWrapper> extends RecyclerView.Adapter<VH> {
    private boolean mDataValid;
    private final Context mContext;
    private final int mResource;
    private CR mCursor;

    public RecyclerViewCursorAdapter(Context context, @LayoutRes int itemResource) {
        mContext = context;
        mResource = itemResource;
    }

    protected abstract VH onCreateViewHolder(View itemView);
    protected abstract void onBindViewHolder(VH holder, int position, CR cursor);

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(createItemView(parent, viewType));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(holder, position, mCursor);
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    protected View createItemView(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        v.setClickable(true);
        v.setFocusable(true);
        return v;
    }

    public void swapData(CR newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        mCursor = newCursor;
        if (newCursor != null) {
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetChanged();
        }
    }
}
