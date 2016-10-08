package com.anod.appwatcher.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.MenuItemAnimation;

import java.util.ArrayList;
import java.util.List;

abstract public class SettingsActionBarActivity extends ToolbarActivity implements AdapterView.OnItemClickListener {
    protected ListView mListView;
    private PreferenceAdapter mPreferenceAdapter;
    private MenuItemAnimation mRefreshAnim;

    public static class Preference {
        final int title;
        final int layout;
        public Preference(@StringRes int title,@LayoutRes int layout) {
            this.title = title;
            this.layout = layout;
        }
    }
    public static class Category extends Preference {
        public Category(@StringRes int title) {
            super(title, R.layout.preference_category);
        }
    }

    public static class Item extends Preference {
        final int action;
        public int summaryRes;
        public String summary;
        public int widget;
        public boolean enabled = true;

        public Item(@StringRes int title,@StringRes int summaryRes, int action) {
            super(title, R.layout.preference_holo);
            this.summaryRes = summaryRes;
            this.action = action;
        }
    }

    public static class CheckboxItem extends Item {
        public boolean checked = false;

        public CheckboxItem(@StringRes int title,@StringRes int summaryRes, int action) {
            super(title, summaryRes, action);
            this.widget = R.layout.preference_widget_checkbox;
        }

        public CheckboxItem(int title, int summaryRes, int action, boolean checked) {
            this(title, summaryRes, action);
            this.checked = checked;
        }

        public void switchState() {
            this.checked = !this.checked;
        }
    }

    static class PreferenceAdapter extends ArrayAdapter<Preference> {

        private final SettingsActionBarActivity mActivity;
        private final LayoutInflater mInflater;

        public PreferenceAdapter(SettingsActionBarActivity activity, List<Preference> objects) {
            super(activity, 0, objects);
            mActivity = activity;
            mInflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getItemViewType(int position) {
            Preference pref = getItem(position);
            if (pref instanceof CheckboxItem) {
                return 0;
            }
            if (pref instanceof Category) {
                return 1;
            }
            return 2;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Preference pref = getItem(position);

            View view;
            if (convertView == null) {
                view = mInflater.inflate(pref.layout, parent, false);
            } else {
                view = convertView;
            }

            TextView title = (TextView) view.findViewById(android.R.id.title);
            title.setText(pref.title);

            if (pref instanceof Item) {
                Item item = (Item) pref;
                View icon = view.findViewById(android.R.id.icon);
                if (icon != null) {
                    icon.setVisibility(View.GONE);
                }

                TextView summary = (TextView) view.findViewById(android.R.id.summary);
                if (item.summaryRes > 0) {
                    summary.setText(item.summaryRes);
                } else if (!TextUtils.isEmpty(item.summary)) {
                    summary.setText(item.summary);
                }

                final ViewGroup widgetFrame = (ViewGroup) view.findViewById(android.R.id.widget_frame);
                if (item.widget > 0) {

                    if (item instanceof CheckboxItem) {
                        CheckBox checkBox = (CheckBox) widgetFrame.findViewById(android.R.id.checkbox);
                        if (checkBox == null) {
                            mInflater.inflate(item.widget, widgetFrame);
                            checkBox = (CheckBox) widgetFrame.findViewById(android.R.id.checkbox);
                        }
                        checkBox.setChecked(((CheckboxItem) item).checked);
                    }
                } else {
                    widgetFrame.setVisibility(View.GONE);
                }
            }

            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            final Preference pref = getItem(position);
            if(pref instanceof Category) {
                return false;
            }
            if (pref instanceof Item) {
                return ((Item) pref).enabled;
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar();

        mRefreshAnim = new MenuItemAnimation(this, R.anim.rotate);
        mRefreshAnim.setInvisibleMode(true);
        init();

        ArrayList<Preference> preferences = initPreferenceItems();

        mPreferenceAdapter = new PreferenceAdapter(this, preferences);
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setAdapter(mPreferenceAdapter);
        mListView.setOnItemClickListener(this);
    }

    protected abstract void init();
    protected abstract ArrayList<Preference> initPreferenceItems();
    protected abstract void onPreferenceItemClick(int action, Item pref);

    protected void setProgressVisibility(boolean visible) {
        if (visible) {
            mRefreshAnim.start();
        } else {
            mRefreshAnim.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.settings, menu);
        MenuItem refreshMenuItem = menu.findItem(R.id.menu_act_refresh);
        mRefreshAnim.setMenuItem(refreshMenuItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Preference pref = (Preference) mListView.getItemAtPosition(position);
        if (pref instanceof Item) {
            int action = ((Item) pref).action;
            if (pref instanceof CheckboxItem) {
                ((CheckboxItem) pref).switchState();
            }
            onPreferenceItemClick(action, (Item) pref);
        }
    }

    protected void notifyDataSetChanged() {
        mPreferenceAdapter.notifyDataSetChanged();
    }

}
