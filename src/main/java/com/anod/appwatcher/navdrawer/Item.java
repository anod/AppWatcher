package com.anod.appwatcher.navdrawer;

/**
 * @author alex
 * @date 2015-03-10
 */
public class Item {
    public static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    public static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    public int iconRes;
    public int titleRes;
    public int id;
    public boolean isNav;

    public Item(int id, int titleRes, int iconRes) {
        this(id, titleRes, iconRes, false);
    }

    public Item(int id, int titleRes, int iconRes, boolean isNav) {
        this.id=id;
        this.titleRes=titleRes;
        this.iconRes=iconRes;
        this.isNav = isNav;
    }

    public static boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }
}
