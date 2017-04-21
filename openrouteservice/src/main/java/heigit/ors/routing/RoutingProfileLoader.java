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
		Thread.currentThread().setName("ORS-pl-" + rpc.Name);
		return new RoutingProfile(osmFile, rpc, routeProfiles, loadCntx);
	}
	
	
	protected void afterExecute(Runnable r, Throwable t) {
	  if (t != null)
	  {
		  
	  }
	}
}