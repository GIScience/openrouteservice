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
