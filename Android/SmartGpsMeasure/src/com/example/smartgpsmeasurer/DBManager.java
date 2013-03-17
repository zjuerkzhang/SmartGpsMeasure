package com.example.smartgpsmeasurer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {  
    private DBHelper helper;  
    private SQLiteDatabase db;  
      
    public DBManager(Context context, String db_name) {  
        helper = new DBHelper(context, db_name);  
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);  
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里  
        db = helper.getWritableDatabase();  
    }  
      
    public void add(List<GeoPoint> points) {  
        db.beginTransaction();  //开始事务  
        try {  
            for (GeoPoint point : points) {  
                db.execSQL("INSERT INTO track VALUES(null, ?, ?)", new Object[]{point.getLatitude(), point.getLongitude()});  
            }  
            db.setTransactionSuccessful();  //设置事务成功完成  
        } finally {  
            db.endTransaction();    //结束事务  
        }  
    }  
      
    public List<GeoPoint> query() {  
        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();  
        Cursor c = queryTheCursor();  
        while (c.moveToNext()) 
        {  
        	GeoPoint point = new GeoPoint(c.getDouble(c.getColumnIndex("latitude")), 
        			                      c.getDouble(c.getColumnIndex("longitude")));  
        	points.add(point);
        }  
        c.close();  
        return points;  
    }  
      
    /** 
     * query all persons, return cursor 
     * @return  Cursor 
     */  
    public Cursor queryTheCursor() {  
        Cursor c = db.rawQuery("SELECT * FROM track", null);  
        return c;  
    }  
      
    /** 
     * close database 
     */  
    public void closeDB() {  
        db.close();  
    }  
}  