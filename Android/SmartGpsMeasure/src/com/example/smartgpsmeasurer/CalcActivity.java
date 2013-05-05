package com.example.smartgpsmeasurer;

import com.example.smartgpsmeasurer.MainActivity.AreaUnit;
import com.example.smartgpsmeasurer.MainActivity.DistanceUnit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalcActivity extends Activity {
	
	static final String tag = "CalcActivity";
	
	private String m_unit_price ="";
	AreaUnit.AreaUnitType m_area_unit = AreaUnit.AreaUnitType.SQ_METER;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        MainActivity.myDebugLog(tag, "onCreate");
        
        Intent intent = getIntent();
        String measurement = intent.getStringExtra(MainActivity.EXTRA_MEASUREMENT);
        int unit = intent.getIntExtra(MainActivity.EXTRA_UNIT, 0);
        m_area_unit = AreaUnit.AreaUnitType.values()[unit];
        String unit_str = AreaUnit.getAreaUnitText(getApplicationContext(), m_area_unit);
     
        setContentView(R.layout.activity_calc);
        
        TextView tv = (TextView)findViewById(R.id.txt_id_cal_area);
        tv.setText(measurement);
        tv = (TextView)findViewById(R.id.txt_id_cal_area_unit);
        tv.setText(unit_str);
        tv = (TextView)findViewById(R.id.txt_id_cal_unit_price_unit);
        tv.setText(this.getString(R.string.txt_static_money_unit)+"/"+unit_str);
    }
	
	@Override
	public void onStart()
	{
		MainActivity.myDebugLog(tag, "onStart");
		super.onStart();
		
		SharedPreferences settings = getSharedPreferences(MainActivity.CONFIG_FILE, 0);
		float unit_price_per_m2 = settings.getFloat(MainActivity.CONFIG_UNIT_PRICE, 0);
		m_unit_price = String.format("%.2f", unit_price_per_m2/AreaUnit.getAreaConversionRate(m_area_unit));
		
		MainActivity.myDebugLog(tag, "m_area_unit: "+m_area_unit.toString());
		MainActivity.myDebugLog(tag, String.format("unit_price_per_m2: %f", unit_price_per_m2));
		
		TextView tv = (TextView)findViewById(R.id.txt_id_cal_unit_price);
		tv.setText(m_unit_price);
	}
	
	@Override
	public void onStop()
	{
		MainActivity.myDebugLog(tag, "onStop");
		super.onStop();
		
		if(m_unit_price != "")
		{
			float unit_price_per_m2 = Float.parseFloat(m_unit_price)*((float)AreaUnit.getAreaConversionRate(m_area_unit));
			
			SharedPreferences settings = getSharedPreferences(MainActivity.CONFIG_FILE, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putFloat(MainActivity.CONFIG_UNIT_PRICE, unit_price_per_m2);
			editor.commit();
		}
	}
	
	public void onNumPadClick(View p_view)
	{
		//MainActivity.myDebugLog(tag, "onNumPadClick: button '" + ((Button)p_view).getText().toString() + "' is clicked");
		
		if(((Button)p_view).getText() == this.getString(R.string.btn_cal_pad_num_C))		
			m_unit_price = "";
		else if(((Button)p_view).getText() == this.getString(R.string.btn_cal_pad_num_dot))
		{
			if(m_unit_price.indexOf(this.getString(R.string.btn_cal_pad_num_dot)) == -1 && m_unit_price!="")
				m_unit_price = m_unit_price + ((Button)p_view).getText();
		}
		else
			m_unit_price = m_unit_price + ((Button)p_view).getText();			
		
		TextView tv = (TextView)findViewById(R.id.txt_id_cal_unit_price);
		tv.setText(m_unit_price);
		
		if(m_unit_price == "")
		{
			tv = (TextView)findViewById(R.id.txt_id_cal_total_price);
			tv.setText("");
		}
	}
	
	public void onCalculate(View p_view)
	{
		MainActivity.myDebugLog(tag, "onCalculate");
		
		if( m_unit_price!="" )
		{
			TextView tv = (TextView)findViewById(R.id.txt_id_cal_area);
			double area = Double.parseDouble(tv.getText().toString());
			double unit_price = Double.parseDouble(m_unit_price);

			double total_price = area*unit_price;
			tv = (TextView)findViewById(R.id.txt_id_cal_total_price);
			tv.setText(String.format("%.2f", total_price));
		}
	}

}
