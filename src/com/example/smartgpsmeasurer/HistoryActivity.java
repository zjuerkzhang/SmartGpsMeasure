package com.example.smartgpsmeasurer;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
              
        }  
          
    }  
	
	
	private List<GeoPoint> m_geo_points;
	private TrackView m_track_view;
	private double m_distance = 0;
	private double m_area = 0;
	private DistanceUnitType m_distance_unit = DistanceUnitType.METER;
	private AreaUnitType m_area_unit = AreaUnitType.MU;
	
	private Spinner length_unit_spinner, area_unit_spinner;
	private ArrayAdapter length_unit_adapter, area_unit_adapter; 
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String db_filename = intent.getStringExtra(MainActivity.EXTRA_DBFILE);        
        Log.v("HistoryActivity", db_filename);
        
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
        
        calculateDistanceAndArea();        
        
        length_unit_spinner = (Spinner)findViewById(R.id.spinner_id_his_distance_unit);
		length_unit_adapter = ArrayAdapter.createFromResource(this, R.array.length_units, android.R.layout.simple_spinner_item);  
		length_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		length_unit_spinner.setAdapter(length_unit_adapter);
		length_unit_spinner.setOnItemSelectedListener(new SpinnerLengthUnitSelectedListener());		
		length_unit_spinner.setVisibility(View.VISIBLE);
		
		area_unit_spinner = (Spinner)findViewById(R.id.spinner_id_his_area_unit);
		area_unit_adapter = ArrayAdapter.createFromResource(this, R.array.area_units, android.R.layout.simple_spinner_item);  
		area_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		area_unit_spinner.setAdapter(area_unit_adapter);
		area_unit_spinner.setOnItemSelectedListener(new SpinnerAreaUnitSelectedListener());		
		area_unit_spinner.setVisibility(View.VISIBLE);
		
    }
    
    @Override
	public void onStop() {
    	m_track_view.ClearAllPoint();
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
    

}
