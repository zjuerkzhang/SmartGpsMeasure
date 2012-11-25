package com.example.smartgpsmeasurer;

public class GeoPoint {


	private double m_latitude;
	private double m_longitude;
	
	public GeoPoint()
	{
		m_latitude = 0.0;
		m_longitude = 0.0;
	}
	
	public GeoPoint(double p_latitude, double p_longitude)
	{
		m_latitude = p_latitude;
		m_longitude = p_longitude;
	}
	
	public double getLatitude()
	{
		return m_latitude;
	}
	
	public double getLongitude()
	{
		return m_longitude;
	}
	
	public static double getDistance(GeoPoint p_gp_a, GeoPoint p_gp_b)
	{
		Point cp_a = GeoPoint.convertGeo2Cart(p_gp_a);
		Point cp_b = GeoPoint.convertGeo2Cart(p_gp_b);
		
		return Point.getDistance(cp_a, cp_b);
	}
	
	public static double getArea(GeoPoint p_base, GeoPoint p_first, GeoPoint p_second)
	{
		Point base = GeoPoint.convertGeo2Cart(p_base);
		Point first = GeoPoint.convertGeo2Cart(p_first);
		Point second = GeoPoint.convertGeo2Cart(p_second);
		
		return Point.getArea(base, first, second);
	}
	
	public static Point convertGeo2Cart(GeoPoint p_gp)
	{
		
		double r_a = 6378137;
		double r_b = 6356752.314;
		//double e2 = 0.0066943799013;
		double S, N, t, t2, m, m2, ng2;
	    double sinB, cosB;
	    double e2, e12;
	    double A0, B0, C0, D0, E0;
	    double L_rad, B_rad, L0;
	    double n0;
	    double x, y;
	    
	    e2 = (r_a*r_a - r_b*r_b)/(r_a*r_a);
	    e12 = (r_a*r_a - r_b*r_b)/(r_b*r_b);
	    n0 = (r_a - r_b)/(r_a + r_b);
	    
	    L_rad = p_gp.getLongitude() * Math.PI /180.0;
		B_rad = p_gp.getLatitude() * Math.PI /180.0;
		if(L_rad < 0.0 )
		{
		    L0 = Math.floor((180.0 + p_gp.getLongitude()) / 6) + 1;
		}
		else
		{
		    L0 = Math.floor(p_gp.getLongitude() / 6) + 31;
		}
		L0 = L0*6 - 3 - 180;
	    
	    A0 = (r_a + r_b)*(1 + Math.pow(n0, 2)/4.0 + Math.pow(n0, 4)/64.0 )/2;
	    B0 = A0*( -3/2.0*n0 + 9/16.0*Math.pow(n0, 3) - 3/32.0*Math.pow(n0, 5) );
	    C0 = A0*(15/16.0*Math.pow(n0,2) - 15/32.0*Math.pow(n0,4));
	    D0 = A0*(-35/48.0*Math.pow(n0,3) + 105/256.0*Math.pow(n0,5));
	    E0 = A0*315/512.0*Math.pow(n0,4);
	    /*
	    printf("A0: %f\n", A0);
	    printf("B0: %f\n", B0);
	    printf("C0: %f\n", C0);
	    printf("D0: %f\n", D0);
	    printf("E0: %f\n", E0);
	    printf("L0: %f\n", L0);
	    */
	    S = A0*B_rad + B0*Math.sin(2 * B_rad) + C0 * Math.sin(4 * B_rad) + 
	        D0 * Math.sin(6 * B_rad) + E0 * Math.sin(8*B_rad);
	    sinB = Math.sin(B_rad);
	    cosB = Math.cos(B_rad);
	    t = Math.tan(B_rad);
	    t2 = t * t;
	    N = r_a / Math.sqrt(1 - e2 * sinB * sinB);
	    m = cosB * (p_gp.getLongitude() - L0)*Math.PI/180.0;
	    m2 = m * m;
	    ng2 = cosB * cosB * e2 / (1 - e2);
	    x = S + N * t * ((0.5 + ((5 - t2 + 9 * ng2 + 4 * ng2 * ng2) / 24.0 + (61 -
	                            58 * t2 + t2 * t2) * m2 / 720.0) * m2) * m2);
	    y = N * m * ( 1 + m2 * ( (1 - t2 + ng2) / 6.0 + m2 * ( 5 - 18 * t2 + t2 * t2 + 14 * ng2 - 58 * ng2 * t2 ) / 120.0));
	    y += 500000;
	    /*
	    printf("--------------------\n");
	    printf("x:%8f y:%8f\n", x, y);
	    */
	    return new Point(y, x);
		
	}

}
