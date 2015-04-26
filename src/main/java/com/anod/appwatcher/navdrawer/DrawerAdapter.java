package com.anod.appwatcher.navdrawer;

/**
 * @author alex
 * @date 2015-03-19
 */
public interface DrawerAdapter {

    void init();

    int getListType();

    Item getItem(int position);
}
