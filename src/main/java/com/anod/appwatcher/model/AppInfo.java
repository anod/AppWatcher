package com.anod.appwatcher.model;

import android.graphics.Bitmap;

import com.gc.android.market.api.model.Market.App;


public class AppInfo extends AppInfoMetadata{

	private int rowId;
	private String packageName;
	private int versionNumber;
	private String versionName;
	private String title;
	private String creator;	
	private Bitmap icon;
	private long updateTime;
	private String priceText;
	private String priceCur;
	private Integer priceMicros;

	/**
	 * 
	 * @param rowId
	 * @param appId
	 * @param pname
	 * @param versionNumber
	 * @param versionName
	 * @param title
	 * @param creator
	 * @param icon
	 * @param status
	 * @param updateTime
	 */
	public AppInfo(int rowId, String appId, String pname, int versionNumber, String versionName,
			String title, String creator, Bitmap icon, int status, long updateTime, String priceText, String priceCur, Integer priceMicros) {
        super(appId,status);
		this.rowId = rowId;
		this.packageName = pname;
		this.versionNumber = versionNumber;
		this.versionName = versionName;
		this.title = title;
		this.creator = creator;
		this.icon = icon;
		this.updateTime = updateTime;

		this.priceText = priceText;
		this.priceCur = priceCur;
		this.priceMicros = priceMicros;
	}
	
	/**
	 * Create loacl AppInfo from Market app object
	 * @param app
	 * @param icon
	 */
	public AppInfo(App app, Bitmap icon) {
        super(app.getId(), STATUS_NORMAL);
        this.rowId = 0;
		this.appId = app.getId();
		this.packageName = app.getPackageName();
		this.title = app.getTitle();
		this.versionNumber = app.getVersionCode();
		this.versionName = app.getVersion();
		this.creator = app.getCreator();
		this.updateTime = 0;
		this.icon = icon;
		this.priceMicros = app.getPriceMicros();
		this.priceText = app.getPrice();
		this.priceCur = app.getPriceCurrency();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRowId() {
		return rowId;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}
	/**
	 * @return the versionNumber
	 */
	public int getVersionCode() {
		return versionNumber;
	}
	/**
	 * @return the versionName
	 */
	public String getVersionName() {
		return versionName;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}
	/**
	 * @return the icon
	 */
	public Bitmap getIcon() {
		return icon;
	}

	/**
	 * @return the last update time
	 */
	public long getUpdateTime() {
		return updateTime;
	}

	/**
	 * @param rowId
	 */
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	

	/**
	 * @param updateTime
	 */
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public String getPriceText() {
		return priceText;
	}

	public String getPriceCur() {
		return priceCur;
	}

	public Integer getPriceMicros() {
		return priceMicros;
	}
}
