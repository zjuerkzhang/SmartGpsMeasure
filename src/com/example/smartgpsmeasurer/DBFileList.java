package com.example.smartgpsmeasurer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DBFileList extends Activity {
	 
    private ListView listView;
    private TextView textView;
    static final String tag = "DBFileList";
    
    //private List<String> data = new ArrayList<String>();
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        MainActivity.myDebugLog(tag, "onCreate");
        List<String> lv_str = getData();
        if(lv_str.size() == 0)
        {
        	textView = new TextView(this);
        	textView.setText(this.getString(R.string.dblist_no_record));
        	textView.setTextSize(18);
        	setContentView(textView);
        }
        else
        {
			listView = new ListView(this);

			listView.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_expandable_list_item_1, getData()));			
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					String db_filename = convertTime2Dbname(((TextView) arg1)
							.getText().toString());
					MainActivity.myDebugLog(tag, convertTime2Dbname(((TextView) arg1)
							.getText().toString()));
					startHistoryActivity(db_filename);
				}
			});
			setContentView(listView);
        }
    }
    /*
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(1, 1, 1, this.getString(R.string.menu_clear_history));
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 1:
			Log.v("DBFileList", "Clear db");
			Context cont = this.getApplicationContext();
			File db_dir = cont.getDatabasePath("temp");
	        db_dir = db_dir.getParentFile();
	        File db_files[] = db_dir.listFiles(); 
	        for(int i=0; i<db_files.length; i++)
	        {	        	
	        	if(db_files[i].getName().indexOf(".db")>0)
	        	{
	        		Log.v("DBFileList", "Delete "+ db_files[i].getName());
	        		if(!db_files[i].delete())
	        			Log.e("DBFileList", "Fail to delete "+ db_files[i].getName());
	        	}  
	        }
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
     */
    private void startHistoryActivity(String db_filename)
    {
    	MainActivity.myDebugLog(tag, "startHistoryActivity");
    	
    	Intent intent = new Intent(this, HistoryActivity.class);
		intent.putExtra(MainActivity.EXTRA_DBFILE, db_filename);
		startActivity(intent);
    }
     
    private List<String> getData()
    {    
    	MainActivity.myDebugLog(tag, "getData");
    	
        List<String> data = new ArrayList<String>();
        Context cont = this.getApplicationContext();
        File db_dir = cont.getDatabasePath("temp");
        db_dir = db_dir.getParentFile();
        File db_files[] = db_dir.listFiles(); 
        if(db_files == null)
        	return data;
        for(int i=db_files.length-1; i>=0; i--)
        {
        	if(db_files[i].getName().indexOf(".db")>0)
        		data.add(convertDbName2Time(db_files[i].getName()));
        }
        
        /*
        data.add("测试数据1");
        data.add("测试数据2");
        data.add("测试数据3");
        data.add("测试数据4");
         */
        return data;
    }
    
    private String convertDbName2Time(String str)
    {  
    	MainActivity.myDebugLog(tag, "convertDbName2Time");
    	
    	String date = str.substring(0, str.indexOf("--"));
    	String time = str.substring(str.indexOf("--")+"--".length(), str.length());
    	return (date + " " + time.replace("-", ":").replace(".db", ""));
    }
    
    private String convertTime2Dbname(String str)
    {
    	MainActivity.myDebugLog(tag, "convertTime2Dbname");
    	
    	return (str.replace(" ", "--").replace(":", "-") + ".db");
    }

}
