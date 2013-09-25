package de.psdev.licensesdialog.licenses;

import android.content.Context;

import com.anod.appwatcher.R;

/**
 * @author alex
 * @date 9/24/13
 */
public class NewBSDLicense extends License {

	@Override
	public String getName() {
		return "New BSD License";
	}

	@Override
	public String getSummaryText(final Context context) {
		return getContent(context, R.raw.bsd_new_summary);
	}

	@Override
	public String getFullText(final Context context) {
		return getContent(context, R.raw.bsd_new_summary);
	}

	@Override
	public String getVersion() {
		return "";
	}

	@Override
	public String getUrl() {
		return "http://opensource.org/licenses/BSD-3-Clause";
	}
}
