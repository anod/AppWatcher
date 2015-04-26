package com.anod.appwatcher.navdrawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.UIUtils;

/**
 * @author alex
 * @date 2014-10-21
 */
public class RowViewBuilder {

    public static final int VIEW_TYPE_DEFAULT = 0;
    private LayoutInflater mInflater;
    private int mSelectedItemId;
    private Context mContext;


    public RowViewBuilder(Context context) {
        mContext = context;
    }

    public View getView(final Item item, View convertView, ViewGroup container) {
        boolean selected = mSelectedItemId == item.id;

        View view;
        if (convertView == null) {
            int layoutToInflate = 0;
            if (item.id == Item.NAVDRAWER_ITEM_SEPARATOR) {
                layoutToInflate = R.layout.navdrawer_separator;
            } else if (item.id == Item.NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
                layoutToInflate = R.layout.navdrawer_separator;
            } else {
                layoutToInflate = R.layout.navdrawer_item;
            }
            view = getLayoutInflater().inflate(layoutToInflate, container, false);
        } else {
            view = convertView;
        }

        if (Item.isSeparator(item.id)) {
            // we are done
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        int iconId = item.id >= 0 ? item.iconRes : 0;
        int titleId = item.id >= 0 ? item.titleRes : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(mContext.getString(titleId));

        formatNavDrawerItem(view, item.id, selected);

        return view;
    }

    public int getViewTypeCount() {
        return 3;
    }

    public int getItemViewType(Item item) {
        if (item.id == Item.NAVDRAWER_ITEM_SEPARATOR) {
            return 2;
        } else if (item.id == Item.NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
            return 1;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    public void setSelectedItemId(int selectedItemId) {
        mSelectedItemId = selectedItemId;
    }



    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (Item.isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        if (selected) {
            view.setBackgroundResource(R.drawable.selected_navdrawer_item_background);
        }

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                mContext.getResources().getColor(R.color.navdrawer_text_color_selected) :
                mContext.getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                mContext.getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                mContext.getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private LayoutInflater getLayoutInflater() {
        if (mInflater == null) {
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return mInflater;
    }

}
