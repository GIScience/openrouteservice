package org.freeopenls.routeservice.routing;

public class GraphBuilder {
	public static void main(String[] args) {

		if (args.length >= 1)
		{
			RouteProfileManager rpm = new RouteProfileManager();
			rpm.prepareGraphs(args[0]);
		//	rpm.prepareGraphs("C:\\Users\\Runge\\workspace\\openrouteserviceGH\\target\\openrouteservice-0.0.1-SNAPSHOT\\WEB-INF\\GraphGH.properties.xml");
		}
	}
}
