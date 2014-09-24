package com.anod.appwatcher.market;

import android.util.Log;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

import java.util.List;

public class AppLoader {
	private final MarketSession mMarketSession;
	private boolean mExtended;

	public AppLoader(MarketSession session) {
		mMarketSession = session;
	}

    public void setExtended(boolean extended) {
        mExtended = extended;
    }

	private static class ResponseWrapper {
		AppsResponse response;
	}
	
	public List<App> load(String... appsId) {
        final ResponseWrapper respWrapper = new ResponseWrapper();

        synchronized (mMarketSession) {

            for (int i=0; i<appsId.length; i++) {
            AppsRequest appsRequest = AppsRequest.newBuilder()
                    .setAppId(appsId[i])
                    .setWithExtendedInfo(mExtended)
                    .build();

                mMarketSession.append(appsRequest, new Callback<AppsResponse>() {
                    @Override
                    public void onResult(ResponseContext context, AppsResponse response) {
                        respWrapper.response = response;
                    }
                });

            mMarketSession.flush();
            }
        }

        if (respWrapper.response != null && respWrapper.response.getAppCount() > 0) {
			return respWrapper.response.getAppList();
		}
		return null;
	}

    public App loadOne(String appId) {
        List<App> apps = load(appId);
        if (apps == null || apps.size() == 0) {
            return null;
        }
        return apps.get(0);
    }
    public String loadRecentChanges(String appId) {
        App app = loadOne(appId);
        String changes = "";
        if (app.getExtendedInfo() != null) {
            changes = app.getExtendedInfo().getRecentChanges();
        }
        return changes;
    }
}
