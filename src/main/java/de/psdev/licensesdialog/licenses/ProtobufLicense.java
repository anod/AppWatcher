package de.psdev.licensesdialog.licenses;

import android.content.Context;

import com.anod.appwatcher.R;

/**
 * @author alex
 * @date 9/24/13
 */
public class ProtobufLicense extends License {

	@Override
	public String getName() {
		return "Protobuf License";
	}

	@Override
	public String getSummaryText(final Context context) {
		return getContent(context, R.raw.protobuf);
	}

	@Override
	public String getFullText(final Context context) {
		return getContent(context, R.raw.protobuf);
	}

	@Override
	public String getVersion() {
		return "";
	}

	@Override
	public String getUrl() {
		return "https://raw.githubusercontent.com/google/protobuf/master/LICENSE";
	}
}
