package com.example.smartgpsmeasurer;

import java.util.Iterator;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MyLocationListener implements LocationListener {
	
	static final String tag = "MyLocationListener";
	static final boolean s_log_switch = true;

	private LocationManager m_lm;
	private MainActivity m_parent;
	
	private Location m_latest_loc;
	private MyGpsStatus m_gps_status;
	private int m_gps_used_sat_count;
	private int m_gps_total_sat_count;	
	
	private GpsStatus.Listener m_gsl = new GpsStatus.Listener() {
		
		@Override
		public void onGpsStatusChanged(int event) {			
			
			if(event==GpsStatus.GPS_EVENT_FIRST_FIX)
			{
				myDebugLog(tag, "onGpsStatusChanged");
				myDebugLog(tag, "===>GPS_EVENT_FIRST_FIX");
				updateGpsStatus(MyGpsStatus.FIXED);
			}
			else if(event==GpsStatus.GPS_EVENT_STARTED)
			{
				myDebugLog(tag, "onGpsStatusChanged");
				myDebugLog(tag, "===>GPS_EVENT_STARTED");
				updateGpsStatus(MyGpsStatus.FIXING);
			}
			else if(event==GpsStatus.GPS_EVENT_STOPPED)
			{
				myDebugLog(tag, "onGpsStatusChanged");
				myDebugLog(tag, "===>GPS_EVENT_STOPPED");
				updateGpsStatus(MyGpsStatus.DISABLED);
			}
			else if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS)
			{
				GpsStatus gs = m_lm.getGpsStatus(null);				
				
				int total_count = 0;
				int used_count = 0;
				Iterable<GpsSatellite> gps_sat_itb = gs.getSatellites();
				Iterator<GpsSatellite> gps_sat_itr = gps_sat_itb.iterator();
				while(gps_sat_itr.hasNext())
				{
					total_count++;
					GpsSatellite gps_sat = gps_sat_itr.next();					
					if(gps_sat.usedInFix())
					{
						used_count++;
					}					
				}
				
				updateGpsSatStatus(total_count, used_count);
				
			}
		}
	};
	
	public MyLocationListener(LocationManager p_location_manager, MainActivity p_parent_activity)
	{
		m_parent = p_parent_activity;
		m_lm = p_location_manager;
		m_gps_status = MyGpsStatus.DISABLED;
		m_gps_used_sat_count = 0;
		m_gps_total_sat_count = 0;
	}
	
	public void registerLocationService(long minInterval, float minDistance)
	{		
		myDebugLog(tag, "registerLocationService");
		m_lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minInterval, minDistance, this);
		m_lm.addGpsStatusListener(m_gsl);
	}
	
	public void deregisterLocationService()
	{
		myDebugLog(tag, "deregisterLocationService");
		m_lm.removeUpdates(this);
		m_lm.removeGpsStatusListener(m_gsl);
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
		updateGpsLocation(arg0);
	}

	@Override
	public void onProviderDisabled(String provider) {		
		myDebugLog(tag, "onProviderDisabled");
		updateGpsStatus(MyGpsStatus.DISABLED);
	}

	@Override
	public void onProviderEnabled(String provider) {
		myDebugLog(tag, "onProviderEnabled");
		updateGpsStatus(MyGpsStatus.FIXING);

	}

	@Override
	public void onStatusChanged(String p_provider, int p_status, Bundle p_extras) {
		myDebugLog(tag, "onStatusChanged");
		
		if(p_status==LocationProvider.AVAILABLE)
		{
			myDebugLog(tag, "===>AVAILABLE");
			updateGpsStatus(MyGpsStatus.FIXED);
		}
		else if(p_status==LocationProvider.TEMPORARILY_UNAVAILABLE)
		{	
			myDebugLog(tag, "===>TEMPORARILY_UNAVAILABLE");
			updateGpsStatus(MyGpsStatus.FIXING);
		}
		else if(p_status==LocationProvider.OUT_OF_SERVICE)
		{
			myDebugLog(tag, "===>OUT_OF_SERVICE");
		}

	}
	
	private void updateGpsStatus(MyGpsStatus p_gps_status)
	{
		m_gps_status = p_gps_status;
		m_parent.onMyGpsStatusUpdate(p_gps_status);
	}
	
	private void updateGpsSatStatus(int p_total_sat_count, int p_used_sat_count)
	{
		m_gps_total_sat_count = p_total_sat_count;
		m_gps_used_sat_count = p_used_sat_count;
		m_parent.onMyGpsSatStatusUpdate(p_total_sat_count, p_used_sat_count);
	}
	
	private void updateGpsLocation(Location p_location)
	{
		m_latest_loc = p_location;
		m_parent.onMyGpsLocationUpdate(p_location);
	}
	
	private void myDebugLog(String p_tag, String p_msg) {
		if (s_log_switch)
		{
			Log.d(p_tag, p_msg);
		}
	}

}

