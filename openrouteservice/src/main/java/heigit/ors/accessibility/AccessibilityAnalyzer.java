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
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import heigit.ors.common.NamedLocation;
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
import heigit.ors.util.GeomUtility;

public class AccessibilityAnalyzer {
	public static AccessibilityResult computeAccessibility(AccessibilityRequest req) throws IOException, Exception
	{
		try
		{
			AccessibilityResult accesibilityResult = new AccessibilityResult();

			// Phase I: compute isochrone that includes all possible POIs or user-defined locations.
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
					List<LocationsResult> destLocations = null;
					Coordinate[] arrDestLocations = null; // all found destinations + 1 source
					
					// Phase II: find locations within an isochrone
					if (req.getUserLocations() != null)
					{
						NamedLocation[] userLocations = req.getUserLocations();
						destLocations = new ArrayList<LocationsResult>(userLocations.length);

						List<NamedLocation> filteredLocations = new ArrayList<>(userLocations.length);
						Polygon poly = (Polygon)geomArea;
						
						for (int i = 0; i < userLocations.length; i++)
						{
							NamedLocation namedLoc = userLocations[i];
							Point p = GeomUtility.createPoint(namedLoc.getCoordinate());
							if (poly.contains(p))
								filteredLocations.add(namedLoc);
						}
						
						if (!filteredLocations.isEmpty())
						{
							arrDestLocations = new Coordinate[filteredLocations.size() + 1];
							for(int i = 0 ; i < filteredLocations.size(); ++i)
							{
								NamedLocation namedLoc = filteredLocations.get(i);
								Coordinate c = namedLoc.getCoordinate();
								
								LocationsResult lr = new LocationsResult();
								lr.setGeometry( GeomUtility.createPoint(c));
								if (namedLoc.getName() != null)
									lr.addProperty("name", namedLoc.getName());
								destLocations.add(lr);

								arrDestLocations[i+1] = c;
							}
						}
					}
					else {
						LocationsRequest reqLocations = req.getLocationsRequest().clone();
						reqLocations.setGeometry(geomArea);

						LocationsDataProvider provider = LocationsDataProviderFactory.getProvider(LocationsServiceSettings.getProviderName(), LocationsServiceSettings.getProviderParameters());
						List<LocationsResult> poiLocations = provider.findLocations(reqLocations);
						
						if (!poiLocations.isEmpty())
						{
							destLocations = new ArrayList<LocationsResult>(poiLocations.size());
							arrDestLocations = new Coordinate[poiLocations.size() + 1];

							for(int i = 0 ; i < poiLocations.size(); ++i)
							{
								LocationsResult lr = poiLocations.get(i);
								destLocations.add(lr);
								arrDestLocations[i+1] = lr.getGeometry().getCoordinate();
							}
						}
					}

					accesibilityResult.setLocations(destLocations);

					// Phase III: compute routes from start point to all found places
					if (arrDestLocations != null)
					{
						List<RouteResult> routes = new ArrayList<RouteResult>(2*req.getLocations().length);
						
						RoutingRequest reqRouting = req.getRoutingRequest();
						reqRouting.setCoordinates(arrDestLocations);

						for (int j = 0; j < req.getLocations().length; j++)
						{
							arrDestLocations[0] = req.getLocations()[j];
							reqRouting.setCoordinates(arrDestLocations);
							reqRouting.setLocationIndex(j);
							
							List<RouteResult> routesToLocation = RoutingProfileManager.getInstance().computeRoutes(reqRouting, "destination".equalsIgnoreCase(req.getLocationType()), true);
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
