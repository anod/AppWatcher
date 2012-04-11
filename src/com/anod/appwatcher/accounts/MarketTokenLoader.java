package com.anod.appwatcher.accounts;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class MarketTokenLoader extends AsyncTaskLoader<String>  {

	public MarketTokenLoader(Context context) {
		super(context);
	}

	@Override
	public String loadInBackground() {
		MarketTokenHelper helper = new MarketTokenHelper(getContext());
		return helper.requestToken();
	}

}
