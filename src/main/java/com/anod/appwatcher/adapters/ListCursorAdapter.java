package com.anod.appwatcher.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;

public class ListCursorAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    public ListCursorAdapter(Context context) {
        super(context, null, 0);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AppListCursor wrapper = (AppListCursor) cursor;
        AppInfo app = wrapper.getAppInfo();

        AppViewHolder holder = (AppViewHolder) view.getTag();
        holder.bindView(cursor.getPosition(), app);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.list_item_app, parent, false);
        v.setClickable(true);
        v.setFocusable(true);
        return v;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }

}