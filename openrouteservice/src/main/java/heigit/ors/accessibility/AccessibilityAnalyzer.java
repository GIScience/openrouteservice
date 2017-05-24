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
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;
import heigit.ors.locations.providers.LocationsDataProvider;
import heigit.ors.locations.providers.LocationsDataProviderFactory;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.services.accessibility.AccessibilityRequest;
import heigit.ors.services.isochrones.IsochroneRequest;
import heigit.ors.services.locations.LocationsServiceSettings;
import heigit.ors.services.routing.RoutingRequest;

public class AccessibilityAnalyzer {
	public static AccessibilityResult computeAccessibility(AccessibilityRequest req) throws IOException, Exception
	{
		try
		{
			AccessibilityResult accesibilityResult = new AccessibilityResult();

			// Phase I: compute isochrone that includes all possible POIs.
			IsochroneRequest reqIsochrone = new IsochroneRequest();
			reqIsochrone.setLocationType(req.getLocationType());
			reqIsochrone.setLocations(req.getLocations());
			reqIsochrone.setRangeType(req.getRangeType());
			reqIsochrone.setRanges(new double[] {req.getRange()});
			reqIsochrone.setRouteSearchParameters(req.getRoutingRequest().getSearchParameters());

			IsochroneMapCollection isoMaps = new IsochroneMapCollection();

			IsochroneSearchParameters searchParams = reqIsochrone.getSearchParameters(req.getLocations()[0]);
			searchParams.setRouteParameters(reqIsochrone.getRouteSearchParameters());

			for (int i = 0;i < req.getLocations().length; ++i){
				searchParams.setLocation(req.getLocations()[i]);
				IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams);
				isoMaps.add(isochroneMap);
			}
			
			if (isoMaps.size() > 0)
			{
				// Phase II: find locations within an isochrone
				Isochrone isochrone = isoMaps.getIsochrone(0).getIsochrone(0);
				LocationsRequest reqLocations = req.getLocationsRequest().clone();
				reqLocations.setGeometry(isochrone.getGeometry());
				LocationsDataProvider provider = LocationsDataProviderFactory.getProvider(LocationsServiceSettings.getProviderName(), LocationsServiceSettings.getProviderParameters());
				List<LocationsResult> locations = provider.findLocations(reqLocations);

				accesibilityResult.setLocations(locations);

				// Phase III: compute routes from start point to all found places
				if (!locations.isEmpty())
				{
					RoutingRequest reqRouting = req.getRoutingRequest();
					reqRouting.setCoordinates(new Coordinate[] { req.getLocations()[0] });
					//reqRouting.setLocationIndex(locationIndex);
					Coordinate[] coords = new Coordinate[locations.size() + 1];
					coords[0] = reqRouting.getCoordinates()[0];
					for(int i = 0 ; i < locations.size(); ++i)
						coords[i+1] = locations.get(i).getGeometry().getCoordinate();
					reqRouting.setCoordinates(coords);

					List<RouteResult> routes = RoutingProfileManager.getInstance().getRoutes(reqRouting, "destination".equalsIgnoreCase(req.getLocationType()), true);
					accesibilityResult.setRoutes(routes);
				}
			}

			return accesibilityResult;
		}
		catch(Exception ex)
		{
			throw new InternalServerException(AccessibilityErrorCodes.UNKNOWN, ex.getMessage());
		}
	}
}
