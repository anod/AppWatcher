package com.anod.appwatcher;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.backup.ListExportManager;
import com.anod.appwatcher.fragments.WaitDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ListExportActivity extends ActionBarActivity {

	private static final String WAIT_DIALOG = "wait_dialog";
	private ListExportManager mBackupManager;
	private ExportListener mExportListener;

	private static final String DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss.SSS";

	public interface ExportListener {
		void OnFinish();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_export);

		mBackupManager = new ListExportManager(this);

	}

	public ListExportManager getBackupManager() {
		return mBackupManager;
	}

	public void setExportListener(ExportListener listener) {
		mExportListener = listener;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.backup, menu);


		final MenuItem exportItem = menu.findItem(R.id.menu_export);
		exportItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				new ExportTask().execute("");
				return true;
			}
		});


		return true;
	}
	
	public void showDialog() {
		DialogFragment dialogFragment = WaitDialogFragment.newInstance();
		dialogFragment.show(getSupportFragmentManager(), WAIT_DIALOG);
	}
	
	public void dismissDialog() {
		DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(WAIT_DIALOG);
		if (dialogFragment != null) {
			dialogFragment.dismiss();
		}
	}

	private class ExportTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// showDialog(DIALOG_WAIT);
		}

		protected Integer doInBackground(String... filenames) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FILENAME, Locale.US);
			String filename =  sdf.format(new Date(System.currentTimeMillis()));
			return mBackupManager.doExport(filename);
		}
		protected void onPostExecute(Integer result) {
			onExportFinish(result);
		}
	}

	private void onExportFinish(int code) {
		Resources r = getResources();
		if (code == ListExportManager.RESULT_DONE) {
			if (mExportListener != null) {
				mExportListener.OnFinish();
			}
			Toast.makeText(this, r.getString(R.string.export_done), Toast.LENGTH_SHORT).show();
			return;
		}
		dismissDialog();
		switch (code) {
			case ListExportManager.ERROR_STORAGE_NOT_AVAILABLE:
				Toast.makeText(this, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
				break;
			case ListExportManager.ERROR_FILE_WRITE:
				Toast.makeText(this, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
