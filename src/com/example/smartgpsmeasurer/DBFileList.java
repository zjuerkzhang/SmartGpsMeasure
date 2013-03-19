package com.example.smartgpsmeasurer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.smartgpsmeasurer.R.color;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DBFileList extends Activity {
	 
    private ListView listView;
    //private List<String> data = new ArrayList<String>();
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
         
        listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData()));
        listView.setBackgroundColor(color.bk_color);
        setContentView(listView);
        listView.setOnItemClickListener(new OnItemClickListener() {          	  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {  
                // TODO Auto-generated method stub  
                Log.v("DBFileList", ((TextView)arg1).getText().toString());
            }  
            });
    }
     
     
     
    private List<String> getData(){
         
        List<String> data = new ArrayList<String>();
        Context cont = this.getApplicationContext();
        File db_dir = cont.getDatabasePath("temp");
        db_dir = db_dir.getParentFile();
        File db_files[] = db_dir.listFiles();
        for(int i=db_files.length-1; i>=0; i--)
        {
        	data.add(db_files[i].getName());
        }
        /*
        data.add("测试数据1");
        data.add("测试数据2");
        data.add("测试数据3");
        data.add("测试数据4");
         */
        return data;
    }
    
    
}
