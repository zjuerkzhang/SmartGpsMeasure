package com.example.smartgpsmeasurer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalcActivity extends Activity {
	
	static final String tag = "CalcActivity";
	static final boolean s_log_switch = true;
	
	private String m_unit_price ="";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String measurement = intent.getStringExtra(MainActivity.EXTRA_MEASUREMENT);
        String unit = intent.getStringExtra(MainActivity.EXTRA_UNIT);
     
        setContentView(R.layout.activity_calc);
        
        TextView tv = (TextView)findViewById(R.id.txt_id_cal_area);
        tv.setText(measurement);
        tv = (TextView)findViewById(R.id.txt_id_cal_area_unit);
        tv.setText(unit);
        tv = (TextView)findViewById(R.id.txt_id_cal_unit_price_unit);
        tv.setText(this.getString(R.string.txt_static_money_unit)+"/"+unit);
    }
	
	public void onNumPadClick(View p_view)
	{
		myDebugLog(tag, "onNumPadClick: button '" + ((Button)p_view).getText().toString() + "' is clicked");
		
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
		myDebugLog(tag, "onCalculate");
		
		TextView tv = (TextView)findViewById(R.id.txt_id_cal_area);
		double area = Double.parseDouble(tv.getText().toString());
		double unit_price = Double.parseDouble(m_unit_price);
		
		double total_price = area*unit_price;
		tv = (TextView)findViewById(R.id.txt_id_cal_total_price);
		tv.setText(String.format("%.2f", total_price));
	}
            
	
	private void myDebugLog(String p_tag, String p_msg) {
		if (s_log_switch)
		{
			Log.d(p_tag, p_msg);
		}
	}

}
