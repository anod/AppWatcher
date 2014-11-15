package com.anod.appwatcher.utils;

import android.content.Context;
import android.content.Intent;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

/**
 * @author alex
 * @date 6/24/14
 */
public class EmailReportSender implements ReportSender {

	private final Context mContext;

	public EmailReportSender(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void send(CrashReportData errorContent) throws ReportSenderException {

		final String body = buildBody(errorContent);

		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AppWatcher: Report a problem");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "alex.gavrishev@gmail.com" });
		mContext.startActivity(emailIntent);
	}

	private String buildBody(CrashReportData errorContent) {
		ReportField[] fields = {
			ReportField.ANDROID_VERSION,
			ReportField.APP_VERSION_NAME,
			ReportField.BRAND,
			ReportField.PHONE_MODEL,
			ReportField.CUSTOM_DATA,
			ReportField.PRODUCT,
			ReportField.BUILD,
			ReportField.LOGCAT,
		};

		final StringBuilder builder = new StringBuilder();
		for (ReportField field : fields) {
			builder.append(field.toString()).append("=");
			builder.append(errorContent.get(field));
			builder.append('\n');
		}
		builder
			.append('\n')
			.append("The technical data is attached above.")
			.append('\n')
			.append("Please, describe the problem:")
			.append('\n')
		;
		return builder.toString();
	}
}
