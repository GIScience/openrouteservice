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
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.exceptions.InternalServerException;
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
				// Compute intersection between all isochrones
				Geometry geomArea = isoMaps.computeIntersection();

				if (geomArea != null)
				{
					// Phase II: find locations within an isochrone
					LocationsRequest reqLocations = req.getLocationsRequest().clone();
					reqLocations.setGeometry(geomArea);
					
					LocationsDataProvider provider = LocationsDataProviderFactory.getProvider(LocationsServiceSettings.getProviderName(), LocationsServiceSettings.getProviderParameters());
					List<LocationsResult> poiLocations = provider.findLocations(reqLocations);
					accesibilityResult.setLocations(poiLocations);

					// Phase III: compute routes from start point to all found places
					if (!poiLocations.isEmpty())
					{
						Coordinate[] coords = new Coordinate[poiLocations.size() + 1];

						for(int i = 0 ; i < poiLocations.size(); ++i)
							coords[i+1] = poiLocations.get(i).getGeometry().getCoordinate();
						
						List<RouteResult> routes = new ArrayList<RouteResult>(2*req.getLocations().length);
						
						RoutingRequest reqRouting = req.getRoutingRequest();
						reqRouting.setCoordinates(coords);

						for (int j = 0; j < req.getLocations().length; j++)
						{
							coords[0] = req.getLocations()[j];
							reqRouting.setCoordinates(coords);
							reqRouting.setLocationIndex(j);
							
							List<RouteResult> routesToLocation = RoutingProfileManager.getInstance().getRoutes(reqRouting, "destination".equalsIgnoreCase(req.getLocationType()), true);
							routes.addAll(routesToLocation);
						}
					
						accesibilityResult.setRoutes(routes);
					}
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
