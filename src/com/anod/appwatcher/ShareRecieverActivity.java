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
		String text = intent.getStringExtra(Intent.EXTRA_TEXT);
		
    	Intent searchIntent = new Intent(this, MarketSearchActivity.class);
    	boolean fallback = true;
		if (text.startsWith(URL_PLAYSTORE)) {
			UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(text);
			String id = sanitizer.getValue("id");
			if (id != null) {
				searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, "id:"+id);
				searchIntent.putExtra(MarketSearchActivity.EXTRA_EXACT, true);
				fallback = false;
			}
		}
		
		if (fallback) {
			String title = intent.getStringExtra(Intent.EXTRA_TITLE);
			if (title != null && !title.equals("")) {
				searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, title);
			} else if (text != null && !text.equals("")) {
				searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, text);				
			} else {
				searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, "");
			}
			searchIntent.putExtra(MarketSearchActivity.EXTRA_EXACT, false);			
		}
		searchIntent.putExtra(MarketSearchActivity.EXTRA_SHARE, true);		
		startActivity(searchIntent);
		finish();
	}


}
