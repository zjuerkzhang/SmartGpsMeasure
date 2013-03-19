package com.example.smartgpsmeasurer;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
	
	class SpinnerLengthUnitSelectedListener implements OnItemSelectedListener{  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {  
        	int id = (int)arg3;
        	m_distance_unit = DistanceUnitType.values()[id];
        	showMeasureData(m_total_distance, m_total_area);
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
              
        }  
          
    }  
	
	class SpinnerAreaUnitSelectedListener implements OnItemSelectedListener{  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {  
        	int id = (int)arg3;
        	m_area_unit = AreaUnitType.values()[id];
        	showMeasureData(m_total_distance, m_total_area);
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
              
        }  
          
    }  
	
	static final String tag = "Main";
	static final boolean s_log_switch = true;
	static final boolean s_trial_version = true;
	static final double s_distance_limit_for_trial_version = 500.0;
	static final double s_area_limit_for_trial_version = 10000.0;
	static final int s_point_array_length = 5;
	static final int s_point_array_avr_buff_len =3;
	static final double s_max_limit_for_single_valid_movement = 30.0;
	static final double s_min_limit_for_valid_movement = 3.0;
	public static final String EXTRA_MEASUREMENT = "com.example.smartgpsmeasurer.MEASUREMENT";
	public static final String EXTRA_UNIT = "com.example.smartgpsmeasurer.UNIT";

	GeoPoint m_first_point;
	GeoPoint m_latest_used_point;
	GeoPoint m_point_array[];
	ArrayList<GeoPoint> m_used_point_list = new ArrayList<GeoPoint>();
	int m_latest_point_idx = 0;
	int m_oldest_count_point_idx = 0;
	MyGpsStatus m_my_gps_status;
	boolean m_first_gps_status = true;
	MeasureState m_measure_state;
	double m_total_distance;
	DistanceUnitType m_distance_unit = DistanceUnitType.METER;	
	double m_total_area;
	AreaUnitType m_area_unit = AreaUnitType.SQ_METER;

	MyLocationListener m_my_loc_listener;
	
	private TrackView mTrackview;
	private Spinner length_unit_spinner, area_unit_spinner;
	private ArrayAdapter length_unit_adapter, area_unit_adapter; 
	
	private Button btn_start, btn_cal;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		myDebugLog(tag, "onCreate");
		btn_start = (Button)findViewById(R.id.btn_gps_button);
		btn_cal = (Button)findViewById(R.id.btn_cal_button);
		mTrackview = (TrackView)findViewById(R.id.view_id_trackview);
		
		length_unit_spinner = (Spinner)findViewById(R.id.spinner_id_distance_unit);
		length_unit_adapter = ArrayAdapter.createFromResource(this, R.array.length_units, android.R.layout.simple_spinner_item);  
		length_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		length_unit_spinner.setAdapter(length_unit_adapter);
		length_unit_spinner.setOnItemSelectedListener(new SpinnerLengthUnitSelectedListener());		
		length_unit_spinner.setVisibility(View.VISIBLE);
		
		area_unit_spinner = (Spinner)findViewById(R.id.spinner_id_area_unit);
		area_unit_adapter = ArrayAdapter.createFromResource(this, R.array.area_units, android.R.layout.simple_spinner_item);  
		area_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		area_unit_spinner.setAdapter(area_unit_adapter);
		area_unit_spinner.setOnItemSelectedListener(new SpinnerAreaUnitSelectedListener());		
		area_unit_spinner.setVisibility(View.VISIBLE);
		
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
		if(m_point_array == null)
		{
			m_point_array = new GeoPoint[s_point_array_length];
		}
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
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(1, 1, 1, this.getString(R.string.menu_history));
		menu.add(1, 2, 2, this.getString(R.string.menu_settings));
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 1:
			Intent intent = new Intent(this, DBFileList.class);
			startActivity(intent);
			break;
		case 2:
			StringBuilder sb = new StringBuilder("");
			sb.append(this.getString(R.string.txt_about_dialog_instruction_guide));
			sb.append("\n");
			sb.append(this.getString(R.string.txt_about_dialog_instruction));
			sb.append("\n");
			sb.append("\n");
			if(s_trial_version)
			{
				sb.append(this.getString(R.string.txt_about_dialog_trial_guide));
				sb.append("\n");
				sb.append(this.getString(R.string.txt_about_dialog_trial_info));
				sb.append("\n");	
				sb.append("\n");
			}
			sb.append(this.getString(R.string.txt_about_dialog_author_guide));
			sb.append(this.getString(R.string.txt_about_dialog_author));
			sb.append("\n");
			sb.append(this.getString(R.string.txt_about_dialog_email_guide));
			sb.append(this.getString(R.string.txt_about_dialog_email));
			
			new AlertDialog.Builder(this)
			.setTitle(this.getString(R.string.txt_about_dialog_title))
			.setMessage(sb.toString())
			.setPositiveButton(this.getString(R.string.txt_about_dialog_ok), null)
			.show();
			break;
		
		default:
	        //对没有处理的事件，交给父类来处理
	        return super.onOptionsItemSelected(item);
		}		
		
		return true;
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
		
		switch(p_gps_status)
		{
		case DISABLED:		
		case FIXING:
			if(m_measure_state == MeasureState.IDLE)
			{
				btn_start.setEnabled(false);
			}				
			break;
		case FIXED:
			btn_start.setEnabled(true);
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
			if(preparePointArrayBeforeMeasure(new GeoPoint(p_location.getLatitude(),p_location.getLongitude())))
			{
				getFirstPoint();
				changeWorkState(MeasureState.WORK);
			}			
		}		
		else if(m_measure_state == MeasureState.WORK)
		{
			processComingPoint(new GeoPoint(p_location.getLatitude(),p_location.getLongitude()));
			
			if(s_trial_version && 
			   (m_total_distance>s_distance_limit_for_trial_version || m_total_area>s_area_limit_for_trial_version) )
			{
				btn_start.performClick();
				Toast.makeText(this, R.string.toast_trial_hint, Toast.LENGTH_LONG).show();
			}
		}
		else
		{}
		
		showMeasureData(m_total_distance, m_total_area);		
	}
	
	public void onMyGpsSatStatusUpdate(int p_total_count, int p_used_count)
	{
		changeGpsSatStatusText(p_total_count, p_used_count);
	}
		
	public void onButtonClick(View p_view)
	{
		myDebugLog(tag, "onButtonClick");
		if(m_measure_state == MeasureState.IDLE)
		{
			mTrackview.ClearAllPoint();
			changeWorkState(MeasureState.FIRST_POINT);
			btn_start.setText(this.getString(R.string.btn_value_stop));
			btn_start.setTextColor(getResources().getColor(R.color.red));
			m_total_distance = 0.0;
			m_total_area = 0.0;
			//m_first_location = null;
			m_latest_point_idx = 0;
			m_oldest_count_point_idx = 0;
			showMeasureData(0,0);
			btn_cal.setVisibility(View.GONE);
			m_used_point_list.clear();
		}
		else
		{
			if(m_measure_state == MeasureState.WORK)
			{
				Calendar c = Calendar.getInstance();
				String curr_time_str = String.format("%04d-%02d-%02d--%02d-%02d-%02d.db", 
						c.get(Calendar.YEAR),
						c.get(Calendar.MONTH),
						c.get(Calendar.DATE),
						c.get(Calendar.HOUR),
						c.get(Calendar.MINUTE),
						c.get(Calendar.SECOND));
				DBManager dbm = new DBManager(this, curr_time_str);
				dbm.add(m_used_point_list);
				dbm.closeDB();
				btn_cal.setVisibility(View.VISIBLE);
			}
			changeWorkState(MeasureState.IDLE);
			btn_start.setText(this.getString(R.string.btn_value_start));
			btn_start.setTextColor(getResources().getColor(R.color.bk_color));			
		}
	}
	
	public void onCalButtonClick(View p_view)
	{
		myDebugLog(tag, "onCalButtonClick");
		
		Intent intent = new Intent(this, CalcActivity.class);
		TextView tv = (TextView) findViewById(R.id.txt_id_area);
		intent.putExtra(EXTRA_MEASUREMENT, tv.getText().toString());
		intent.putExtra(EXTRA_UNIT, getAreaUnitText(m_area_unit));
		startActivity(intent);
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
	
	private boolean preparePointArrayBeforeMeasure(GeoPoint p_geo_point)
	{
		boolean l_array_ready = false;
		
		if(m_latest_point_idx>0 && 
		   GeoPoint.getDistance(m_point_array[m_latest_point_idx-1], p_geo_point)>s_max_limit_for_single_valid_movement)
		{
			return l_array_ready;
		}
		
		m_point_array[m_latest_point_idx] = p_geo_point;
		m_point_array[m_latest_point_idx].setGeoPointUsed(false);
		m_latest_point_idx++;
		
		if(m_latest_point_idx>=s_point_array_length)
		{
			m_latest_point_idx = s_point_array_length - 1;
			m_oldest_count_point_idx = s_point_array_avr_buff_len - 1;
			l_array_ready = true;
		}
		
		return l_array_ready;
	}
	
	private void getFirstPoint()
	{
		double total_latitude = 0.0;
		double total_longitude = 0.0;
		int i;
		
		for(i=0; i<s_point_array_length; i++)
		{
			total_latitude += m_point_array[i].getLatitude();
			total_longitude += m_point_array[i].getLongitude();
		}
		
		m_first_point = new GeoPoint(total_latitude/s_point_array_length,
				                     total_longitude/s_point_array_length,
				                     false);
		m_latest_used_point = m_first_point;
	}
	
	private int getPreviousPointIndex(int p_current_idx)
	{   
	    if(0==p_current_idx)
	    	p_current_idx = s_point_array_length-1;
	    else
	    	p_current_idx--;
	         
	    return p_current_idx;
	}
	
	private int getNextPointIndex(int p_current_idx)
	{   
		p_current_idx++;
	    if(s_point_array_length==p_current_idx)
	    	p_current_idx = 0;
	         
	    return p_current_idx;
	}
	
	private void processComingPoint(GeoPoint p_point)
	{
		GeoPoint l_base;
		double l_base_latitude = 0.0;
		double l_base_longitude = 0.0;
		int i, j;
		
		if(GeoPoint.getDistance(m_point_array[m_latest_point_idx], p_point)>s_max_limit_for_single_valid_movement)
		{
			return;
		}
		
		j = m_oldest_count_point_idx;
		for(i=0; i<s_point_array_avr_buff_len; i++)
		{
			if(m_point_array[j].isGeoPointUsed())
			{
				l_base = m_point_array[j];
				break;
			}
			l_base_latitude += m_point_array[j].getLatitude();
			l_base_longitude += m_point_array[j].getLongitude();
			j = getPreviousPointIndex(j);
		}
		if(i>=s_point_array_avr_buff_len)
		{
			l_base = new GeoPoint(l_base_latitude/s_point_array_avr_buff_len,
					              l_base_longitude/s_point_array_avr_buff_len);			
		}
		else
		{
			l_base = m_point_array[j];
		}
		m_latest_point_idx = getNextPointIndex(m_latest_point_idx);
		m_oldest_count_point_idx = getNextPointIndex(m_oldest_count_point_idx);
		m_point_array[m_latest_point_idx] = p_point;
		m_point_array[m_latest_point_idx].setGeoPointUsed(false);
		
		if(GeoPoint.getDistance(p_point, l_base)>s_min_limit_for_valid_movement)
		{
			m_total_distance += GeoPoint.getDistance(p_point, m_latest_used_point);
			m_total_area += GeoPoint.getArea(m_first_point, m_latest_used_point, p_point);
			
			m_point_array[m_latest_point_idx].setGeoPointUsed(true);
			m_latest_used_point = p_point;	
			m_used_point_list.add(p_point);
			
			mTrackview.AddNewPoint(GeoPoint.convertGeo2Cart(p_point));
		}		
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
		//tv = (TextView)findViewById(R.id.txt_id_distance_unit);
		//tv.setText(getDistanceUnitText(m_distance_unit));
		
		tv = (TextView) findViewById(R.id.txt_id_area);		
		if(p_area < 0)
			p_area *= -1;
		tv.setText(String.format("%.2f", p_area*getAreaConversionRate(m_area_unit)));
		//tv = (TextView)findViewById(R.id.txt_id_area_unit);
		//tv.setText(getAreaUnitText(m_area_unit));
		
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
