package com.anod.appwatcher.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.ListExportActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.backup.ImportTask;
import com.anod.appwatcher.backup.DbBackupManager;

import java.io.File;
import java.util.ArrayList;

public class ListExportFragment extends ListFragment implements ImportTask.Listener {
	private ImportListAdapter mAdapter;
	private ImportClickListener mRestoreListener;
	private DeleteClickListener mDeleteListener;

	private DbBackupManager mBackupManager;
	private Context mContext;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_restore_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
		setListAdapter(getAdapter());
		reload();
	}

	public void init() {
		mBackupManager = ((ListExportActivity)getActivity()).getBackupManager();
		mRestoreListener = new ImportClickListener();
		mDeleteListener = new DeleteClickListener();
		mAdapter = new ImportListAdapter(mContext, R.layout.list_item_restore, new ArrayList<File>());
	}

	public ImportListAdapter getAdapter() {
		return mAdapter;
	}

	public void reload() {
		new FileListTask().execute(0);
	}

	private class FileListTask extends AsyncTask<Integer, Void, File[]> {
		@Override
		protected void onPreExecute() {
		}

		protected File[] doInBackground(Integer... params) {
			return mBackupManager.getFileList();
		}

		protected void onPostExecute(File[] result) {
			//Avoid crash when fragment not attached to activity
			if (!isAdded()) {
				return;
			}
            mAdapter.clear();
			if (result != null) {
				for (File aResult : result) {
					mAdapter.add(aResult);
				}
			}
            mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onImportFinish(int code) {
		//Avoid crash when fragment not attached to activity
		if (!isAdded()) {
			return;
		}

		ImportTask.showImportFinishToast(mContext, code);

        getActivity().getSupportFragmentManager().popBackStack();
	}

	private class ImportListAdapter extends ArrayAdapter<File> {
		private int resource;

		ImportListAdapter(Context _context, int _resource, ArrayList<File> _items) {
			super(_context, _resource, _items);
			resource = _resource;
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(resource, null);
			}
			File entry = getItem(position);

			TextView titleView = (TextView) v.findViewById(android.R.id.title);
			String name = entry.getName();
			name = name.substring(0, name.lastIndexOf(DbBackupManager.FILE_EXT_DAT));
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
			File file = DbBackupManager.getBackupFile((String) v.getTag());
			Uri uri = Uri.fromFile(file);
			new ImportTask(getContext(), ListExportFragment.this).execute(uri);
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

		}

		protected Boolean doInBackground(File... files) {
			return files[0].delete();
		}

		protected void onPostExecute(Boolean result) {
			//Avoid crash when fragment not attached to activity
			if (!isAdded()) {
				return;
			}

			if (!result) {
				Toast.makeText(mContext, getString(R.string.unable_delete_file), Toast.LENGTH_SHORT).show();
			} else {
				new FileListTask().execute(0);
			}
		}
	}
}
