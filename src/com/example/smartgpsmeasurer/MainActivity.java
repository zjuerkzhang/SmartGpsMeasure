package com.example.smartgpsmeasurer;

import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {

	static final String tag = "Main";
	static final boolean s_log_switch = true;

	LocationManager m_lm;
	Location m_location;

	private GpsStatus.Listener m_gsl = new GpsStatus.Listener() {
		
		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Auto-generated method stub			
			
			TextView tv = (TextView)findViewById(R.id.txt_gps_status);
			if(event==GpsStatus.GPS_EVENT_FIRST_FIX)
			{
				myDebugLog(tag, "onGpsStatusChanged");
				myDebugLog(tag, "===>GPS_EVENT_FIRST_FIX");
				tv.setTextColor(getResources().getColor(R.color.green));
				tv.setText(getResources().getString(R.string.txt_gps_fixed));
			}
			else if(event==GpsStatus.GPS_EVENT_STARTED)
			{
				myDebugLog(tag, "onGpsStatusChanged");
				myDebugLog(tag, "===>GPS_EVENT_STARTED");
				tv.setTextColor(getResources().getColor(R.color.yellow));
				tv.setText(getResources().getString(R.string.txt_gps_fixing));
			}
			else if(event==GpsStatus.GPS_EVENT_STOPPED)
			{
				myDebugLog(tag, "onGpsStatusChanged");
				myDebugLog(tag, "===>GPS_EVENT_STOPPED");
				tv.setTextColor(getResources().getColor(R.color.red));
				tv.setText(getResources().getString(R.string.txt_gps_disable));
			}
			else if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS)
			{
				GpsStatus gs = m_lm.getGpsStatus(null);
				
				TextView tv_sat = (TextView) findViewById(R.id.txt_sat_data);
				tv_sat.setText("");
				
				StringBuilder sb = new StringBuilder();
				sb.append(getResources().getString(R.string.txt_sat_max));
				sb.append(gs.getMaxSatellites());
				sb.append("\n");
				
				int count = 0;
				Iterable<GpsSatellite> gps_sat_itb = gs.getSatellites();
				Iterator<GpsSatellite> gps_sat_itr = gps_sat_itb.iterator();
				while(gps_sat_itr.hasNext())
				{					
					GpsSatellite gps_sat = gps_sat_itr.next();
					sb.append("#");
					sb.append(gps_sat.getPrn());
					sb.append(": SNR=");
					sb.append(gps_sat.getSnr());
					if(gps_sat.usedInFix())
					{
						sb.append(" Used");
						count++;
					}
					sb.append("\n");
				}
				
				sb.append(getResources().getString(R.string.txt_use_max));
				sb.append(count);
				tv_sat.setText(sb.toString());
				
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		myDebugLog(tag, "onCreate");
		m_lm = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	@Override
	public void onStart() {
		myDebugLog(tag, "onStart");
		super.onStart();
		m_lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
		m_lm.addGpsStatusListener(m_gsl);
	}

	@Override
	public void onStop() {
		myDebugLog(tag, "onStop");
		m_lm.removeUpdates(this);
		m_lm.removeGpsStatusListener(m_gsl);
		super.onStop();
	}
    /*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
    */
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

	@Override
	public void onLocationChanged(Location p_location) {
		myDebugLog(tag, "onLocationChanged");		
		
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
		if(m_location==null)
			sb.append(0);
		else
			sb.append(p_location.distanceTo(m_location));
		sb.append("\n");
		
		tv.setText(sb.toString());
		
		m_location = p_location;
	}

	@Override
	public void onProviderDisabled(String p_provider) {
		myDebugLog(tag, "onProviderDisabled");
		
		TextView tv = (TextView)findViewById(R.id.txt_gps_status);
		tv.setTextColor(getResources().getColor(R.color.red));
		tv.setText(this.getString(R.string.txt_gps_disable));
		Toast.makeText(this, R.string.toast_gps_disable, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	@Override
	public void onProviderEnabled(String p_provider) {
		myDebugLog(tag, "onProviderEnabled");
		
		TextView tv = (TextView)findViewById(R.id.txt_gps_status);
		tv.setTextColor(getResources().getColor(R.color.yellow));
		tv.setText(this.getString(R.string.txt_gps_fixing));
	}

	@Override
	public void onStatusChanged(String p_provider, int p_status, Bundle p_extras) {
		myDebugLog(tag, "onStatusChanged");
		
		TextView tv = (TextView)findViewById(R.id.txt_gps_status);
		if(p_status==LocationProvider.AVAILABLE)
		{
			myDebugLog(tag, "===>AVAILABLE");
			tv.setTextColor(getResources().getColor(R.color.green));
			tv.setText(this.getString(R.string.txt_gps_fixed));
		}
		else if(p_status==LocationProvider.TEMPORARILY_UNAVAILABLE)
		{	
			myDebugLog(tag, "===>TEMPORARILY_UNAVAILABLE");
			tv.setTextColor(getResources().getColor(R.color.yellow));
			tv.setText(this.getString(R.string.txt_gps_fixing));
		}
		else if(p_status==LocationProvider.OUT_OF_SERVICE)
		{
			myDebugLog(tag, "===>OUT_OF_SERVICE");
		}
	}

}
