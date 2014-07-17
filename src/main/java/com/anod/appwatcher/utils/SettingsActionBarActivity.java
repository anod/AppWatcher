package com.anod.appwatcher.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.ListExportActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.backup.ExportTask;
import com.anod.appwatcher.backup.GDriveBackup;
import com.anod.appwatcher.backup.ListExportManager;

import org.acra.ACRA;
import org.acra.ErrorReporter;

import java.util.ArrayList;
import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;

abstract public class SettingsActionBarActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    protected ListView mListView;

    public static class Preference {
        final int title;
        final int layout;
        public Preference(int title, int layout) {
            this.title = title;
            this.layout = layout;
        }
    }
    public static class Category extends Preference {
        public Category(int title) {
            super(title, R.layout.preference_category);
        }
    }

    public static class Item extends Preference {
        final int action;
        final int summaryRes;
        public String summary;
        public int widget;
        public boolean enabled = true;

        public Item(int title, int summaryRes, int action) {
            super(title, R.layout.preference_holo);
            this.summaryRes = summaryRes;
            this.action = action;
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
            if (pref instanceof Category) {
                return 0;
            }
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
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
                icon.setVisibility(View.GONE);

                TextView summary = (TextView) view.findViewById(android.R.id.summary);
                if (item.summaryRes > 0) {
                    summary.setText(item.summaryRes);
                } else if (!TextUtils.isEmpty(item.summary)) {
                    summary.setText(item.summary);
                }

                final ViewGroup widgetFrame = (ViewGroup) view.findViewById(android.R.id.widget_frame);
                if (item.widget > 0) {
                    mInflater.inflate(item.widget, widgetFrame);
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
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.preference_list_content);

        init();

        ArrayList<Preference> preferences = getPreferenceItems();

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setAdapter(new PreferenceAdapter(this, preferences));
        mListView.setOnItemClickListener(this);
    }

    protected abstract void init();
    protected abstract ArrayList<Preference> getPreferenceItems();
    protected abstract void onPreferenceItemClick(int action, Item pref);
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Preference pref = (Preference) mListView.getItemAtPosition(position);
        if (pref instanceof Item) {
            int action = ((Item) pref).action;
            onPreferenceItemClick(action, (Item) pref);
        }
    }
}
