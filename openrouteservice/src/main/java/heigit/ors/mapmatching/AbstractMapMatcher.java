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
package heigit.ors.mapmatching;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;

public abstract class AbstractMapMatcher implements MapMatcher {
	protected double _searchRadius = 50;
	protected EdgeFilter _edgeFilter;
	protected GraphHopper _graphHopper;
	
	public void setSearchRadius(double radius)
	{
		_searchRadius = radius;
	}
	
	public void setEdgeFilter(EdgeFilter edgeFilter)
	{
		_edgeFilter = edgeFilter;
	}
	
	public void setGraphHopper(GraphHopper gh)
	{
		_graphHopper = gh;
	}
	
	public RouteSegmentInfo match(double lat0, double lon0, double lat1, double lon1)
	{
		return null;
	}
}
