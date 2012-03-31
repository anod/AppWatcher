package com.anod.appwatcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class DeviceIdActivity extends SherlockActivity {

	private EditText mDeviceId;
	private Button mSaveBtn;
	private Button mOpenMonitorButton;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_id);
		final Preferences preferences = new Preferences(this);
		mDeviceId = (EditText)findViewById(R.id.device_id);
		
		String cDeviceId = preferences.getDeviceId();
		if (cDeviceId != null) {
			mDeviceId.setText(cDeviceId);
		}
		
		mSaveBtn = (Button)findViewById(R.id.save_button);
		mSaveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String newDeviceId = mDeviceId.getText().toString();
				if (newDeviceId.length() > 0) {
					preferences.disableDeviceIdMessage();
				}
				preferences.saveDeviceId(newDeviceId);
				Toast.makeText(DeviceIdActivity.this, "Device Id saved", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		mOpenMonitorButton = (Button)findViewById(R.id.open_button);
		mOpenMonitorButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(
					Uri.parse(String.format("tel:%s", Uri.encode("*#*#8255#*#*")))
				);
				startActivity(intent);
			}
		});
	}

}
