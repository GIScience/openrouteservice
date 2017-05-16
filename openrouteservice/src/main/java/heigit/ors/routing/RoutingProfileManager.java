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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import heigit.ors.routing.graphhopper.extensions.flagencoders.*;
import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.pathprocessors.ElevationSmoothPathProcessor;
import heigit.ors.routing.pathprocessors.ExtraInfoProcessor;
import heigit.ors.routing.configuration.RoutingManagerConfiguration;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.services.routing.RoutingRequest;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.FormatUtility;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.ServerLimitExceededException;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.routing.RoutingProfilesCollection;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.util.RuntimeUtility;
import heigit.ors.util.TimeUtility;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.BikeCommonFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.storage.RAMDataAccess;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

public class RoutingProfileManager {
	private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());

	private RoutingProfilesCollection _routeProfiles;
	private RoutingProfilesUpdater _profileUpdater;
	private static RoutingProfileManager mInstance;

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

			RoutingManagerConfiguration rmc = RoutingManagerConfiguration.loadFromFile(graphProps);
			RoutingProfilesCollection coll = new RoutingProfilesCollection();
			RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
			int nRouteInstances = rmc.Profiles.length;

			for (int i = 0; i < nRouteInstances; i++) {
				RouteProfileConfiguration rpc = rmc.Profiles[i];
				if (!rpc.getEnabled())
					continue;

				LOGGER.info("Preparing route profile in "  + rpc.getGraphPath() + " ...");

				RoutingProfile rp = new RoutingProfile(RoutingServiceSettings.getSourceFile(), rpc, coll, loadCntx);

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

		try 
		{
			if (RoutingServiceSettings.getEnabled())
			{
				RoutingManagerConfiguration rmc = RoutingManagerConfiguration.loadFromFile(graphProps);

				LOGGER.info(String.format("====> Initializing profiles (%d threads) ...", RoutingServiceSettings.getInitializationThreads()));
				LOGGER.info("                              ");

				DistanceCalc3D.ASIN_APPROXIMATION = RoutingServiceSettings.getDistanceApproximation();
				RAMDataAccess.LZ4_COMPRESSION_ENABLED = "LZ4".equalsIgnoreCase(RoutingServiceSettings.getStorageFormat());	
				BikeCommonFlagEncoder.SKIP_WAY_TYPE_INFO = true;

				if ("PrepareGraphs".equalsIgnoreCase(RoutingServiceSettings.getWorkingMode())) {
					prepareGraphs(graphProps);
				} else {
					registerFlagEncoders();

					_routeProfiles = new RoutingProfilesCollection();
					int nRouteInstances = rmc.Profiles.length;

					RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext(RoutingServiceSettings.getInitializationThreads());
					ExecutorService executor = Executors.newFixedThreadPool(RoutingServiceSettings.getInitializationThreads());
					ExecutorCompletionService<RoutingProfile> compService = new ExecutorCompletionService<RoutingProfile>(executor);

					int nTotalTasks = 0;
					int j = 1; 
					for (int i = 0; i < nRouteInstances; i++) {
						RouteProfileConfiguration rpc = rmc.Profiles[i];
						if (!rpc.getEnabled())
							continue;

						LOGGER.info(String.format("[%d] Profiles: '%s', location: '%s'.", j, rpc.getProfiles(), rpc.getGraphPath()));

						Integer[] routeProfiles = rpc.getProfilesTypes();

						if (routeProfiles != null) {
							Callable<RoutingProfile> task = new RoutingProfileLoader(RoutingServiceSettings.getSourceFile(), rpc,
									_routeProfiles, loadCntx);
							compService.submit(task);
							nTotalTasks++;
						}

						j++;
					}

					LOGGER.info("               ");

					int nCompletedTasks = 0;
					while (nCompletedTasks < nTotalTasks)
					{
						Future<RoutingProfile> future = compService.take();

						try {
							RoutingProfile rp = future.get();
							nCompletedTasks ++;
							if (!_routeProfiles.add(rp))
								LOGGER.warn("Routing profile has already been added.");
						} catch (InterruptedException e) {
							LOGGER.error(e);
							e.printStackTrace();
						} catch (ExecutionException e) {
							LOGGER.error(e);
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
				
				RoutingProfileManagerStatus.setReady(true);
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to initialize RoutingProfileManager instance.", ex);
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

		RoutingProfile rp = getRouteProfile(req);

		RouteSearchParameters searchParams = req.getSearchParameters();

		PathProcessor pathProcessor = null;

		if (req.getExtraInfo() > 0)
		{
			// do not allow geometry simplification when extras are requested
			req.setSimplifyGeometry(false);

			pathProcessor = new ExtraInfoProcessor(rp.getGraphhopper(), req.getExtraInfo());
		}
		else
		{ 
			if (req.getIncludeElevation())
				pathProcessor = new ElevationSmoothPathProcessor();
		}

		Coordinate[] coords = req.getCoordinates();
		Coordinate c0 = coords[0];
		Coordinate c1;
		int nSegments = coords.length - 1;
		RouteProcessContext routeProcCntx = new RouteProcessContext(pathProcessor);

		for(int i = 1; i <= nSegments; ++i)
		{
			if (pathProcessor != null)
				pathProcessor.setSegmentIndex(i - 1, nSegments);

			c1 = coords[i];
			GHResponse gr = rp.getRoute(c0.y, c0.x, c1.y, c1.x, c0.z == 1.0, searchParams, req.getSimplifyGeometry(), routeProcCntx);

			if (gr.hasErrors())
				throw new InternalServerException(RoutingErrorCodes.UNKNOWN, String.format("Unable to find a route between points %d (%s) and %d (%s)", i, FormatUtility.formatCoordinate(c0), i + 1, FormatUtility.formatCoordinate(c1)));

			routes.add(gr);
			c0 = c1;
		}

		return new RouteResultBuilder().createRouteResult(routes, req, (pathProcessor != null && (pathProcessor instanceof ExtraInfoProcessor)) ? ((ExtraInfoProcessor)pathProcessor).getExtras(): null);
	}

	public RoutingProfile getRouteProfile(RoutingRequest req) throws Exception {
		RouteSearchParameters searchParams = req.getSearchParameters();
		int profileType = searchParams.getProfileType();

		boolean dynamicWeights = (searchParams.hasAvoidAreas() || searchParams.hasAvoidFeatures() || searchParams.getMaximumSpeed() > 0 || (RoutingProfileType.isDriving(profileType) && (searchParams.hasParameters(VehicleParameters.class) || searchParams.getConsiderTraffic())) || (searchParams.getWeightingMethod() == WeightingMethod.SHORTEST || searchParams.getWeightingMethod() == WeightingMethod.RECOMMENDED) || searchParams.getConsiderTurnRestrictions() /*|| RouteExtraInformationFlag.isSet(extraInfo, value) searchParams.getIncludeWaySurfaceInfo()*/);

		RoutingProfile rp = _routeProfiles.getRouteProfile(profileType, !dynamicWeights);

		if (rp == null && dynamicWeights == false)
			rp = _routeProfiles.getRouteProfile(profileType, false);

		if (rp == null)
			throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to get an appropriate route profile for RoutePreference = " + RoutingProfileType.getName(req.getSearchParameters().getProfileType()));

		RouteProfileConfiguration config = rp.getConfiguration();

		if (config.getMaximumDistance() > 0 || (dynamicWeights && config.getMaximumSegmentDistanceWithDynamicWeights() > 0) || config.getMaximumWayPoints() > 0)
		{
			Coordinate[] coords = req.getCoordinates();
			int nCoords = coords.length;
			if (config.getMaximumWayPoints() > 0)
			{
				if (nCoords > config.getMaximumWayPoints())
					throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The specified number of waypoints must not be greater than " + Integer.toString(config.getMaximumWayPoints()) + ".");
			}

			if (config.getMaximumDistance() > 0 || (dynamicWeights && config.getMaximumSegmentDistanceWithDynamicWeights() > 0))
			{
				double totalDist =  0.0;
				double longestSegmentDist = 0.0;
				DistanceCalc distCalc = Helper.DIST_EARTH;

				Coordinate c0 = coords[0], c1 = null;

				if (nCoords == 2)
				{
					c1 = coords[1];
					totalDist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
					longestSegmentDist = totalDist;
				}
				else
				{
					double dist = 0;
					for(int i = 1; i < nCoords; i++)
					{
						c1 = coords[i];
						dist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
						totalDist += dist;
						if (dist > longestSegmentDist)
							longestSegmentDist = dist;

						c0 = c1;
					}
				}

				if (config.getMaximumDistance() > 0 && totalDist > config.getMaximumDistance())
					throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The approximated route distance must not be greater than " + Double.toString(config.getMaximumDistance()) + " meters.");
				if (dynamicWeights && config.getMaximumSegmentDistanceWithDynamicWeights() > 0 && longestSegmentDist > config.getMaximumSegmentDistanceWithDynamicWeights())
					throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "By dynamic weighting, the approximated distance of a route segment must not be greater than " + Double.toString(config.getMaximumSegmentDistanceWithDynamicWeights()) + " meters.");
			}
		}

		return rp;
	}

	public IsochroneMap buildIsochrone(IsochroneSearchParameters parameters) throws Exception
	{
		int profileType = parameters.getRouteParameters().getProfileType();
		RoutingProfile rp = _routeProfiles.getRouteProfile(profileType, false);

		return rp.buildIsochrone(parameters);
	}
}
