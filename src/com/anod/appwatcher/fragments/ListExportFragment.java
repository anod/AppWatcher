package com.anod.appwatcher.fragments;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.ListExportActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.backup.ListExportManager;

public class ListExportFragment extends ListFragment {
	private ImportListAdapter mAdapter;
	private ImportClickListener mRestoreListener;
	private DeleteClickListener mDeleteListener;

	private ListExportManager mBackupManager;
	private Activity mContext;

	private static final String DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss.SSS";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

		Button backupButton = (Button) mContext.findViewById(R.id.backup_button);
		backupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ExportTask().execute("");
			}
		});

		mRestoreListener = new ImportClickListener();
		mDeleteListener = new DeleteClickListener();
		mAdapter = new ImportListAdapter(mContext, R.layout.restore_item, new ArrayList<File>());
	}

	public void load() {
		new FileListTask().execute(0);
	}

	public ImportListAdapter getAdapter() {
		return mAdapter;
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
		//Avoid crash when fragment not attached to activity
		if (!isAdded()) {
			return;
		}
		Resources r = getResources();
		if (code == ListExportManager.RESULT_DONE) {
			new FileListTask().execute(0);
			Toast.makeText(mContext, r.getString(R.string.export_done), Toast.LENGTH_SHORT).show();
			return;
		}
		dismissDialog();
		switch (code) {
		case ListExportManager.ERROR_STORAGE_NOT_AVAILABLE:
			Toast.makeText(mContext, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
			break;
		case ListExportManager.ERROR_FILE_WRITE:
			Toast.makeText(mContext, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private class FileListTask extends AsyncTask<Integer, Void, File[]> {
		@Override
		protected void onPreExecute() {
			showDialog();
			mAdapter.clear();
			mAdapter.notifyDataSetChanged();
		}

		protected File[] doInBackground(Integer... params) {
			return mBackupManager.getFileList();
		}

		protected void onPostExecute(File[] result) {
			//Avoid crash when fragment not attached to activity
			if (!isAdded()) {
				return;
			}

			if (result != null) {
				for (int i = 0; i < result.length; i++) {
					mAdapter.add(result[i]);
					mAdapter.notifyDataSetChanged();
				}
			}
			dismissDialog();

		}
	}
	
	private void showDialog() {
		((ListExportActivity)getActivity()).showDialog();
	}

	private void dismissDialog() {
		((ListExportActivity)getActivity()).dismissDialog();
	}

	private class ImportTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			showDialog();
		}

		protected Integer doInBackground(String... filenames) {
			String filename = filenames[0];
			return mBackupManager.doImport(filename);
		}

		protected void onPostExecute(Integer result) {
			onImportFinish(result);
		}
	}

	private void onImportFinish(int code) {
		//Avoid crash when fragment not attached to activity
		if (!isAdded()) {
			return;
		}

		dismissDialog();
		
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

	private class ImportListAdapter extends ArrayAdapter<File> {
		private int resource;

		public ImportListAdapter(Context _context, int _resource, ArrayList<File> _items) {
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

	private class ImportClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			new ImportTask().execute((String) v.getTag());
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
			showDialog();
		}

		protected Boolean doInBackground(File... files) {
			return files[0].delete();
		}

		protected void onPostExecute(Boolean result) {
			//Avoid crash when fragment not attached to activity
			if (!isAdded()) {
				return;
			}

			dismissDialog();
			if (!result) {
				Toast.makeText(mContext, getString(R.string.unable_delete_file), Toast.LENGTH_SHORT).show();
			} else {
				new FileListTask().execute(0);
			}
		}
	}
}
