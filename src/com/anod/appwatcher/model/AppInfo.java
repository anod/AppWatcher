package com.anod.appwatcher.model;

import android.graphics.Bitmap;

import com.gc.android.market.api.model.Market.App;


public class AppInfo {
	
	public static final int STATUS_NORMAL = 0;	
	public static final int STATUS_UPDATED = 1;
	
	private int rowId;
	private String appId;
	private String packageName;
	private int versionNumber;
	private String versionName;
	private String title;
	private String creator;	
	private Bitmap icon;
	private int status;
	private long updateTime;
	
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
			String title, String creator, Bitmap icon, int status, long updateTime) {
		this.rowId = rowId;
		this.appId = appId;		
		this.packageName = pname;
		this.versionNumber = versionNumber;
		this.versionName = versionName;
		this.title = title;
		this.creator = creator;
		this.icon = icon;
		this.status = status;
		this.updateTime = updateTime;
	}
	
	/**
	 * Create loacl AppInfo from Market app object
	 * @param app
	 * @param icon
	 */
	public AppInfo(App app, Bitmap icon) {
		this.rowId = 0;
		this.appId = app.getId();    	
		this.packageName = app.getPackageName();
		this.title = app.getTitle();
		this.versionNumber = app.getVersionCode();  	    
		this.versionName = app.getVersion();
		this.creator = app.getCreator();
		this.status = STATUS_NORMAL;
		this.updateTime = 0;
		this.icon = icon;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRowId() {
		return rowId;
	}

	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
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
	 * @return the status
	 */
	public int getStatus() {
		return status;
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
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @param updateTime
	 */
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
}
