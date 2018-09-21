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
package heigit.ors.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.PMap;

public class RouteSearchContext {
	private GraphHopper _graphhopper;
	private EdgeFilter _edgeFilter;
	private FlagEncoder _encoder;
	
	private PMap _properties;

	public RouteSearchContext(GraphHopper gh, EdgeFilter edgeFilter, FlagEncoder encoder)
	{
		_graphhopper = gh;   
		_edgeFilter = edgeFilter;
		_encoder = encoder;
	}

	public FlagEncoder getEncoder() {
		return _encoder;
	}

	public EdgeFilter getEdgeFilter() {
		return _edgeFilter;
	}

	public GraphHopper getGraphHopper() {
		return _graphhopper;
	}
	
	public PMap getProperties()
	{
		return _properties;
	}
	
	public void setProperties(PMap value)
	{
		_properties = value;
	}
}
