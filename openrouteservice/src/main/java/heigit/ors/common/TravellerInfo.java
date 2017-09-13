/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.common;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.routing.RouteSearchParameters;

public class TravellerInfo 
{
	private String _id = "0";
	private Coordinate _location;
	private String _locationType = "start"; // either start or destination
	private double[] _ranges;
	private TravelRangeType _rangeType = TravelRangeType.Time;
	private RouteSearchParameters _routeSearchParams;
	
	public TravellerInfo()
	{
		_routeSearchParams = new RouteSearchParameters();
	}

	public String getId() 
	{
		return _id;
	}

	public void setId(String id) 
	{
		_id = id;
	}

	public Coordinate getLocation() 
	{
		return _location;
	}

	public void setLocation(Coordinate location) 
	{
		_location = location;
	}

	public double[] getRanges() 
	{
		return _ranges;
	}

	public void setRanges(double range, double interval) {
		if (interval > range)
			range = interval;

		int nRanges = (int) Math.ceil(range / interval);
		_ranges = new double[nRanges];
		for (int i = 0; i < nRanges - 1; i++) 
			_ranges[i] = (i + 1) * interval;

		_ranges[nRanges - 1]= range;
	}

	public void setRanges(double[] ranges) 
	{
		_ranges = ranges;
	}
	
	public double getMaximumRange() {
		double maxRange = Double.MIN_VALUE;
		
		for(double range : _ranges)
		{
			if (maxRange < range)
				maxRange = range;
		}
		
		return maxRange;
	}

	public TravelRangeType getRangeType() 
	{
		return _rangeType;
	}

	public void setRangeType(TravelRangeType rangeType) 
	{
		_rangeType = rangeType;
	}

	public RouteSearchParameters getRouteSearchParameters()
	{
		return _routeSearchParams;
	}

	public void setRouteSearchParameters(RouteSearchParameters routeSearchParams)
	{
		_routeSearchParams = routeSearchParams;
	}

	public String getLocationType() {
		return _locationType;
	}

	public void setLocationType(String locationType) {
		this._locationType = locationType;
	}
	
	public TravellerInfo clone()
	{
		TravellerInfo res = new TravellerInfo();
		res._id = res._id;
		res._location = null;
		res._locationType = _locationType;
		res._ranges = _ranges;
		res._rangeType = _rangeType;
		res._routeSearchParams = _routeSearchParams;
		
		return res;
	}
}
