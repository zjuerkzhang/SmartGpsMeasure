package com.example.smartgpsmeasurer;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final String tag = "Main";
	static final boolean s_log_switch = true;

	Location m_my_location;
	MyGpsStatus m_my_gps_status;
	boolean m_first_gps_status = true;

	MyLocationListener m_my_loc_listener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		myDebugLog(tag, "onCreate");
		if(m_my_loc_listener == null)
		{
			myDebugLog(tag, "new instance of MyLocationListener in onCreate");
			m_my_loc_listener = new MyLocationListener((LocationManager)getSystemService(LOCATION_SERVICE),
					                                   this);
		}
		m_first_gps_status = true;
	}

	@Override
	public void onStart() {
		myDebugLog(tag, "onStart");
		super.onStart();
		if(m_my_loc_listener == null)
		{
			myDebugLog(tag, "new instance of MyLocationListener in onStart");
			m_my_loc_listener = new MyLocationListener((LocationManager)getSystemService(LOCATION_SERVICE),
					                                   this);
		}
		m_my_loc_listener.registerLocationService(1000, 0);
	}

	@Override
	public void onStop() {
		myDebugLog(tag, "onStop");
		if(m_my_loc_listener != null)
		{
			m_my_loc_listener.deregisterLocationService();
		}
		super.onStop();
	}
    
	public void onMyGpsStatusUpdate(MyGpsStatus p_gps_status)
	{
		myDebugLog(tag, "onMyGpsStatusUpdate");
		m_my_gps_status = p_gps_status;
		changeGpsStatusText(p_gps_status);
		if(m_first_gps_status && p_gps_status==MyGpsStatus.DISABLED)
		{
			m_first_gps_status = false;
			Toast.makeText(this, R.string.toast_gps_disable, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
	}
	
	public void onMyGpsLocationUpdate(Location p_location)
	{
		m_my_location = p_location;
		changeLocationStatusText(p_location);
	}
	
	public void onMyGpsSatStatusUpdate(int p_total_count, int p_used_count)
	{
		changeGpsSatStatusText(p_total_count, p_used_count);
	}
	
	private void changeGpsSatStatusText(int p_total_count, int p_used_count)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getResources().getString(R.string.txt_sat_max));
		sb.append(p_total_count);
		sb.append("\n");
		sb.append(getResources().getString(R.string.txt_use_max));
		sb.append(p_used_count);
		sb.append("\n");		
	}
	
	private void changeGpsStatusText(MyGpsStatus p_gps_status)
	{
		TextView tv = (TextView)findViewById(R.id.txt_gps_status);
		switch (p_gps_status)
		{
		case DISABLED:
			tv.setTextColor(getResources().getColor(R.color.red));
			tv.setText(this.getString(R.string.txt_gps_disable));
			break;
		case FIXING:
			tv.setTextColor(getResources().getColor(R.color.yellow));
			tv.setText(this.getString(R.string.txt_gps_fixing));
			break;
		case FIXED:
			tv.setTextColor(getResources().getColor(R.color.green));
			tv.setText(this.getString(R.string.txt_gps_fixed));
			break;
		default:
			break;
		}
		
	}
	
	private void changeLocationStatusText(Location p_location) {
		
		Time time = new Time();
		
		TextView tv = (TextView) findViewById(R.id.txt_gps_data);
		tv.setText("");
		
		StringBuilder sb = new StringBuilder();
		
		time.set(p_location.getTime());
		sb.append(this.getString(R.string.txt_timestamp));
		sb.append(time.format("%Y-%m-%d %H:%M:%S"));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_provider));
		sb.append(p_location.getProvider());
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_latitude));
		sb.append(p_location.getLatitude());
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_longitude));
		sb.append(p_location.getLongitude());
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_altitude));
		sb.append(p_location.getAltitude());
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_accuracy));
		sb.append(p_location.getAccuracy());
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_speed));
		sb.append(p_location.getSpeed());
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_delta_distance));
		if(m_my_location==null)
			sb.append(0);
		else
			sb.append(p_location.distanceTo(m_my_location));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_delta_distance));
		if(m_my_location==null)
			sb.append(0);
		else
			sb.append(GeoPoint.getDistance(new GeoPoint(m_my_location.getLatitude(), m_my_location.getLongitude()), 
					                       new GeoPoint(p_location.getLatitude(), p_location.getLongitude())));
		sb.append("\n");
		
		tv.setText(sb.toString());
		
		m_my_location = p_location;
	}
	
	private void myDebugLog(String p_tag, String p_msg) {
		if (s_log_switch)
		{
			Log.d(p_tag, p_msg);
			visibleDebugLog(p_msg);
		}
	}
	
	private void visibleDebugLog(String p_msg)
	{
		TextView tv = (TextView)findViewById(R.id.txt_log);
		
		StringBuilder sb = new StringBuilder();
		sb.append(tv.getText());
		sb.append("\n");
		sb.append(p_msg);		
		tv.setText(sb.toString());
	}
}
