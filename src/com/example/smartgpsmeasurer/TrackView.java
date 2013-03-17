package com.example.smartgpsmeasurer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TrackView extends View {
	
	private ArrayList<Point> mTrack = new ArrayList<Point>();
	
	private final Paint mPaint = new Paint();
	
	private int mMaxCanvasRadius = 0;
	private double mMaxGeoRadius = 1.0;
	private Point mCenterPoint = null;
	private Point mFirstGeoPoint = null;
	
	public TrackView(Context context, AttributeSet attrs, int defStyle) 
	{
        super(context, attrs, defStyle);        
    }

    public TrackView(Context context, AttributeSet attrs) 
    {
        super(context, attrs);        
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) 
    {
    	if(w > h)
    		mMaxCanvasRadius = (h-1)/2;
    	else
    		mMaxCanvasRadius = (w-1)/2;
    	mCenterPoint = new Point(w/2, h/2);
    }
    
    @Override
    public void onDraw(Canvas canvas) 
    {
    	super.onDraw(canvas);       
        
        mPaint.setColor(Color.GREEN);
        /*
        canvas.drawLine((float)mCenterPoint.getX()-mMaxCanvasRadius*3/4, 
        		(float)mCenterPoint.getY(), 
        		(float)mCenterPoint.getX()+mMaxCanvasRadius*3/4, 
        		(float)mCenterPoint.getY(),
        		mPaint);
        canvas.drawLine((float)mCenterPoint.getX(), 
        		(float)mCenterPoint.getY()-mMaxCanvasRadius*3/4, 
        		(float)mCenterPoint.getX(), 
        		(float)mCenterPoint.getY()+mMaxCanvasRadius*3/4,
        		mPaint);
        */
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle((float)mCenterPoint.getX(), 
        		(float)mCenterPoint.getY(), 
        		3, 
        		mPaint);
        /*
        canvas.drawCircle((float)mCenterPoint.getX(), 
        		(float)mCenterPoint.getY(), 
        		mMaxCanvasRadius*2/3, 
        		mPaint);
        */
        if(mTrack.size() >= 2)
        {
        	mPaint.setColor(Color.WHITE);
        	int i = 1;
        	double l_factor = mMaxCanvasRadius/mMaxGeoRadius*19/20;
        	for( ;i < mTrack.size();i++)
        	{
        		canvas.drawLine((int)Math.floor(mTrack.get(i-1).getX()*l_factor + mCenterPoint.getX()), 
        		        (int)Math.floor(mTrack.get(i-1).getY()*l_factor + mCenterPoint.getY()), 
        		        (int)Math.floor(mTrack.get(i).getX()*l_factor + mCenterPoint.getX()), 
        		        (int)Math.floor(mTrack.get(i).getY()*l_factor + mCenterPoint.getY()), 
        		        mPaint);
        	}
        	i--;
        	mPaint.setColor(Color.YELLOW);
        	mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle((float)(mTrack.get(i).getX()*l_factor + mCenterPoint.getX()), 
            		          (float)(mTrack.get(i).getY()*l_factor + mCenterPoint.getY()), 
            		          3, 
            		          mPaint);
            
            canvas.drawLine((float)(mTrack.get(i).getX()*l_factor + mCenterPoint.getX())-6, 
            		(float)(mTrack.get(i).getY()*l_factor + mCenterPoint.getY()), 
            		(float)(mTrack.get(i).getX()*l_factor + mCenterPoint.getX())+6, 
            		(float)(mTrack.get(i).getY()*l_factor + mCenterPoint.getY()),
            		mPaint);
            canvas.drawLine((float)(mTrack.get(i).getX()*l_factor + mCenterPoint.getX()), 
            		(float)(mTrack.get(i).getY()*l_factor + mCenterPoint.getY())-6, 
            		(float)(mTrack.get(i).getX()*l_factor + mCenterPoint.getX()), 
            		(float)(mTrack.get(i).getY()*l_factor + mCenterPoint.getY())+6,
            		mPaint);        	
        }
        
        /*
        canvas.drawLine((int)Math.floor(mCenterPoint.getX()), 
        		        (int)Math.floor(mCenterPoint.getY()), 
        		        (int)Math.floor(mCenterPoint.getX()) - mRadius, 
        		        (int)Math.floor(mCenterPoint.getY()), 
        		        mPaint);  
        
        canvas.drawLine((int)Math.floor(mCenterPoint.getX()), 
		        (int)Math.floor(mCenterPoint.getY()), 
		        (int)Math.floor(mCenterPoint.getX()), 
		        (int)Math.floor(mCenterPoint.getY()) + mRadius, 
		        mPaint);  
         */
    }

    public void AddNewPoint(Point p_point)
    {
    	if(mTrack.size()==0)
    	{
    		mFirstGeoPoint = p_point;
    		mTrack.add(new Point(0,0));
    	}
    	else
    	{
    		p_point =  new Point(p_point.getX()-mFirstGeoPoint.getX(),
    				             mFirstGeoPoint.getY()-p_point.getY()); 
    		// It seems that the Geo Convert function has something wrong with the Y scale
    		mTrack.add(p_point);
    		
    		double l_new_radius = Point.getDistance(new Point(0,0), p_point);
        	if(l_new_radius > mMaxGeoRadius )
        		mMaxGeoRadius = l_new_radius;
    	}    	
    	
    	TrackView.this.invalidate();
    }
    
    public void ClearAllPoint()
    {
    	mTrack.clear();
    	TrackView.this.invalidate();
    }
    
}
