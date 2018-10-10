/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
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

	/**
	 * Returns the range specified in the unit given by the user. The algorithm converts
	 * the input range to m so we need to convert it back.
	 *
	 * @param  unit  the unit to return in
	 * @return      the ranges array in the specified unit
	 */

	public double[] getRangesInUnit(String unit){
		// convert ranges from meters to user specified unit
		double[] rangesInUnit = new double[_ranges.length];

		if (!(unit == null || "m".equalsIgnoreCase(unit)))
		{
			double scale = 1.0;
			if (_rangeType == TravelRangeType.Distance)
			{
				switch(unit)
				{
					case "m":
						break;
					case "km":
						scale = 1/1000.0;
						break;
					case "mi":
						scale = 1/1609.34;
						break;
				}
			}

			if (scale != 1.0)
			{
				for (int i = 0; i < _ranges.length; i++)
					rangesInUnit[i] = _ranges[i]*scale;
				return rangesInUnit;
			}
			return _ranges;
		}
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
