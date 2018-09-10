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
package heigit.ors.isochrones;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.TravelRangeType;
import heigit.ors.routing.RouteSearchParameters;

public class IsochroneSearchParameters {
	private int _travellerId;
	private Coordinate _location;
	private Boolean _reverseDirection = false;
	private TravelRangeType _rangeType = TravelRangeType.Time;
	private double[] _ranges;
	private RouteSearchParameters _parameters;
	private String _calcMethod;
	private float _smoothingFactor = -1.0f;

	public IsochroneSearchParameters(int travellerId, Coordinate location, double[] ranges) {
		_travellerId = travellerId;
		_location = location;   
		_ranges = ranges;
	}
	
	public int getTravellerId()
	{
		return _travellerId;
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

	public void setRangeType(TravelRangeType rangeType)
	{
		_rangeType = rangeType;
	}

	public TravelRangeType getRangeType()
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

	public float getSmoothingFactor() {
		return _smoothingFactor;
	}

	public void setSmoothingFactor(float smoothingFactor) {
		this._smoothingFactor = smoothingFactor;
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
