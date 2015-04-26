package com.anod.appwatcher.navdrawer.list;

import android.content.Context;

import com.anod.appwatcher.R;
import com.anod.appwatcher.navdrawer.DrawerAdapter;
import com.anod.appwatcher.navdrawer.Item;

/**
 * @author alex
 * @date 2015-03-09
 */
public class Main extends StaticList {
    protected static final int NAVDRAWER_ITEM_ADD = 0;
    protected static final int NAVDRAWER_ITEM_FILTER = 1;
    protected static final int NAVDRAWER_ITEM_TAGS = 2;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 3;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_ITEMS = new int[]{
            NAVDRAWER_ITEM_ADD,
            NAVDRAWER_ITEM_FILTER,
            NAVDRAWER_ITEM_TAGS,
            Item.NAVDRAWER_ITEM_SEPARATOR,
            NAVDRAWER_ITEM_SETTINGS
    };

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_add,
            R.string.navdrawer_item_filter,
            R.string.navdrawer_item_tags,
            R.string.navdrawer_item_settings
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.ic_add_black_36dp,
            R.drawable.ic_filter_list_black_36dp,
            R.drawable.ic_action_tags,
            R.drawable.ic_drawer_settings
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final boolean[] NAVDRAWER_ITEM_NAV = new boolean[] {
            false,
            true,
            true,
            false
    };

    public Main(Context context) {
        super(context);
    }

    @Override
    public void init() {
        for(int itemId : NAVDRAWER_ITEMS) {
            if (Item.isSeparator(itemId)) {
                add(new Item(itemId, 0,0, false));
            } else {
                add(new Item(itemId, NAVDRAWER_TITLE_RES_ID[itemId], NAVDRAWER_ICON_RES_ID[itemId], NAVDRAWER_ITEM_NAV[itemId]));
            }
        }
    }

    @Override
    public int getListType() {
        return Factory.MAIN;
    }

}
