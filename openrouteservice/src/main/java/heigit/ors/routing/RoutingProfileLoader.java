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