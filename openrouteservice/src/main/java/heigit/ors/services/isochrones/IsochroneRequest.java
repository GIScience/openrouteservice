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
package heigit.ors.services.isochrones;

import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.IsochronesRangeType;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.services.ServiceRequest;

import com.vividsolutions.jts.geom.Coordinate;

public class IsochroneRequest extends ServiceRequest
{
	private String _calcMethod;
	private Coordinate[] _locations;
	private String _locationType = "start"; // either start or destination
	private String _units = null;
	private RouteSearchParameters _routeParameters;   
	private double[] _ranges;
	private double _maxRange;
	private String[] _attributes;
	private IsochronesRangeType _rangeType = IsochronesRangeType.Time;
	private Boolean _includeIntersections = false;

	public IsochroneRequest()
	{
		_routeParameters = new RouteSearchParameters();
	}

	public String getCalcMethod() 
	{
		return _calcMethod;
	}

	public void setCalcMethod(String calcMethod) 
	{
		_calcMethod = calcMethod;
	}

	public Coordinate[] getLocations() {
		return _locations;
	}

	public void setLocations(Coordinate[]  coordinates) {
		_locations =  coordinates;
	}

	public String getLocationType()	{
		return _locationType;
	}

	public void setLocationType(String value){
		_locationType = value;
	}

	public String getUnits() {
		return _units;
	}

	public void setUnits(String units) {
		_units = units;
	}

	public RouteSearchParameters getRouteSearchParameters() {
		return _routeParameters;
	}

	public void setRouteSearchParameters(RouteSearchParameters parameters) {
		_routeParameters = parameters;
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

	public IsochronesRangeType getRangeType() {
		return _rangeType;
	}

	public void setRangeType(IsochronesRangeType rangeType) {
		_rangeType = rangeType;
	}

	public boolean isValid()
	{
		return _locations != null && _locations.length >= 1;
	}

	public String[] getAttributes() {
		return _attributes;
	}

	public void setAttributes(String[] _attributes) {
		this._attributes = _attributes;
	}

	public boolean hasAttribute(String attr) {
		if (_attributes == null || attr == null)
			return false;

		for (int i = 0; i< _attributes.length; i++)
			if (attr.equalsIgnoreCase(_attributes[i]))
				return true;

		return false;
	}

	public double[] getRanges() {
		return _ranges;
	}

	public void setRanges(double[] _ranges) {
		this._ranges = _ranges;
	}

	public double getMaximumRange() {
		return _maxRange;
	}

	public void setMaximumRange(double _maxRange) {
		this._maxRange = _maxRange;
	}
	
	public Boolean getIncludeIntersections()
	{
		return _includeIntersections;
	}
	
	public void setIncludeIntersections(Boolean value)
	{
		_includeIntersections = value;
	}
	
	public IsochroneSearchParameters getSearchParameters(Coordinate location)
	{
		double[] ranges = _ranges;
		
		// convert ranges in units to meters or seconds
		if (!(_units == null || "m".equalsIgnoreCase(_units)))
		{
			double scale = 1.0;
			if (_rangeType == IsochronesRangeType.Distance)
			{
				switch(_units)
				{
				case "m":
					break;
				case "km":
					scale = 1000;
					break;
				case "mi":
					scale = 1609.34;
					break;
				}
			}
			
			if (scale != 1.0)
			{
				for (int i = 0; i < ranges.length; i++)
					ranges[i] = ranges[i]*scale;
			}
		}
		
		IsochroneSearchParameters parameters = new IsochroneSearchParameters(location, ranges);
		parameters.setRangeType(_rangeType);
		parameters.setCalcMethod(_calcMethod);
		parameters.setRouteParameters(_routeParameters);
		if ("destination".equalsIgnoreCase(_locationType))
			parameters.setReverseDirection(true);

		return parameters;
	}
}
