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

// Authors: M. Rylov

package heigit.ors.routing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import heigit.ors.routing.graphhopper.extensions.flagencoders.*;
import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.pathprocessors.ExtraInfoProcessor;
import heigit.ors.routing.configuration.RouteManagerConfiguration;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.services.routing.RoutingRequest;
import heigit.ors.util.CoordTools;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.routing.RoutingProfilesCollection;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.util.RuntimeUtility;
import heigit.ors.util.TimeUtility;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PathProcessor;
import com.vividsolutions.jts.geom.Coordinate;

public class RoutingProfileManager {
	private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());

	private RoutingProfilesCollection _routeProfiles;
	private RoutingProfilesUpdater _profileUpdater;
	private static RoutingProfileManager mInstance;
	private double _dynamicWeightingMaxDistance = 0; 

	public static synchronized RoutingProfileManager getInstance() throws IOException {
		if (mInstance == null)
		{
			mInstance = new RoutingProfileManager();
			mInstance.initialize(null);
		}
		
		return mInstance;
	}

	public RoutingProfileManager() {
	}
	
	private void registerFlagEncoders()
	{
		// Register all custom EdgeFlagaEncoders here.
		EncodingManager.registerDefaultEdgeFlagEncoder("wheelchair", new WheelchairFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("safetybike", new SafetyBikeFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("electrobike", new ElectroBikeFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("cycletourbike", new CycleTourBikeFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("offroadvehicle", new OffRoadVehicleFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("cartmc", new CarTmcFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("heavyvehicle", new HeavyVehicleFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("hiking", new HikingFlagEncoder());
		// EncodingManager.registerDefaultEdgeFlagEncoder("ELECTRO_VEHICLE",
	}
	
	public void prepareGraphs(String graphProps)
	{
		long startTime = System.currentTimeMillis();
		
		try
		{
			registerFlagEncoders();
			
			RouteManagerConfiguration rmc = RouteManagerConfiguration.loadFromFile(graphProps);
			RoutingProfilesCollection coll = new RoutingProfilesCollection();
			RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
			int nRouteInstances = rmc.Profiles.length;
			
			for (int i = 0; i < nRouteInstances; i++) {
				RouteProfileConfiguration rpc = rmc.Profiles[i];
				if (!rpc.Enabled)
					continue;

				LOGGER.info("Preparing route profile in "  + rpc.GraphPath + " ...");
               
				RoutingProfile rp = new RoutingProfile(rmc.SourceFile, rpc, coll, loadCntx);
				
				rp.close();
				
				LOGGER.info("Done.");
			}
			
			loadCntx.release();
			
			LOGGER.info("Graphs were prepaired in " + TimeUtility.getElapsedTime(startTime, true) + ".");
		}
		catch(Exception ex)
		{
			LOGGER.error("Failed to prepare graphs.", ex);
		}
	
		RuntimeUtility.clearMemory(LOGGER);
	}
	
	public void initialize(String graphProps) {
		RuntimeUtility.printRAMInfo("", LOGGER);

		LOGGER.info("      ");
		
		long startTime = System.currentTimeMillis();
		
		try {
			RouteManagerConfiguration rmc = RouteManagerConfiguration.loadFromFile(graphProps);

			LOGGER.info(String.format("====> Initializing profiles (%d threads) ...", rmc.InitializationThreads));
			LOGGER.info("                              ");

			_dynamicWeightingMaxDistance = rmc.DynamicWeightingMaxDistance;
			
			if ("PrepareGraphs".equalsIgnoreCase(rmc.Mode)) {
				prepareGraphs(graphProps);
			} else {
				registerFlagEncoders();
				
				_routeProfiles = new RoutingProfilesCollection();
				int nRouteInstances = rmc.Profiles.length;

				RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
				ExecutorService executor = Executors.newFixedThreadPool(rmc.InitializationThreads);
				List<Future<RoutingProfile>> list = new ArrayList<Future<RoutingProfile>>();
				
				int j = 1;
				
				for (int i = 0; i < nRouteInstances; i++) {
					RouteProfileConfiguration rpc = rmc.Profiles[i];
					if (!rpc.Enabled)
						continue;

					LOGGER.info(String.format("[%d] Profiles: '%s', location: '%s'.", j, rpc.Profiles, rpc.GraphPath));

					Integer[] routeProfiles = rpc.getProfilesTypes();

					if (routeProfiles != null) {
						Callable<RoutingProfile> worker = new RoutingProfileLoader(rmc.SourceFile, rpc,
								_routeProfiles, loadCntx);
						Future<RoutingProfile> submit = executor.submit(worker);
						list.add(submit);
					}
					
					j++;
				}

				LOGGER.info("               ");

				// now retrieve the result
				for (Future<RoutingProfile> future : list) {
					try {
						RoutingProfile rp = future.get();

						if (!_routeProfiles.add(rp))
							LOGGER.warn("Routing profile has already been added.");
					} catch (InterruptedException e) {
						LOGGER.error(e.getMessage());
						e.printStackTrace();
					} catch (ExecutionException e) {
						LOGGER.error(e.getMessage());
						e.printStackTrace();
					}
				}

				executor.shutdown();
				loadCntx.release();
				
				LOGGER.info("Total time: " + TimeUtility.getElapsedTime(startTime, true) + ".");
				LOGGER.info("========================================================================");
				
				if (rmc.TrafficInfoConfig != null && rmc.TrafficInfoConfig.Enabled) {
					RealTrafficDataProvider.getInstance().initialize(rmc, _routeProfiles);
				}

				if (rmc.UpdateConfig != null && rmc.UpdateConfig.Enabled) {
					_profileUpdater = new RoutingProfilesUpdater(rmc.UpdateConfig, _routeProfiles);
					_profileUpdater.start();
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to initialize RouteProfileManager instance.", ex);
		}
		
		RuntimeUtility.clearMemory(LOGGER);
		
		if (LOGGER.isInfoEnabled())
			_routeProfiles.printStatistics(LOGGER);
	}

	public void destroy() {
		if (_profileUpdater != null)
			_profileUpdater.destroy();
		
		if (RealTrafficDataProvider.getInstance().isInitialized())
			RealTrafficDataProvider.getInstance().destroy();

		_routeProfiles.destroy();
	}
	
	public RoutingProfilesCollection getProfiles()
	{
		return _routeProfiles;
	}
	
	public boolean updateEnabled()
	{
		return _profileUpdater != null; 
	}
	
	public Date getNextUpdateTime()
	{
		return _profileUpdater == null ? new Date() : _profileUpdater.getNextUpdate();
	}
	
	public String getUpdatedStatus()
	{
		return _profileUpdater == null ? null : _profileUpdater.getStatus();
	}
	
	public RouteResult getRoute(RoutingRequest req) throws Exception
	{
		List<GHResponse> routes = new ArrayList<GHResponse>();

		RouteSearchParameters searchParams = req.getSearchParameters();

		Coordinate[] coords = req.getCoordinates();
		
		RoutingProfile rp = getRouteProfile(coords[0].y, coords[0].x, coords[1].y, coords[1].x, false, searchParams);

		ExtraInfoProcessor extraInfoAggregator = null;

		if (req.getExtraInfo() > 0)
		{
			// do not allow geometry simplification when extras are requested
			req.setSimplifyGeometry(false);
			
			extraInfoAggregator = new ExtraInfoProcessor(rp.getGraphhopper(), req.getExtraInfo());
		}
		
		Coordinate c0 = coords[0];
		Coordinate c1;
		int nSegments = coords.length - 1;
		
		for(int i = 1; i <= nSegments; ++i)
		{
			if (extraInfoAggregator != null)
				extraInfoAggregator.setSegmentIndex(i - 1, nSegments);
			c1 = coords[i];
			GHResponse gr = rp.getRoute(c0.y, c0.x, c1.y, c1.x, c0.z == 1.0, searchParams, req.getSimplifyGeometry(), extraInfoAggregator);
			routes.add(gr);
			c0 = c1;
		}

		return new RouteResultBuilder().createRouteResult(routes, req, extraInfoAggregator != null ? extraInfoAggregator.getExtras(): null);
	}
	
	public GHResponse getRoute(double lat0, double lon0, double lat1, double lon1,  boolean directedSegment, RouteSearchParameters searchParams, boolean simplifyGeometry, PathProcessor pathProcessor) throws Exception {
		RoutingProfile rp = getRouteProfile(lat0, lon0, lat1, lon1, directedSegment, searchParams);

		return rp.getRoute(lat0, lon0, lat1, lon1, directedSegment, searchParams, simplifyGeometry, pathProcessor);
	}
	
	public RoutingProfile getRouteProfile(double lat0, double lon0, double lat1, double lon1, boolean directedSegment, RouteSearchParameters searchParams) throws Exception {
		int profileType = searchParams.getProfileType();
		
		boolean dynamicWeights = (searchParams.hasAvoidAreas() || searchParams.hasAvoidFeatures() || searchParams.getMaximumSpeed() > 0 || (RoutingProfileType.isDriving(profileType) && (searchParams.hasParameters(VehicleParameters.class) || searchParams.getConsiderTraffic())) || (searchParams.getWeightingMethod() == WeightingMethod.SHORTEST || searchParams.getWeightingMethod() == WeightingMethod.RECOMMENDED) || searchParams.getConsiderTurnRestrictions() /*|| RouteExtraInformationFlag.isSet(extraInfo, value) searchParams.getIncludeWaySurfaceInfo()*/);
		boolean chEnabled = !dynamicWeights;
		boolean checkDistance = true;
		
		if (chEnabled == false && _dynamicWeightingMaxDistance > 0.0)
		{
			double dist = CoordTools.calcDistHaversine(lon0, lat0, lon1, lat1);
			if (dist > _dynamicWeightingMaxDistance)
			{
				chEnabled = true;
				dynamicWeights = false;
				checkDistance = false;
				searchParams.setWeightingMethod(WeightingMethod.FASTEST);
				searchParams.setMaximumSpeed(-1);
				searchParams.setAvoidAreas(null);
				searchParams.setAvoidFeatureTypes(-1);
				searchParams.setConsiderTurnRestrictions(false);
				if (searchParams.hasParameters(VehicleParameters.class))
				{
					VehicleParameters vehicleParams = (VehicleParameters)searchParams.getProfileParameters();
					vehicleParams.setLoadCharacteristics(VehicleLoadCharacteristicsFlags.NONE);
				}
			}
		}
		
		/*chEnabled = true;
		dynamicWeights  = false;
		checkDistance = false;*/
		RoutingProfile rp = _routeProfiles.getRouteProfile(profileType, chEnabled, lat0, lon0, lat1, lon1, checkDistance);
		
		if (rp == null && checkDistance) {
			if (chEnabled == true) {
				rp = _routeProfiles.getRouteProfile(profileType, false, lat0, lon0, lat1, lon1, checkDistance);
			} else {
				rp = _routeProfiles.getRouteProfile(profileType, true, lat0, lon0, lat1, lon1, checkDistance);
			}
		}

		if (rp == null) {
			if (checkDistance)
				throw new Exception("Unable to get an appropriate route profile instance for RoutePreference = " + RoutingProfileType.getName(profileType));
			else
				throw new Exception("The requested route is too long and therefore cann't be processed due to a lack of resources.");
		}

		return rp;
	}
	public IsochroneMap buildIsochrone(IsochroneSearchParameters parameters) throws Exception
	{
		int profileType = parameters.getRouteParameters().getProfileType();
		RoutingProfile rp = _routeProfiles.getRouteProfile(profileType, false, false);
		
		if (rp == null)
			rp = _routeProfiles.getRouteProfile(profileType, false, true);
		
		return rp.buildIsochrone(parameters);
	}
}
