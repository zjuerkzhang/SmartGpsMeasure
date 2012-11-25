package com.example.smartgpsmeasurer;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;


public class MyLocationListener implements LocationListener {
	public enum MyGpsStatus {
		DISABLED, FIXING, FIXED
	};
	
	static final String tag = "MyLocationListener";
	static final boolean s_log_switch = true;

	private LocationManager m_lm;
	
	private Location m_latest_loc;
	private MyGpsStatus m_gps_status;
	
	
	
	public MyLocationListener(LocationManager p_location_manager)
	{
		m_lm = p_location_manager;
		m_gps_status = MyGpsStatus.DISABLED;
	}
	
	public void registerLocationService(long minInterval, float minDistance)
	{		
		myDebugLog(tag, "registerLocationService");
		m_lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minInterval, minDistance, this);		
	}
	
	public void deregisterLocationService()
	{
		myDebugLog(tag, "deregisterLocationService");
		m_lm.removeUpdates(this);
	}
	
	public Location getLatestLocation()
	{
		myDebugLog(tag, "getLatestLocation");
		return m_latest_loc;
	}
	
	public MyGpsStatus getGpsStatus()
	{
		myDebugLog(tag, "getGpsStatus");
		return m_gps_status;
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		m_latest_loc = arg0;
	}

	@Override
	public void onProviderDisabled(String provider) {		
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
	
	
	private void myDebugLog(String p_tag, String p_msg) {
		if (s_log_switch)
		{
			Log.d(p_tag, p_msg);
		}
	}

}
