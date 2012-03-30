package com.anod.appwatcher.market;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.anod.appwatcher.utils.ImageLoader;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.GetImageRequest;
import com.gc.android.market.api.model.Market.GetImageRequest.AppImageUsage;
import com.gc.android.market.api.model.Market.GetImageResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

public class AppIconLoader extends ImageLoader {
	final private MarketSession mMarketSession;

	private class IconWrapper {
		byte[] icon = null;
	}
	
	public AppIconLoader(final MarketSession session) {
		super();
		mMarketSession = session;
	}
	
	public void precacheIcon(String appId) {
        Bitmap bmp=loadBitmap(appId);
        if (bmp != null) {
        	cacheImage(appId, bmp);
        }
	}
	
	
	@Override
	protected Bitmap loadBitmap(String imgUID) {
		GetImageRequest imgReq = GetImageRequest
		 	.newBuilder()
		 	.setAppId(imgUID)
		 	.setImageUsage(AppImageUsage.ICON)
		 	.build();
	
		final IconWrapper wrapper = new IconWrapper();
		synchronized (mMarketSession) {
			mMarketSession.append(imgReq, new Callback<GetImageResponse>() {
				@Override
				public void onResult(ResponseContext context, GetImageResponse response) {
					try {
						wrapper.icon = response.getImageData().toByteArray();
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			mMarketSession.flush();
		}
		 
		try {
			 return BitmapFactory.decodeByteArray(wrapper.icon, 0, wrapper.icon.length);
		} catch (Exception e) {
			 return null;
		}
	}	
}
