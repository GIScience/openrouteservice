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
