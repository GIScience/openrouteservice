/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package org.freeopenls.routeservice.routing;

import java.util.concurrent.Callable;

import org.freeopenls.routeservice.routing.configuration.RouteProfileConfiguration;

public class RouteProfileLoader implements Callable<RouteProfile> {
	private String osmFile;
	private String configRoot;
	private RouteProfileConfiguration rpc;
	private RouteProfilesCollection routeProfiles;

	public RouteProfileLoader(String osmFile, String configRoot, RouteProfileConfiguration rpc, RouteProfilesCollection routeProfiles) {
		this.osmFile = osmFile;
		this.configRoot = configRoot;
		this.rpc = rpc;
		this.routeProfiles = routeProfiles;
	}

	@Override
	public RouteProfile call() throws Exception {
		return new RouteProfile(osmFile, configRoot, rpc, routeProfiles);
	}
}