/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
