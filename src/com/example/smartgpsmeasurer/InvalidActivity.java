package com.example.smartgpsmeasurer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class InvalidActivity extends Activity{
	
	static final String tag = "InvalidActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_invalid_device);        
        
    }
	
	public void onClickValid(View p_view)
	{
		Log.d(tag, "onClickValid");
		InvalidActivity.this.finish();
	}

}
