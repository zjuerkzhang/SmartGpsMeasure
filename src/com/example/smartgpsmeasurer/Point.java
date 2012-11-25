package com.example.smartgpsmeasurer;

public class Point {
	private double m_x;
	private double m_y;
	
	public Point()
	{
		setX(0.0);
		setY(0.0);
	}
	
	public Point(double x, double y)
	{
		setX(x);
		setY(y);
	}

	public double getX() {
		return m_x;
	}

	public void setX(double x) {
		this.m_x = x;
	}

	public double getY() {
		return m_y;
	}

	public void setY(double m_y) {
		this.m_y = m_y;
	}
	
	public static double getDistance(Point p_a, Point p_b)
	{
		return Math.sqrt( Math.pow(p_a.getX()-p_b.getX(), 2.0) +
				          Math.pow(p_a.getY()-p_b.getY(), 2.0) );
	}
	
	public static double getArea(Point p_base, Point p_first, Point p_second)
	{
		Point delta_first = new Point(p_first.getX()-p_base.getX(),
				                      p_first.getY()-p_base.getY());
    	Point delta_second = new Point(p_second.getX()-p_base.getX(),
                                       p_second.getY()-p_base.getY());
    	
    	return (delta_first.getX()*delta_second.getY() -
    			delta_first.getY()*delta_second.getX())/2.0;

	}
}
