package com.example.smartgpsmeasurer;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public enum MeasureState {
		IDLE, 
		FIRST_POINT, 
		WORK
	};
	
	public enum DistanceUnitType {
		METER,
		KILOMETER,
		FEET,
		MILE
	};
	
	public enum AreaUnitType {
		SQ_METER,
		HECTARE,
		ACRE,
		MU
	};
	
	static final String tag = "Main";
	static final boolean s_log_switch = true;
	static final boolean s_trial_version = true;
	static final double s_distance_limit_for_trial_version = 500.0;
	static final double s_area_limit_for_trial_version = 10000.0;

	Location m_first_location;
	Location m_latest_location;
	MyGpsStatus m_my_gps_status;
	boolean m_first_gps_status = true;
	MeasureState m_measure_state;
	double m_total_distance;
	DistanceUnitType m_distance_unit = DistanceUnitType.METER;	
	double m_total_area;
	AreaUnitType m_area_unit = AreaUnitType.SQ_METER;

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
		m_my_gps_status = MyGpsStatus.DISABLED;
		changeWorkState(MeasureState.IDLE);
		showMeasureData(0,0);
		m_total_distance = 0.0;
		m_total_area = 0.0;
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
		if(m_first_gps_status && p_gps_status==MyGpsStatus.DISABLED)
		{
			m_first_gps_status = false;
			Toast.makeText(this, R.string.toast_gps_disable, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
		
		changeGpsStatusText(p_gps_status);
		
		Button btn = (Button) findViewById(R.id.btn_gps_button);
		switch(p_gps_status)
		{
		case DISABLED:		
		case FIXING:
			if(m_measure_state == MeasureState.IDLE)
			{
				btn.setEnabled(false);
			}				
			break;
		case FIXED:
			btn.setEnabled(true);
			break;
		default:
			break;
		}
		m_my_gps_status = p_gps_status;
	}
	
	public void onMyGpsLocationUpdate(Location p_location)
	{
		showLocationInfo(p_location);
		
		if(m_measure_state == MeasureState.FIRST_POINT)
		{
			m_first_location = p_location;
			changeWorkState(MeasureState.WORK);
		}		
		else if(m_measure_state == MeasureState.WORK)
		{
			if(m_first_location!=null && m_latest_location != null)
			{
				m_total_distance += GeoPoint.getDistance(
						            new GeoPoint(m_latest_location.getLatitude(), m_latest_location.getLongitude()), 
	                                new GeoPoint(p_location.getLatitude(), p_location.getLongitude()));
				m_total_area += GeoPoint.getArea(
						            new GeoPoint(m_first_location.getLatitude(), m_first_location.getLongitude()),
						            new GeoPoint(m_latest_location.getLatitude(), m_latest_location.getLongitude()), 
			                        new GeoPoint(p_location.getLatitude(), p_location.getLongitude()) );	
				
				if(s_trial_version && 
				   (m_total_distance>s_distance_limit_for_trial_version || m_total_area>s_area_limit_for_trial_version) )
				{
					Button btn = (Button) findViewById(R.id.btn_gps_button);
					btn.performClick();
					Toast.makeText(this, R.string.toast_trial_hint, Toast.LENGTH_LONG).show();
				}
			}
		}
		else
		{}
		
		showMeasureData(m_total_distance, m_total_area);
		
		m_latest_location = p_location;
	}
	
	public void onMyGpsSatStatusUpdate(int p_total_count, int p_used_count)
	{
		changeGpsSatStatusText(p_total_count, p_used_count);
	}
		
	public void onButtonClick(View p_view)
	{
		myDebugLog(tag, "onButtonClick");
		Button btn = (Button) findViewById(R.id.btn_gps_button);
		if(m_measure_state == MeasureState.IDLE)
		{
			changeWorkState(MeasureState.FIRST_POINT);
			btn.setText(this.getString(R.string.btn_value_stop));
			btn.setTextColor(getResources().getColor(R.color.red));
			m_total_distance = 0.0;
			m_total_area = 0.0;
			m_first_location = null;
			showMeasureData(0,0);
		}
		else
		{
			changeWorkState(MeasureState.IDLE);
			btn.setText(this.getString(R.string.btn_value_start));
			btn.setTextColor(getResources().getColor(R.color.bk_color));
		}
	}
	
	public void onDistanceUnitTextClick(View p_view)
	{
		if(m_distance_unit == DistanceUnitType.METER)
			m_distance_unit = DistanceUnitType.KILOMETER;
		else if(m_distance_unit == DistanceUnitType.KILOMETER)
			m_distance_unit = DistanceUnitType.FEET;
		else if(m_distance_unit == DistanceUnitType.FEET)
			m_distance_unit = DistanceUnitType.MILE;
		else if(m_distance_unit == DistanceUnitType.MILE)
			m_distance_unit = DistanceUnitType.METER;
		else
		{}
		
		showMeasureData(m_total_distance, m_total_area);
	}
	
	public void onAreaUnitTextClick(View p_view)
	{
		if(m_area_unit == AreaUnitType.SQ_METER)
			m_area_unit = AreaUnitType.HECTARE;
		else if(m_area_unit == AreaUnitType.HECTARE)
			m_area_unit = AreaUnitType.ACRE;
		else if(m_area_unit == AreaUnitType.ACRE)
			m_area_unit = AreaUnitType.MU;
		else if(m_area_unit == AreaUnitType.MU)
			m_area_unit = AreaUnitType.SQ_METER;
		else
		{}
		
		showMeasureData(m_total_distance, m_total_area);
	}
	
	private void changeWorkState(MeasureState p_state)
	{
		m_measure_state = p_state;
		TextView tv = (TextView)findViewById(R.id.txt_id_running_state);
		switch(p_state)
		{
		case IDLE:
			tv.setText(this.getString(R.string.txt_value_running_idle));
			break;
		case FIRST_POINT:
			tv.setText(this.getString(R.string.txt_value_running_first_point));
			break;
		case WORK:
			tv.setText(this.getString(R.string.txt_value_running_measure));
			break;
		default:
			break;
		}
	}
	
	private void changeGpsSatStatusText(int p_total_count, int p_used_count)
	{
		TextView tv = (TextView)findViewById(R.id.txt_id_sat_status);
		StringBuilder sb = new StringBuilder();
		sb.append(p_used_count);
		sb.append("/");
		sb.append(p_total_count);
		tv.setText(sb.toString());
	}
	
	private void changeGpsStatusText(MyGpsStatus p_gps_status)
	{
		TextView tv = (TextView)findViewById(R.id.txt_id_gps_status);
		switch (p_gps_status)
		{
		case DISABLED:
			tv.setTextColor(getResources().getColor(R.color.red));
			tv.setText(this.getString(R.string.txt_value_gps_disable));
			break;
		case FIXING:
			tv.setTextColor(getResources().getColor(R.color.yellow));
			tv.setText(this.getString(R.string.txt_value_gps_fixing));
			break;
		case FIXED:
			tv.setTextColor(getResources().getColor(R.color.green));
			tv.setText(this.getString(R.string.txt_value_gps_fixed));
			break;
		default:
			break;
		}		
	}
	
	private void showLocationInfo(Location p_current_location)
	{
		TextView tv = (TextView) findViewById(R.id.txt_id_latitude);
		tv.setText(String.format("%.6f", p_current_location.getLatitude()));
		
		tv = (TextView) findViewById(R.id.txt_id_longitude);
		tv.setText(String.format("%.6f", p_current_location.getLongitude()));
	}
	/*
	private void changeLocationStatusText(Location p_current_location,
			                              Location p_last_location) {
		Time time = new Time();
		
		TextView tv = (TextView) findViewById(R.id.txt_gps_data);
		tv.setText("");
		
		StringBuilder sb = new StringBuilder();
		
		time.set(p_current_location.getTime());
		sb.append(this.getString(R.string.txt_timestamp));
		sb.append(time.format("%Y-%m-%d %H:%M:%S"));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_provider));
		sb.append(p_current_location.getProvider());
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_latitude));
		sb.append(String.format("%.6f", p_current_location.getLatitude()));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_longitude));
		sb.append(String.format("%.6f", p_current_location.getLongitude()));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_altitude));
		sb.append(String.format("%.2f", p_current_location.getAltitude()));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_accuracy));
		sb.append(String.format("%.2f", p_current_location.getAccuracy()));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_speed));
		sb.append(String.format("%.2f", p_current_location.getSpeed()));
		sb.append("\n");
		
		sb.append(this.getString(R.string.txt_delta_distance));
		if(p_current_location==null || p_last_location==null)
			sb.append(0);
		else
			sb.append(String.format("%.2f", 
					                GeoPoint.getDistance(
					                        new GeoPoint(p_last_location.getLatitude(), p_last_location.getLongitude()), 
					                        new GeoPoint(p_current_location.getLatitude(), p_current_location.getLongitude())) ) );
		sb.append("\n");
		
		tv.setText(sb.toString());			
		
	}
	*/
	private void showMeasureData(double p_distance, double p_area)
	{
		TextView tv = (TextView) findViewById(R.id.txt_id_distance);		
		tv.setText(String.format("%.2f", p_distance*getDistanceConversionRate(m_distance_unit)));
		tv = (TextView)findViewById(R.id.txt_id_distance_unit);
		tv.setText(getDistanceUnitText(m_distance_unit));
		
		tv = (TextView) findViewById(R.id.txt_id_area);		
		if(p_area < 0)
			p_area *= -1;
		tv.setText(String.format("%.2f", p_area*getAreaConversionRate(m_area_unit)));
		tv = (TextView)findViewById(R.id.txt_id_area_unit);
		tv.setText(getAreaUnitText(m_area_unit));
		
	}
	
	private void myDebugLog(String p_tag, String p_msg) {
		if (s_log_switch)
		{
			Log.d(p_tag, p_msg);
			//visibleDebugLog(p_msg);
		}
	}
	/*
	private void visibleDebugLog(String p_msg)
	{
		TextView tv = (TextView)findViewById(R.id.txt_log);
		
		StringBuilder sb = new StringBuilder();
		sb.append(tv.getText());
		sb.append("\n");
		sb.append(p_msg);		
		tv.setText(sb.toString());
	}
	*/
	
	private double getDistanceConversionRate(DistanceUnitType p_type)
	{
		switch(p_type)
		{
		case METER:
			return 1.0;
		case KILOMETER:
			return 0.001;
		case FEET:
			return 3.2808399;
		case MILE:
			return 0.0006213712;
		default:
			return 1.0;			
		}
	}
	
	private String getDistanceUnitText(DistanceUnitType p_type)
	{
		switch(p_type)
		{
		case METER:
			return this.getString(R.string.txt_value_distance_unit);
		case KILOMETER:
			return this.getString(R.string.txt_value_distance_km);
		case FEET:
			return this.getString(R.string.txt_value_distance_feet);
		case MILE:
			return this.getString(R.string.txt_value_distance_mile);
		default:
			return this.getString(R.string.txt_value_distance_unit);	
		}
	}
	
	private double getAreaConversionRate(AreaUnitType p_type)
	{
		switch(p_type)
		{
		case SQ_METER:
			return 1.0;
		case HECTARE:
			return 0.0001;
		case ACRE:
			return 0.00024710538;
		case MU:
			return 0.0015;
		default:
			return 1.0;			
		}
	}
	
	private String getAreaUnitText(AreaUnitType p_type)
	{
		switch(p_type)
		{
		case SQ_METER:
			return this.getString(R.string.txt_value_area_unit);
		case HECTARE:
			return this.getString(R.string.txt_value_area_hectare);
		case ACRE:
			return this.getString(R.string.txt_value_area_acre);
		case MU:
			return this.getString(R.string.txt_value_area_mu);
		default:
			return this.getString(R.string.txt_value_area_unit);	
		}
	}

}
