package com.anod.appwatcher.navdrawer.list;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.anod.appwatcher.navdrawer.DrawerAdapter;
import com.anod.appwatcher.navdrawer.Item;
import com.anod.appwatcher.navdrawer.RowViewBuilder;

/**
 * @author alex
 * @date 2015-03-09
 */
public class Tags extends CursorAdapter implements DrawerAdapter {
    private final RowViewBuilder mViewBuilder;

    public Tags(Context context) {
        super(context, createCursor(), 0);
        mViewBuilder = new RowViewBuilder(context);
    }

    private static Cursor createCursor() {
        return null;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Item item = convertItem(cursor);
        return mViewBuilder.getView(item, null,parent);

    }

    private Item convertItem(Cursor cursor) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Item item = convertItem(cursor);
        mViewBuilder.getView(item, view, null);
    }

    @Override
    public void init() {

    }

    @Override
    public int getListType() {
        return Factory.TAGS;
    }

    @Override
    public Item getItem(int position) {
        return (Item)super.getItem(position);
    }
}
