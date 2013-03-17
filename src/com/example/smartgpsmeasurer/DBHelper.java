package com.example.smartgpsmeasurer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {  
	  
    private static final int DATABASE_VERSION = 1;  
      
    public DBHelper(Context context, String db_name) {  
        //CursorFactory����Ϊnull,ʹ��Ĭ��ֵ  
        super(context, db_name, null, DATABASE_VERSION);  
    }  
  
    //���ݿ��һ�α�����ʱonCreate�ᱻ����  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        db.execSQL("CREATE TABLE IF NOT EXISTS track" +  
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude FLOAT, longitude FLOAT)");  
    }  
  
    //���DATABASE_VERSIONֵ����Ϊ2,ϵͳ�����������ݿ�汾��ͬ,�������onUpgrade  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL("ALTER TABLE track ADD COLUMN other STRING");  
    }  
}  
