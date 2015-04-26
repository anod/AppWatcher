package com.anod.appwatcher.navdrawer.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.anod.appwatcher.navdrawer.DrawerAdapter;
import com.anod.appwatcher.navdrawer.Item;
import com.anod.appwatcher.navdrawer.RowViewBuilder;

/**
 * @author alex
 * @date 2015-03-09
 */
public abstract class StaticList extends ArrayAdapter<Item> implements DrawerAdapter {
    private final RowViewBuilder mViewBuilder;

    public StaticList(Context context) {
        super(context, 0);
        mViewBuilder = new RowViewBuilder(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        return mViewBuilder.getView(item, convertView, parent);

    }

    @Override
    public int getViewTypeCount() {
        return mViewBuilder.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (getCount() == 0) {
            return RowViewBuilder.VIEW_TYPE_DEFAULT;
        }
        Item item = getItem(position);
        return mViewBuilder.getItemViewType(item);
    }

}
