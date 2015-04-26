package com.anod.appwatcher.navdrawer.list;

import android.content.Context;

import com.anod.appwatcher.R;
import com.anod.appwatcher.navdrawer.DrawerAdapter;
import com.anod.appwatcher.navdrawer.Item;

/**
 * @author alex
 * @date 2015-03-09
 */
public class Filters extends StaticList {
    public static final int NAVDRAWER_ITEM_ALL = 0;
    public static final int NAVDRAWER_ITEM_INSTALLED = 1;
    public static final int NAVDRAWER_ITEM_UNINSTALLED = 2;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_ITEMS = new int[]{
            NAVDRAWER_ITEM_ALL,
            NAVDRAWER_ITEM_INSTALLED,
            NAVDRAWER_ITEM_UNINSTALLED
    };

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_filter_all,
            R.string.navdrawer_item_filter_installed,
            R.string.navdrawer_item_filter_notinstalled
    };

    public Filters(Context context) {
        super(context);
    }

    @Override
    public void init() {
        for(int itemId : NAVDRAWER_ITEMS) {
            add(new Item(itemId, NAVDRAWER_TITLE_RES_ID[itemId], 0));
        }
    }

    @Override
    public int getListType() {
        return Factory.FILTERS;
    }
}
