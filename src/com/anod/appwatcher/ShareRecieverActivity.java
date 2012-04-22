package com.anod.appwatcher;

import android.app.Activity;
import android.content.Intent;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;

public class ShareRecieverActivity extends Activity {
	private static final String URL_PLAYSTORE = "https://play.google.com";

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String strURL = intent.getStringExtra(Intent.EXTRA_TEXT);
		
    	Intent searchIntent = new Intent(this, MarketSearchActivity.class);
		if (strURL.startsWith(URL_PLAYSTORE)) {
			UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(strURL);
			String id = sanitizer.getValue("id");
			if (id != null) {
				searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, "id:"+id);
			}
		} else {
			String title = intent.getStringExtra(Intent.EXTRA_TITLE);
			searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, title);
		}
		startActivity(searchIntent);
		finish();
	}


}
