package com.anod.appwatcher.market;

import java.util.List;

import com.anod.appwatcher.utils.AppLog;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.AppType;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

public class AppsResponseLoader {
	private static final int PAGE_COUNT = 10;
	private static final int MAX_APPS = 100;
	private boolean mNetworkError;

	private String mQuery;
	private int mStartIndex;
	private boolean mHasNext;
	private MarketSession mMarketSession;

	class ResponseWrapper {
		AppsResponse response;
	}

	public AppsResponseLoader(MarketSession session, String query) {
		mMarketSession = session;
		mQuery = query;
		mStartIndex = 0;
		mHasNext = true;
		mNetworkError = false;
	}

	public String getQuery() {
		return mQuery;
	}
	public boolean hasNext() {
		return mHasNext;
	}
	public boolean moveToNext() {
		if (mHasNext) {
			mStartIndex += PAGE_COUNT;
			return true;
		}
		return false;
	}

	public boolean hasNetworkError() {
		return mNetworkError;
	}

	public List<App> load() {
		AppsRequest appsRequest = AppsRequest.newBuilder()
			.setQuery(mQuery)
			.setStartIndex(mStartIndex)
			.setEntriesCount(PAGE_COUNT)
			.setAppType(AppType.NONE)
			.setWithExtendedInfo(true)
//			.setOrderType(AppsRequest.OrderType.FEATURED)
			.setWithExtendedInfo(false).build();
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
			AppLog.e("Error search market", e);
			mHasNext = false;
			mNetworkError = true;
			return null;
		}
		List<App> apps = respWrapper.response.getAppList();
		int appCount = respWrapper.response.getEntriesCount();
		
		if (apps.size() < mStartIndex) {
			appCount = apps.size();
		}
		int totalCount = Math.min(appCount, MAX_APPS);
		int nextCount = mStartIndex + PAGE_COUNT;
		if (nextCount > totalCount) {
			mHasNext = false;
		}
		
		return apps;
	}

}
