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
 *//*

package heigit.ors.accessibility;

import java.util.List;

import heigit.ors.locations.LocationsResult;
import heigit.ors.routing.RouteResult;

public class AccessibilityResult {
    private List<RouteResult> _routes;
    private List<LocationsResult> _locations;
    
    public AccessibilityResult()
    {
    	
    }
    
    public void setRoutes(List<RouteResult> routes)
    {
    	_routes = routes;
    }
    
    public List<RouteResult> getRoutes()
    {
    	return _routes;
    }

	public List<LocationsResult> getLocations() {
		return _locations;
	}

	public void setLocations(List<LocationsResult> locations) {
		_locations = locations;
	}
}
*/
