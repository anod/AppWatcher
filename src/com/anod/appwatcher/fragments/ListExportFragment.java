package com.anod.appwatcher.fragments;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.R;
import com.anod.appwatcher.R.id;
import com.anod.appwatcher.R.layout;
import com.anod.appwatcher.R.string;
import com.anod.appwatcher.backup.ListExportManager;

public class ListExportFragment extends ListFragment {
	private RestoreAdapter mAdapter;
	private RestoreClickListener mRestoreListener;
	private DeleteClickListener mDeleteListener;

	private String mLastBackupStr;
	private TextView mLastBackup;
	private SparseArray<Dialog> mManagedDialogs;
	private ListExportManager mBackupManager;
	private Activity mContext;

	private static final int DIALOG_WAIT = 1;

	private static final int DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		return inflater.inflate(R.layout.restore_list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
		setListAdapter(getAdapter());
		load();
	}

	public void init() {
		mBackupManager = new ListExportManager(mContext);

		mLastBackupStr = getString(R.string.last_export);
		Button backupButton = (Button) mContext.findViewById(R.id.backup_button);
		backupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new BackupTask().execute("");
			}
		});
		mLastBackup = (TextView) mContext.findViewById(R.id.last_backup);

		mRestoreListener = new RestoreClickListener();
		mDeleteListener = new DeleteClickListener();
		mAdapter = new RestoreAdapter(mContext, R.layout.restore_item, new ArrayList<File>());
	}

	public void load() {
		updateBackupTime();
		new FileListTask().execute(0);
	}

	public RestoreAdapter getAdapter() {
		return mAdapter;
	}

	private Dialog createDialog(int id) {
		switch (id) {
		case DIALOG_WAIT:
			ProgressDialog waitDialog = new ProgressDialog(mContext);
			waitDialog.setCancelable(true);
			String message = getString(R.string.please_wait);
			waitDialog.setMessage(message);
			return waitDialog;
		}
		return null;
	}

	private class BackupTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// showDialog(DIALOG_WAIT);
		}

		protected Integer doInBackground(String... filenames) {
			String filename = filenames[0];
			return mBackupManager.doBackupMain(filename);
		}

		protected void onPostExecute(Integer result) {
			onBackupFinish(result);
		}
	}

	private void onBackupFinish(int code) {
		Resources r = getResources();
		if (code == ListExportManager.RESULT_DONE) {
			updateBackupTime();
			new FileListTask().execute(0);
			Toast.makeText(mContext, r.getString(R.string.export_done), Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			// dismissDialog(DIALOG_WAIT);
		} catch (IllegalArgumentException e) {
		}
		switch (code) {
		case ListExportManager.ERROR_STORAGE_NOT_AVAILABLE:
			Toast.makeText(mContext, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
			break;
		case ListExportManager.ERROR_FILE_WRITE:
			Toast.makeText(mContext, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void updateBackupTime() {
		String summary;
		long timeMain = mBackupManager.getMainTime();
		if (timeMain > 0) {
			summary = DateUtils.formatDateTime(mContext, timeMain, DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mLastBackup.setText(String.format(mLastBackupStr, summary));
	}

	private class FileListTask extends AsyncTask<Integer, Void, File[]> {
		@Override
		protected void onPreExecute() {
			// showDialog(DIALOG_WAIT);
			mAdapter.clear();
			mAdapter.notifyDataSetChanged();
		}

		protected File[] doInBackground(Integer... params) {
			return mBackupManager.getMainBackups();
		}

		protected void onPostExecute(File[] result) {
			if (result != null) {
				for (int i = 0; i < result.length; i++) {
					mAdapter.add(result[i]);
					mAdapter.notifyDataSetChanged();
				}
			}
			try {
				// dismissDialog(DIALOG_WAIT);
			} catch (IllegalArgumentException e) {
			}

		}
	}

	private class RestoreTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// showDialog(DIALOG_WAIT);
		}

		protected Integer doInBackground(String... filenames) {
			String filename = filenames[0];
			return mBackupManager.doRestoreMain(filename);
		}

		protected void onPostExecute(Integer result) {
			onRestoreFinish(result);
		}
	}

	private void onRestoreFinish(int code) {
		try {
			// dismissDialog(DIALOG_WAIT);
		} catch (IllegalArgumentException e) {
		}
		if (code == ListExportManager.RESULT_DONE) {
			Toast.makeText(mContext, getString(R.string.import_done), Toast.LENGTH_SHORT).show();
			return;
		}
		switch (code) {
		case ListExportManager.ERROR_STORAGE_NOT_AVAILABLE:
			Toast.makeText(mContext, getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
			break;
		case ListExportManager.ERROR_DESERIALIZE:
			Toast.makeText(mContext, getString(R.string.restore_deserialize_failed), Toast.LENGTH_SHORT).show();
			break;
		case ListExportManager.ERROR_FILE_READ:
			Toast.makeText(mContext, getString(R.string.failed_to_read_file), Toast.LENGTH_SHORT).show();
			break;
		case ListExportManager.ERROR_FILE_NOT_EXIST:
			Toast.makeText(mContext, getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private class RestoreAdapter extends ArrayAdapter<File> {
		private int resource;

		public RestoreAdapter(Context _context, int _resource, ArrayList<File> _items) {
			super(_context, _resource, _items);
			resource = _resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(resource, null);
			}
			File entry = getItem(position);

			TextView titleView = (TextView) v.findViewById(android.R.id.title);
			String name = entry.getName();
			name = name.substring(0, name.lastIndexOf(ListExportManager.FILE_EXT_DAT));
			titleView.setTag(name);
			titleView.setText(name);

			titleView.setOnClickListener(mRestoreListener);
			ImageView applyView = (ImageView) v.findViewById(R.id.apply_icon);
			applyView.setTag(name);
			applyView.setOnClickListener(mRestoreListener);

			ImageView deleteView = (ImageView) v.findViewById(R.id.delete_action_button);
			deleteView.setTag(entry);
			deleteView.setOnClickListener(mDeleteListener);

			v.setId(position);
			return v;
		}
	}

	private class RestoreClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			new RestoreTask().execute((String) v.getTag());
		}
	}

	private class DeleteClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			File file = (File) v.getTag();
			new DeleteTask().execute(file);
		}
	}

	private class DeleteTask extends AsyncTask<File, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			// showDialog(DIALOG_WAIT);
		}

		protected Boolean doInBackground(File... files) {
			return files[0].delete();
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Toast.makeText(mContext, getString(R.string.unable_delete_file), Toast.LENGTH_SHORT).show();
			} else {
				new FileListTask().execute(0);
			}
		}
	}
}
