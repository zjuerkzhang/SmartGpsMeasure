package com.example.smartgpsmeasurer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
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
	
	
	
	public static class DistanceUnit
	{
		public enum DistanceUnitType {
			METER,
			KILOMETER,
			FEET,
			MILE
		};
		
		public static int enum2int(DistanceUnitType p_unit_type)
		{
			switch(p_unit_type)
			{
			case METER:
				return 0;
			case KILOMETER:
				return 1;
			case FEET:
				return 2;
			case MILE:
				return 3;
			default:
				return 0;			
			}
		}
		
		public static double getDistanceConversionRate(DistanceUnitType p_type)
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
		
		public static String getDistanceUnitText(Context cont, DistanceUnitType p_type)
		{
			switch(p_type)
			{
			case METER:
				return cont.getString(R.string.txt_value_distance_unit);
			case KILOMETER:
				return cont.getString(R.string.txt_value_distance_km);
			case FEET:
				return cont.getString(R.string.txt_value_distance_feet);
			case MILE:
				return cont.getString(R.string.txt_value_distance_mile);
			default:
				return cont.getString(R.string.txt_value_distance_unit);	
			}
		}
	}
	
	public static class AreaUnit
	{
		public enum AreaUnitType {
			SQ_METER, 
			HECTARE, 
			ACRE, 
			MU
		};
		
		public static int enum2int(AreaUnitType p_unit_type)
		{
			switch(p_unit_type)
			{
			case SQ_METER:
				return 0;
			case HECTARE:
				return 1;
			case ACRE:
				return 2;
			case MU:
				return 3;
			default:
				return 0;			
			}
		}
		
		public static double getAreaConversionRate(AreaUnitType p_type)
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
		
		public static String getAreaUnitText(Context cont, AreaUnitType p_type)
		{
			switch(p_type)
			{
			case SQ_METER:
				return cont.getString(R.string.txt_value_area_unit);
			case HECTARE:
				return cont.getString(R.string.txt_value_area_hectare);
			case ACRE:
				return cont.getString(R.string.txt_value_area_acre);
			case MU:
				return cont.getString(R.string.txt_value_area_mu);
			default:
				return cont.getString(R.string.txt_value_area_unit);	
			}
		}
	
	}
	
	class SpinnerLengthUnitSelectedListener implements OnItemSelectedListener{  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {  
        	int id = (int)arg3;
        	m_distance_unit = DistanceUnit.DistanceUnitType.values()[id];
        	showMeasureData(m_total_distance, m_total_area);
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
              
        }  
          
    }  
	
	class SpinnerAreaUnitSelectedListener implements OnItemSelectedListener{  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {  
        	int id = (int)arg3;
        	m_area_unit = AreaUnit.AreaUnitType.values()[id];
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
	public static final String EXTRA_DBFILE = "com.example.smartgpsmeasurer.DBFILE";
	static final String validFilePath = "/sgm/oicq.bin";
	
	public static final String CONFIG_FILE = "configfile";
	public static final String CONFIG_DIS_TYPE = "dis_unit";
	public static final String CONFIG_AREA_TYPE = "area_unit";
	
	static final String gps_status_app_pac_name = "com.eclipsim.gpsstatus2";
	static final int    magic_num = 89;

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
	DistanceUnit.DistanceUnitType m_distance_unit = DistanceUnit.DistanceUnitType.METER;	
	double m_total_area;
	AreaUnit.AreaUnitType m_area_unit = AreaUnit.AreaUnitType.SQ_METER;

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
		
		SharedPreferences settings = getSharedPreferences(CONFIG_FILE, 0);
		int distance_unit_idx = settings.getInt(CONFIG_DIS_TYPE, 0);
		m_distance_unit = DistanceUnit.DistanceUnitType.values()[distance_unit_idx];
		
		length_unit_spinner = (Spinner)findViewById(R.id.spinner_id_distance_unit);
		length_unit_adapter = ArrayAdapter.createFromResource(this, R.array.length_units, android.R.layout.simple_spinner_item);  
		length_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		length_unit_spinner.setAdapter(length_unit_adapter);
		length_unit_spinner.setSelection(distance_unit_idx);
		length_unit_spinner.setOnItemSelectedListener(new SpinnerLengthUnitSelectedListener());		
		length_unit_spinner.setVisibility(View.VISIBLE);
		
		int area_unit_idx = settings.getInt(CONFIG_AREA_TYPE, 0);
		m_distance_unit = DistanceUnit.DistanceUnitType.values()[area_unit_idx];
		
		area_unit_spinner = (Spinner)findViewById(R.id.spinner_id_area_unit);
		area_unit_adapter = ArrayAdapter.createFromResource(this, R.array.area_units, android.R.layout.simple_spinner_item);  
		area_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		area_unit_spinner.setAdapter(area_unit_adapter);
		area_unit_spinner.setSelection(area_unit_idx);
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
		
		if(!checkAppValid())
		{
			myDebugLog(tag, "not valid equipment, quit");
			Intent intent = new Intent(this, InvalidActivity.class);
			startActivity(intent);
			MainActivity.this.finish();
		}
		
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
		
		SharedPreferences settings = getSharedPreferences(CONFIG_FILE, 0);
		SharedPreferences.Editor editor = settings.edit(); 
		editor.putInt(CONFIG_DIS_TYPE, DistanceUnit.enum2int(m_distance_unit));
		editor.putInt(CONFIG_AREA_TYPE, AreaUnit.enum2int(m_area_unit));
		editor.commit();
		
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
	
	public void onGpsStatusClick(View p_view)
	{
		myDebugLog(tag, "onGpsStatusClick");
		PackageManager packageManager = this.getPackageManager(); 
		Intent intent=new Intent(); 
		intent =packageManager.getLaunchIntentForPackage(gps_status_app_pac_name);
		if(intent != null)
			startActivity(intent); 
		else
			Toast.makeText(this, R.string.gps_status_app_not_installed, Toast.LENGTH_SHORT).show();
	}
	
	public void onCalButtonClick(View p_view)
	{
		myDebugLog(tag, "onCalButtonClick");
		
		Intent intent = new Intent(this, CalcActivity.class);
		TextView tv = (TextView) findViewById(R.id.txt_id_area);
		intent.putExtra(EXTRA_MEASUREMENT, tv.getText().toString());
		intent.putExtra(EXTRA_UNIT, AreaUnit.getAreaUnitText(this, m_area_unit));
		startActivity(intent);
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
		
		m_used_point_list.add(m_first_point);
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
			mTrackview.FlushView();
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
	
	private void showMeasureData(double p_distance, double p_area)
	{
		TextView tv = (TextView) findViewById(R.id.txt_id_distance);		
		tv.setText(String.format("%.2f", p_distance*DistanceUnit.getDistanceConversionRate(m_distance_unit)));
		//tv = (TextView)findViewById(R.id.txt_id_distance_unit);
		//tv.setText(getDistanceUnitText(m_distance_unit));
		
		tv = (TextView) findViewById(R.id.txt_id_area);		
		if(p_area < 0)
			p_area *= -1;
		tv.setText(String.format("%.2f", p_area*AreaUnit.getAreaConversionRate(m_area_unit)));
		//tv = (TextView)findViewById(R.id.txt_id_area_unit);
		//tv.setText(getAreaUnitText(m_area_unit));
		
	}
	
	private boolean checkAppValid()
	{
		boolean l_valid = false;
		
		String sDStateString = android.os.Environment.getExternalStorageState(); 
		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED) ||
		    sDStateString.endsWith(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)	) 
		{
			File SDFile = android.os.Environment.getExternalStorageDirectory();  

		    File myFile = new File(SDFile.getAbsolutePath() + validFilePath);  
		  
		    // 判断文件是否存在  
		    if (myFile.exists()) 
		    {  
		        try 
		        {  
		        	String dev_modle;
		        	String dev_mac_addr = "";
		        	Build bd = new Build();		        
		        	dev_modle = bd.MODEL;		        	
		        	
		        	WifiManager wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		        	WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
		        	if (null != info) {
		        		dev_mac_addr = info.getMacAddress();		        		
		        	}
		        	myDebugLog(tag, "MODEL&MAC: "+dev_modle+"-"+dev_mac_addr);
		        	
		        	byte[] dev_buff = getChecksumByDevInfo(dev_modle, dev_mac_addr);
		        	
		            
		            FileInputStream inputStream = new FileInputStream(myFile);  
		            byte[] buffer = new byte[32];  
		            inputStream.read(buffer);  
		            inputStream.close();  
		            
		            for(int i=0; i<32; i++)
		            {
		            	if(dev_buff[i]!=buffer[i])
		            		return false;
		            }
		            
		  
		        } catch (Exception e) {  
		        }// end of try
		        
		        l_valid = true;
		    }
		}
		
		return l_valid;
	}
	
	private byte[] getChecksumByDevInfo(String dev_mod, String dev_mac)
	{
		byte[] ret = new byte[32];		
		byte[] mod = dev_mod.getBytes();
		
		for(int i=0; i<ret.length; i++)
			ret[i] = 0;
		
		for(int i=0; i<mod.length; i++)
			ret[i] = mod[i];
		
		String mac_sub_str[] = dev_mac.split(":");
		
		for(int i=0; i<mac_sub_str.length; i++)
		{
			int c = Integer.valueOf(mac_sub_str[i], 16);
			c = c*magic_num;
			ret[mod.length + 2*i] = (byte)(c/256);
			ret[mod.length + 2*i +1] = (byte)(c%256);
		}
		
		return ret;
	}
	
	static public void myDebugLog(String p_tag, String p_msg) {
		if (s_log_switch)
		{
			Log.d(p_tag, p_msg);
			//visibleDebugLog(p_msg);
		}
	}
	

}
