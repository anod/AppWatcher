package com.anod.appwatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DeviceIdDialog extends SherlockDialogFragment {

	public static DeviceIdDialog newInstance() {
		DeviceIdDialog frag = new DeviceIdDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	final Preferences preferences = new Preferences(getActivity());
    	final EditText input = new EditText(getActivity());
    	input.setHint(R.string.device_id);
    	String dId = preferences.getDeviceId();
    	if (dId != null) {
    		input.setText(dId);
    	}
/*
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(
			Uri.parse(String.format("tel:%s", Uri.encode("*#*#8255#*#*")))
		);
		startActivity(intent);
	*/	
        return new AlertDialog.Builder(getActivity())
             //   .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.device_id)
                .setMessage(R.string.device_id_explanation)
                .setView(input)
                .setPositiveButton(R.string.save_device_id, 
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
            				String newDeviceId = input.getText().toString();
            				if (newDeviceId.length() > 0) {
            					preferences.disableDeviceIdMessage();
            				}
            				preferences.saveDeviceId(newDeviceId);
            				Toast.makeText(getActivity(), "Device Id saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                )
                .setNegativeButton(R.string.alert_dialog_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            
                        }
                    }
                )
                .create();
    }

}
