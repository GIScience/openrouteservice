package heigit.ors.util;

import java.lang.Math;

public class SphericalMercator 
{
	public static final double RADIUS = 6378137.0; /* in meters on the equator */
	public static final double PI_DIV_2 = Math.PI/2.0;
	public static final double PI_DIV_4 = Math.PI/4.0;

	public static double yToLat(double aY) {
		return Math.toDegrees(Math.atan(Math.exp(aY / RADIUS)) * 2 - PI_DIV_2);
	}
	public static double xToLon(double aX) {
		return Math.toDegrees(aX / RADIUS);
	}

	public static double latToY(double aLat) {
		return Math.log(Math.tan(PI_DIV_4 + Math.toRadians(aLat) / 2)) * RADIUS;
	}  

	public static double lonToX(double aLong) {
		return Math.toRadians(aLong) * RADIUS;
	}
}