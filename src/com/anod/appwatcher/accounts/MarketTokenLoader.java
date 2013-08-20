package com.anod.appwatcher.accounts;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class MarketTokenLoader extends AsyncTaskLoader<String>  {
	private Activity mActivity;
	public MarketTokenLoader(Activity activity) {
		super(activity);
		mActivity = activity;
	}

	@Override
	public String loadInBackground() {
		MarketTokenHelper helper = new MarketTokenHelper(getContext());
		return helper.requestToken(mActivity);
	}

}
