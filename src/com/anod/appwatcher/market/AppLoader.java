package com.anod.appwatcher.market;

import android.util.Log;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

public class AppLoader {
	private MarketSession mMarketSession;
	private boolean mExtended;

	public AppLoader(MarketSession session, boolean extended) {
		mMarketSession = session;
		mExtended = extended;
	}
	
	class ResponseWrapper {
		AppsResponse response;
	}
	
	public App load(String appId) {
		AppsRequest appsRequest = AppsRequest.newBuilder()
			.setAppId(appId)
			.setWithExtendedInfo(mExtended).build();
		final ResponseWrapper respWrapper = new ResponseWrapper();

		try {
			synchronized (mMarketSession) {	
				mMarketSession.append(appsRequest, new Callback<AppsResponse>() {
					@Override
					public void onResult(ResponseContext context, AppsResponse response) {
						respWrapper.response = response;
					}
				});
				mMarketSession.flush();
			}
		} catch (Exception e) {
			Log.e("AppWatcher", e.toString());
			return null;
		}
		if (respWrapper.response.getAppCount() > 0) {
			return respWrapper.response.getApp(0);
		}
		return null;
	}	
}
