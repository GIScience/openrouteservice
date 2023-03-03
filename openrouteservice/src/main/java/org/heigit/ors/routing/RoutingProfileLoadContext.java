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

import com.graphhopper.reader.dem.ElevationProvider;

/**
 * Holds resources shared between instances of {@link RoutingProfile}.
 * An example of such a shared resource is {@link ElevationProvider}.
 */
public class RoutingProfileLoadContext {
	// add here any other shared resources
	private ElevationProvider elevationProvider = null;
	
	public ElevationProvider getElevationProvider(){
		return elevationProvider;
	}

	public void setElevationProvider(ElevationProvider ep) {
		if (elevationProvider == null) {
			elevationProvider = ep;
		}
	}

	public void releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed() {
		if (elevationProvider != null){
			elevationProvider.release();
		}
	}
}
