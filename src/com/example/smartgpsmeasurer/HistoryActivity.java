package com.example.smartgpsmeasurer;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.smartgpsmeasurer.MainActivity.AreaUnit;
import com.example.smartgpsmeasurer.MainActivity.AreaUnit.AreaUnitType;
import com.example.smartgpsmeasurer.MainActivity.DistanceUnit;
import com.example.smartgpsmeasurer.MainActivity.DistanceUnit.DistanceUnitType;

public class HistoryActivity extends Activity{
	
	static final String tag = "HistoryActivity";
	
	class SpinnerLengthUnitSelectedListener implements OnItemSelectedListener{  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {  
        	int id = (int)arg3;
        	m_distance_unit = DistanceUnitType.values()[id];
        	showMeasureData(m_distance, m_area);
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
              
        }  
          
    }  
	
	class SpinnerAreaUnitSelectedListener implements OnItemSelectedListener{  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {  
        	int id = (int)arg3;
        	m_area_unit = AreaUnitType.values()[id];
        	showMeasureData(m_distance, m_area);
        	TextView tv = (TextView)findViewById(R.id.txt_id_his_unit_price);
    		tv.setText(String.format("%.2f", m_unit_price_per_m2/AreaUnit.getAreaConversionRate(m_area_unit)));
    		
    		tv = (TextView)findViewById(R.id.txt_id_his_unit_price_unit);
            tv.setText(getString(R.string.txt_static_money_unit)+"/"+AreaUnit.getAreaUnitText(getApplicationContext(), m_area_unit));
    		
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
              
        }  
          
    }  
	
	
	private List<GeoPoint> m_geo_points;
	private TrackView m_track_view;
	private double m_distance = 0;
	private double m_area = 0;
	private float m_unit_price_per_m2 = 0; 
	private DistanceUnitType m_distance_unit = DistanceUnitType.METER;
	private AreaUnitType m_area_unit = AreaUnitType.MU;
	
	private Spinner length_unit_spinner, area_unit_spinner;
	private ArrayAdapter length_unit_adapter, area_unit_adapter; 
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String db_filename = intent.getStringExtra(MainActivity.EXTRA_DBFILE);  
        MainActivity.myDebugLog(tag, "onCreate");
        MainActivity.myDebugLog(tag, db_filename);
        
        setContentView(R.layout.activity_history);        
        
        
        m_track_view = (TrackView)findViewById(R.id.view_id_his_trackview);
        
        DBManager dbm = new DBManager(this, db_filename);
        m_geo_points = dbm.query();
        dbm.closeDB();
        for(int i=0; i<m_geo_points.size(); i++)
        {
        	m_track_view.AddNewPoint(GeoPoint.convertGeo2Cart(m_geo_points.get(i)));
        }
        m_track_view.FlushView();  
    }
    
    @Override 
    public void onStart()
    {
    	MainActivity.myDebugLog(tag, "onStart");
    	super.onStart();
    	
    	SharedPreferences settings = getSharedPreferences(MainActivity.CONFIG_FILE, 0);
		int distance_unit_idx = settings.getInt(MainActivity.CONFIG_DIS_TYPE, 0);
		m_distance_unit = DistanceUnit.DistanceUnitType.values()[distance_unit_idx];
        
        length_unit_spinner = (Spinner)findViewById(R.id.spinner_id_his_distance_unit);
		length_unit_adapter = ArrayAdapter.createFromResource(this, R.array.length_units, android.R.layout.simple_spinner_item);  
		length_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		length_unit_spinner.setAdapter(length_unit_adapter);
		length_unit_spinner.setSelection(distance_unit_idx, true);
		length_unit_spinner.setOnItemSelectedListener(new SpinnerLengthUnitSelectedListener());		
		length_unit_spinner.setVisibility(View.VISIBLE);
		
		int area_unit_idx = settings.getInt(MainActivity.CONFIG_AREA_TYPE, 0);
		m_area_unit = AreaUnit.AreaUnitType.values()[area_unit_idx];
		
		area_unit_spinner = (Spinner)findViewById(R.id.spinner_id_his_area_unit);
		area_unit_adapter = ArrayAdapter.createFromResource(this, R.array.area_units, android.R.layout.simple_spinner_item);  
		area_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		area_unit_spinner.setAdapter(area_unit_adapter);
		area_unit_spinner.setSelection(area_unit_idx, true);
		area_unit_spinner.setOnItemSelectedListener(new SpinnerAreaUnitSelectedListener());		
		area_unit_spinner.setVisibility(View.VISIBLE);    
		
		m_unit_price_per_m2 = settings.getFloat(MainActivity.CONFIG_UNIT_PRICE, 0);
		TextView tv = (TextView)findViewById(R.id.txt_id_his_unit_price);
		tv.setText(String.format("%.2f", m_unit_price_per_m2/AreaUnit.getAreaConversionRate(m_area_unit)));
		
		tv = (TextView)findViewById(R.id.txt_id_his_unit_price_unit);
        tv.setText(this.getString(R.string.txt_static_money_unit)+"/"+AreaUnit.getAreaUnitText(getApplicationContext(), m_area_unit));		
        
		calculateDistanceAndArea();  
		showMeasureData(m_distance, m_area);
		
		tv = (TextView)findViewById(R.id.txt_id_his_total_price);
		tv.setText(String.format("%.2f", Math.abs(m_unit_price_per_m2*m_area)));
        
    }
    
    @Override
	public void onStop() {
    	MainActivity.myDebugLog(tag, "onStop");
    	
    	m_track_view.ClearAllPoint();
    	
    	SharedPreferences settings = getSharedPreferences(MainActivity.CONFIG_FILE, 0);
		SharedPreferences.Editor editor = settings.edit(); 
		editor.putInt(MainActivity.CONFIG_DIS_TYPE, DistanceUnit.enum2int(m_distance_unit));
		editor.putInt(MainActivity.CONFIG_AREA_TYPE, AreaUnit.enum2int(m_area_unit));
		editor.commit();
    	
    	super.onStop();
	}
    
    private void calculateDistanceAndArea()
    {
    	if(m_geo_points.size()<2)
    		return;
    	
    	GeoPoint firstPoint = m_geo_points.get(0);
    	for(int i=1; i<m_geo_points.size(); i++)
    	{
    		m_distance += GeoPoint.getDistance(m_geo_points.get(i-1), m_geo_points.get(i));
    		m_area += GeoPoint.getArea(firstPoint, m_geo_points.get(i-1), m_geo_points.get(i));
    	}
    }
    
    private void showMeasureData(double p_distance, double p_area)
	{
		TextView tv = (TextView) findViewById(R.id.txt_id_his_distance);		
		tv.setText(String.format("%.2f", p_distance*DistanceUnit.getDistanceConversionRate(m_distance_unit)));
		
		tv = (TextView) findViewById(R.id.txt_id_his_area);		
		if(p_area < 0)
			p_area *= -1;
		tv.setText(String.format("%.2f", p_area*AreaUnit.getAreaConversionRate(m_area_unit)));
	}   
    
    public void onHisCalButtonClick(View p_view)
	{
    	MainActivity.myDebugLog(tag, "onCalButtonClick");
		
		Intent intent = new Intent(this, CalcActivity.class);
		TextView tv = (TextView) findViewById(R.id.txt_id_his_area);
		intent.putExtra(MainActivity.EXTRA_MEASUREMENT, tv.getText().toString());
		intent.putExtra(MainActivity.EXTRA_UNIT, AreaUnit.enum2int(m_area_unit));
		startActivity(intent);
		finish();
	}

}
