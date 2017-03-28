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
package heigit.ors.routing;

import com.graphhopper.util.shapes.BBox;

public class RouteSummary 
{
	private double _distance;
	private double _distanceActual;
	private double _duration;
	private double _ascent;
	private double _descent;
	private BBox _bbox;

	public double getDistance() {
		return _distance;
	}

	public void setDistance(double distance) {
		_distance = distance;
	}

	public double getDuration() {
		return _duration;
	}

	public void setDuration(double duration) {
		this._duration = duration;
	}
	
	public BBox getBBox()
	{
		return _bbox;
	}
	
	public void setBBox(BBox bbox)
	{
		_bbox = bbox;
	}

	public double getAscent() {
		return _ascent;
	}

	public void setAscent(double ascent) {
		_ascent = ascent;
	}

	public double getDescent() {
		return _descent;
	}

	public void setDescent(double descent) {
		_descent = descent;
	}

	public double getDistanceActual() {
		return _distanceActual;
	}

	public void setDistanceActual(double distanceActual) {
		_distanceActual = distanceActual;
	}
}
