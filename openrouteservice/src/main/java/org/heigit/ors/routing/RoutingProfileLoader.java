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

import java.util.concurrent.Callable;

import heigit.ors.routing.configuration.RouteProfileConfiguration;

public class RoutingProfileLoader implements Callable<RoutingProfile> {
	private String osmFile;
	private RouteProfileConfiguration rpc;
	private RoutingProfilesCollection routeProfiles;
	private RoutingProfileLoadContext loadCntx;

	public RoutingProfileLoader(String osmFile, RouteProfileConfiguration rpc, RoutingProfilesCollection routeProfiles, RoutingProfileLoadContext loadCntx) {
		this.osmFile = osmFile;
		this.rpc = rpc;
		this.routeProfiles = routeProfiles;
		this.loadCntx = loadCntx;
	}

	@Override
	public RoutingProfile call() throws Exception {
		Thread.currentThread().setName("ORS-pl-" + rpc.getName());
		return new RoutingProfile(osmFile, rpc, routeProfiles, loadCntx);
	}
}