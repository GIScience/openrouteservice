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

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.shapes.BBox;

import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;

public class RouteSegment {
	private double _distance;
	private double _duration;
	private double _ascent;
	private double _descent;
	private double _detourFactor = 0.0;
	private BBox _bbox;
	private List<RouteStep> _steps;

	public RouteSegment(PathWrapper path, DistanceUnit units) throws Exception
	{
		_distance = FormatUtility.roundToDecimals(DistanceUnitUtil.convert(path.getDistance(), DistanceUnit.Meters, units), FormatUtility.getUnitDecimals(units));
		_duration =   FormatUtility.roundToDecimals(path.getTime()/1000.0, 1);
		_ascent = FormatUtility.roundToDecimals(path.getAscend(), 1);
		_descent = FormatUtility.roundToDecimals(path.getDescend() ,1);

		if (_bbox == null)
		{
			double lat = path.getPoints().getLat(0);
			double lon = path.getPoints().getLon(0);
			_bbox = new BBox(lon, lon, lat, lat);
		}

		path.calcRouteBBox(_bbox);

		_steps = new ArrayList<RouteStep>();
	}

	public double getDistance()
	{
		return _distance;
	}   

	public double getDuration()
	{
		return _duration;
	}

	public double getAscent()
	{
		return _ascent;
	}

	public double getDescent()
	{
		return _descent;
	}

	public BBox getBBox()
	{
		return _bbox;
	}

	public void addStep(RouteStep step)
	{
		_steps.add(step);
	}

	public List<RouteStep> getSteps() {
		return _steps;
	}

	public double getDetourFactor() {
		return _detourFactor;
	}

	public void setDetourFactor(double detourFactor) {
		_detourFactor = detourFactor;
	}
}
