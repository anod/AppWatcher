package com.anod.appwatcher.navdrawer.list;

import android.content.Context;
import android.widget.BaseAdapter;

/**
 * @author alex
 * @date 2015-03-12
 */
public class Factory {
    public static final int MAIN = 0;
    public static final int FILTERS = Main.NAVDRAWER_ITEM_FILTER;
    public static final int TAGS = Main.NAVDRAWER_ITEM_TAGS;

    public static BaseAdapter create(int listType, Context context) {
        if (listType == MAIN) {
            return new Main(context);
        }
        if (listType == FILTERS) {
            return new Filters(context);
        }
        if (listType == TAGS) {
            return new Tags(context);
        }
        return null;
    }

}
