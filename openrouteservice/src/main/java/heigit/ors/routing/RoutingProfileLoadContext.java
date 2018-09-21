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

import com.graphhopper.reader.dem.ElevationProvider;

public class RoutingProfileLoadContext
{
	// add here any other shared resources
	private int _threads = 1;

	private ElevationProvider _elevProvider = null;
	
	public RoutingProfileLoadContext()
	{
		this(1);
	}
	
	public RoutingProfileLoadContext(int threads)
	{
		_threads = threads;
	}

	public ElevationProvider getElevationProvider(){
		return _elevProvider;
	}

	public void setElevationProvider(ElevationProvider ep) {
		if(_elevProvider == null) {
			_elevProvider = ep;
		}
	}

	public void releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed() {
		if(_elevProvider != null){
			_elevProvider.release();
		}
	}
}
