package com.anod.appwatcher.market;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.gc.android.market.api.MarketSession;

public class MarketSessionHelper {
	public static final String EXTRA_TOKEN = "extra_token";
	private Context mContext;
	

	public MarketSessionHelper(Context context) {
		mContext = context;
	}
	
	/**
	 * @param deviceId
	 */
	public MarketSession create(String deviceId, String authSubToken) {
		MarketSession session = new MarketSession();
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
		session.setOperator(
			tm.getNetworkOperatorName(), 
			tm.getSimOperatorName(), 
			tm.getNetworkOperator(),
			tm.getSimOperator()
		);
		
		String deviceAndSdkVersion = Build.PRODUCT + ":" + Build.VERSION.SDK_INT;
		session.getContext().setDeviceAndSdkVersion(deviceAndSdkVersion);
		session.getContext().setAndroidId(deviceId);
		
		if (authSubToken !=null) {
			session.setAuthSubToken(authSubToken);
		}
		
		return session;
	}
	


}
