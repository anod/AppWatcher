package com.anod.appwatcher.model;

import android.graphics.Bitmap;


public class AppInfo {
	private String appId;
	private String packageName;
	private int versionNumber;
	private String versionName;
	private String title;
	private String creator;	
	private Bitmap icon;
	private int status;
	public AppInfo(String appId, String pname, int versionNumber, String versionName,
			String title, String creator, Bitmap icon, int status) {
		this.appId = appId;		
		this.packageName = pname;
		this.versionNumber = versionNumber;
		this.versionName = versionName;
		this.title = title;
		this.creator = creator;
		this.icon = icon;
		this.status = status;
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
	public int getVersionNumber() {
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
	
	
}
