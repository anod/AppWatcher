package com.anod.appwatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
    	
    	LayoutInflater inflater = getActivity().getLayoutInflater();
    	View messageView = (View)inflater.inflate(R.layout.device_id, null);
    	final EditText input = (EditText)messageView.findViewById(R.id.deviceid_edit);
    			
    	String dId = preferences.getDeviceId();
    	if (dId != null) {
    		input.setText(dId);
    	}

    	return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.device_id)
                .setView(messageView)
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
