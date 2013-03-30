package com.example.smartgpsmeasurer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class InvalidActivity extends Activity{
	
	static final String tag = "InvalidActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		MainActivity.myDebugLog(tag, "onCreate");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_invalid_device);        
        
    }
	
	public void onClickValid(View p_view)
	{
		MainActivity.myDebugLog(tag, "onClickValid");
		InvalidActivity.this.finish();
	}

}
