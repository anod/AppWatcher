package com.anod.appwatcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.backup.ExportTask;
import com.anod.appwatcher.backup.ListExportManager;
import com.anod.appwatcher.utils.EmailReportSender;

import org.acra.ACRA;
import org.acra.ErrorReporter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingsActivity extends ActionBarActivity implements ExportTask.Listener, AdapterView.OnItemClickListener {
    private static final int ACTION_EXPORT = 3;
    private static final int ACTION_IMPORT = 4;
    private static final int ACTION_LICENSES = 6;
    private static final int ACTION_ABOUT = 5;
    private ListView mListView;
    private int mAboutCounter;

    @Override
    public void onExportStart() {
        setSupportProgressBarIndeterminate(true);
    }

    @Override
    public void onExportFinish(int code) {
        setSupportProgressBarIndeterminate(true);
        Resources r = getResources();
        if (code == ListExportManager.RESULT_DONE) {
            Toast.makeText(this, r.getString(R.string.export_done), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (code) {
            case ListExportManager.ERROR_STORAGE_NOT_AVAILABLE:
                Toast.makeText(this, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
                break;
            case ListExportManager.ERROR_FILE_WRITE:
                Toast.makeText(this, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
                break;
        }
    }


    static class Preference {
        final int title;
        final int layout;
        Preference(int title, int layout) {
            this.title = title;
            this.layout = layout;
        }
    }
    static class Category extends Preference {
        Category(int title) {
            super(title, R.layout.preference_category);
        }
    }

    static  class Item extends Preference {
        final int action;
        final int summary;

        Item(int title, int summary, int action) {
            super(title, R.layout.preference_holo);
            this.summary = summary;
            this.action = action;
        }
    }

    static class PreferenceAdapter extends ArrayAdapter<Preference> {

        private final SettingsActivity mActivity;
        private final LayoutInflater mInflater;

        public PreferenceAdapter(SettingsActivity activity, List<Preference> objects) {
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
                View icon = view.findViewById(android.R.id.icon);
                icon.setVisibility(View.GONE);

                if (((Item) pref).summary > 0) {
                    TextView summary = (TextView) view.findViewById(android.R.id.summary);
                    summary.setText(((Item) pref).summary);
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
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.preference_list_content);

        ArrayList<Preference> preferences = new ArrayList<Preference>();

        preferences.add(new Category(R.string.pref_header_drive_sync));
        preferences.add(new Item(R.string.pref_title_drive_sync_enabled, R.string.pref_descr_drive_sync_enabled, 1));
        preferences.add(new Item(R.string.pref_title_drive_sync_now, R.string.pref_descr_drive_sync_now, 2));

        preferences.add(new Category(R.string.pref_header_backup));
        preferences.add(new Item(R.string.pref_title_export, R.string.pref_descr_export, ACTION_EXPORT));
        preferences.add(new Item(R.string.pref_title_import, R.string.pref_descr_import, ACTION_IMPORT));

        preferences.add(new Category(R.string.pref_header_about));
        preferences.add(new Item(R.string.pref_title_about, R.string.pref_descr_about, ACTION_ABOUT));
        preferences.add(new Item(R.string.pref_title_opensource, R.string.pref_descr_opensource, ACTION_LICENSES));

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setAdapter(new PreferenceAdapter(this, preferences));
        mListView.setOnItemClickListener(this);
        mAboutCounter = 0;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Preference pref = (Preference) mListView.getItemAtPosition(position);
        if (pref instanceof Item) {
            int action = ((Item) pref).action;
            if (action== ACTION_EXPORT) {
                new ExportTask(this, this).execute("");
            } else if (action==ACTION_IMPORT) {
                startActivity(new Intent(this, ListExportActivity.class));
            } else if (action == ACTION_LICENSES) {
                new LicensesDialog(this, R.raw.notices, false, true).show();
            } else if (action == ACTION_ABOUT) {
                onAboutAction();
            }
        }
    }

    private void onAboutAction() {
        if (mAboutCounter >= 4) {
            ErrorReporter rs = ACRA.getErrorReporter();
            rs.removeAllReportSenders();

            EmailReportSender sender = new EmailReportSender(getApplicationContext());
            rs.setReportSender(sender);
            Throwable ex = new Throwable("Report a problem");
            rs.handleException(ex);
        } else {
            if (mAboutCounter >=2) {
                Toast.makeText(this, (5 - mAboutCounter + 1) + " taps to report a problem", Toast.LENGTH_SHORT).show();
            }
            mAboutCounter++;
        }
    }


    private String getAppVersion() {
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return versionName;
    }
}
