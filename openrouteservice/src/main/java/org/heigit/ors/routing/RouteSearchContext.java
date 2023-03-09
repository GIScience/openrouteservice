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
package org.heigit.ors.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.PMap;

public class RouteSearchContext {
	private final GraphHopper graphhopper;
	private final FlagEncoder encoder;
	private final String profileName;
	private final String profileNameCH;
	private PMap properties;

	public RouteSearchContext(GraphHopper gh, FlagEncoder encoder, String profileName, String profileNameCH) {
		graphhopper = gh;
		this.encoder = encoder;
		this.profileName = profileName;
		this.profileNameCH = profileNameCH;
	}

	public FlagEncoder getEncoder() {
		return encoder;
	}

	public GraphHopper getGraphHopper() {
		return graphhopper;
	}
	
	public PMap getProperties()
	{
		return properties;
	}
	
	public void setProperties(PMap value)
	{
		properties = value;
	}

	public String profileName() {
		return profileName;
	}

	public String profileNameCH() {
		return profileNameCH;
	}
}
