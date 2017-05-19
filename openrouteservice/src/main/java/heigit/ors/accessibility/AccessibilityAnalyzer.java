/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.accessibility;

import java.io.IOException;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.exceptions.InternalServerException;
import heigit.ors.locations.LocationsResult;
import heigit.ors.locations.providers.LocationsDataProvider;
import heigit.ors.locations.providers.LocationsDataProviderFactory;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.services.accessibility.AccessibilityRequest;
import heigit.ors.services.locations.LocationsServiceSettings;
import heigit.ors.services.routing.RoutingRequest;

public class AccessibilityAnalyzer {
   public static AccessibilityResult computeAccessibility(AccessibilityRequest req) throws IOException, Exception
   {
	   List<LocationsResult> locations = null;

		try
		{
			LocationsDataProvider provider = LocationsDataProviderFactory.getProvider(LocationsServiceSettings.getProviderName(), LocationsServiceSettings.getProviderParameters());
			locations = provider.findLocations(req.getLocationsRequest());
		}
		catch(Exception ex)
		{
			throw new InternalServerException(AccessibilityErrorCodes.UNKNOWN, ex.getMessage());
		}

		AccessibilityResult accesibilityResult = new AccessibilityResult();
		accesibilityResult.setLocations(locations);

		if (!locations.isEmpty())
		{
			RoutingRequest reqRouting = req.getRoutingRequest();
			Coordinate[] coords = new Coordinate[locations.size() + 1];
			coords[0] = reqRouting.getCoordinates()[0];
			for(int i = 0 ; i < locations.size(); ++i)
				coords[i+1] = locations.get(i).getGeometry().getCoordinate();
			reqRouting.setCoordinates(coords);

			List<RouteResult> routes = RoutingProfileManager.getInstance().getRoutes(reqRouting, "destination".equalsIgnoreCase(req.getLocationType()), true);
			accesibilityResult.setRoutes(routes);
		}
		
		return accesibilityResult;
   }
}
