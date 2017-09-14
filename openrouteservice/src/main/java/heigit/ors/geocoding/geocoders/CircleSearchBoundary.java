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
