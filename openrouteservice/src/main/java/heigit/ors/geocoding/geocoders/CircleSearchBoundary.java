/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.geocoding.geocoders;

import com.graphhopper.util.Helper;

public class CircleSearchBoundary implements SearchBoundary {

	private double _lon;
	private double _lat;
	
	private double _radius;
	
	public CircleSearchBoundary(double lon, double lat, double radius)
	{
		_lon = lon;
		_lat = lat;
		_radius = radius;
	}
	
	public double getLongitude()
	{
		return _lon;
	}
	
	public double getLatitude()
	{
		return _lat;
	}
	
	public double getRadius()
	{
		return _radius;
	}
	
	@Override
	public boolean contains(double lon, double lat) {
        double dist = Helper.DIST_EARTH.calcDist(_lat, _lon, lat, lon) / 1000;
        return dist <= _radius;
	}
}
