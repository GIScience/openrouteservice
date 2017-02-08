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
package heigit.ors.isochrones;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.isochrones.IsochronesRangeType;
import heigit.ors.routing.RouteSearchParameters;

public class IsochroneSearchParameters {
	private Coordinate _location;
	private Boolean _reverseDirection = false;
	private IsochronesRangeType _rangeType = IsochronesRangeType.Time;
	private double[] _ranges;
	private RouteSearchParameters _parameters;
	private String _calcMethod;

	public IsochroneSearchParameters(Coordinate location, double[] ranges) {
		_location = location;   
		_ranges = ranges;
	}

	public Coordinate getLocation()
	{
		return _location;
	}
	
	public void setLocation(Coordinate location)
	{
		_location = location;
	}

	public Boolean getReverseDirection()
	{
		return _reverseDirection;
	}

	public void setReverseDirection(Boolean value)
	{
		_reverseDirection = value;
	}

	public void setRangeType(IsochronesRangeType rangeType)
	{
		_rangeType = rangeType;
	}

	public IsochronesRangeType getRangeType()
	{
		return _rangeType;
	}

	public void setRanges(double[] values)
	{
		_ranges = values;
	}

	public double[] getRanges()
	{
		return _ranges;
	}
	
	public double getMaximumRange()
	{
		if (_ranges.length == 1)
			return _ranges[0];
		else
		{
			double maxValue = Double.MIN_VALUE;
			for (int i = 0; i < _ranges.length; ++i)
			{
				double v = _ranges[i];
				if (v > maxValue)
					maxValue = v;
			}
			
			return maxValue;
		}
	}

	public RouteSearchParameters getRouteParameters() {
		return _parameters;
	}

	public void setRouteParameters(RouteSearchParameters parameters) {
		_parameters = parameters;
	}

	public String getCalcMethod() 
	{
		return _calcMethod;
	}

	public void setCalcMethod(String calcMethod) 
	{
		_calcMethod = calcMethod;
	}
}
