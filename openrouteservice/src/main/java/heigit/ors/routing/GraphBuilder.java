/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing;

import heigit.ors.routing.RoutingProfileManager;

public class GraphBuilder {
	public static void main(String[] args) {

		if (args.length >= 1)
		{
			RoutingProfileManager rpm = new RoutingProfileManager();
			rpm.prepareGraphs(args[0]);
		//	rpm.prepareGraphs("C:\\Users\\Runge\\workspace\\openrouteserviceGH\\target\\openrouteservice-0.0.1-SNAPSHOT\\WEB-INF\\GraphGH.properties.xml");
		}
	}
}
