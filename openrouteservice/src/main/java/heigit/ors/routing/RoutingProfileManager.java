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
	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(RoutingProfileManager.class.getName());

	private RoutingProfilesCollection m_routeProfiles;
	private RoutingProfilesUpdater m_profileUpdater;
	private static RoutingProfileManager mInstance;
	private double mDynamicWeightingMaxDistance = 0; 

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
			int nRouteInstances = rmc.Profiles.length;
			
			for (int i = 0; i < nRouteInstances; i++) {
				RouteProfileConfiguration rpc = rmc.Profiles[i];
				if (!rpc.Enabled)
					continue;

				mLogger.info("Preparing route profile in "  + rpc.GraphPath + " ...");
               
				RoutingProfile rp = new RoutingProfile(rmc.SourceFile, rmc.ConfigPathsRoot, rpc, coll);
				
				rp.close();
				
				mLogger.info("Done.");
			}
			
	 	    mLogger.info("*  Graphs were prepaired in " + TimeUtility.getElapsedTime(startTime, true) + ".");
		}
		catch(Exception ex)
		{
			mLogger.error("Failed to prepare graphs.", ex);
		}
	
		RuntimeUtility.clearMemAndLogRAM(mLogger);
	}
	
	public void initialize(String graphProps) {

		mLogger.info("*  Start preparing RouteProfileManager ...");
		RuntimeUtility.logRAMInformations(mLogger);

		long startTime = System.currentTimeMillis();
		
		try {
			RouteManagerConfiguration rmc = RouteManagerConfiguration.loadFromFile(graphProps);
			mDynamicWeightingMaxDistance = rmc.DynamicWeightingMaxDistance;
			
			if ("PrepareGraphs".equalsIgnoreCase(rmc.Mode)) {
				prepareGraphs(graphProps);
			} else {
				registerFlagEncoders();

				m_routeProfiles = new RoutingProfilesCollection();
				int nRouteInstances = rmc.Profiles.length;

				ExecutorService executor = Executors.newFixedThreadPool(rmc.InitializationThreads);
				List<Future<RoutingProfile>> list = new ArrayList<Future<RoutingProfile>>();
				for (int i = 0; i < nRouteInstances; i++) {
					RouteProfileConfiguration rpc = rmc.Profiles[i];
					if (!rpc.Enabled)
						continue;

					Integer[] routeProfiles = rpc.GetProfiles();

					if (routeProfiles != null) {
						Callable<RoutingProfile> worker = new RoutingProfileLoader(rmc.SourceFile, rmc.ConfigPathsRoot, rpc,
								m_routeProfiles);
						Future<RoutingProfile> submit = executor.submit(worker);
						list.add(submit);
					}
				}

				// now retrieve the result
				for (Future<RoutingProfile> future : list) {
					try {
						RoutingProfile rp = future.get();

						if (!m_routeProfiles.add(rp))
							mLogger.warn("Route preference has already been added.");
						
					} catch (InterruptedException e) {
						mLogger.error(e.getMessage());
						e.printStackTrace();
					} catch (ExecutionException e) {
						mLogger.error(e.getMessage());
						e.printStackTrace();
					}
				}

				executor.shutdown();

				if (rmc.TrafficInfoConfig != null && rmc.TrafficInfoConfig.Enabled) {
					RealTrafficDataProvider.getInstance().initialize(rmc, m_routeProfiles);
				}

				if (rmc.UpdateConfig != null && rmc.UpdateConfig.Enabled) {
					m_profileUpdater = new RoutingProfilesUpdater(rmc.UpdateConfig, m_routeProfiles);
					m_profileUpdater.start();
				}
				
				mLogger.info("*  RouteProfileManager is ready. Took " + TimeUtility.getElapsedTime(startTime, true) + ".");
			}
		} catch (Exception ex) {
			mLogger.error("Failed to initialize RouteProfileManager", ex);
		}
		
		RuntimeUtility.clearMemAndLogRAM(mLogger);
	}

	public void destroy() {
		if (m_profileUpdater != null)
			m_profileUpdater.destroy();
		
		if (RealTrafficDataProvider.getInstance().isInitialized())
			RealTrafficDataProvider.getInstance().destroy();

		m_routeProfiles.destroy();
	}
	
	public RoutingProfilesCollection getProfiles()
	{
		return m_routeProfiles;
	}
	
	public boolean updateEnabled()
	{
		return m_profileUpdater != null; 
	}
	
	public Date getNextUpdateTime()
	{
		return m_profileUpdater == null ? new Date() : m_profileUpdater.getNextUpdate();
	}
	
	public String getUpdatedStatus()
	{
		return m_profileUpdater == null ? null : m_profileUpdater.getStatus();
	}
	
	public RouteResult getRoute(RoutingRequest req) throws Exception
	{
		List<GHResponse> routes = new ArrayList<GHResponse>();

		RouteSearchParameters searchParams = req.getSearchParameters();

		Coordinate[] coords = req.getCoordinates();
		
		RoutingProfile rp = getRouteProfile(coords[0].y, coords[0].x, coords[1].y, coords[1].x, searchParams, false);

		ExtraInfoProcessor extraInfoAggregator = null;

		if (req.getExtraInfo() > 0)
			extraInfoAggregator = new ExtraInfoProcessor(rp.getGraphhopper(), req.getExtraInfo());
		
		Coordinate c0 = coords[0];
		Coordinate c1;
		int nSegments = coords.length - 1;
		
		for(int i = 1; i <= nSegments; ++i)
		{
			if (extraInfoAggregator != null)
				extraInfoAggregator.setSegmentIndex(i - 1, nSegments);
			c1 = coords[i];
			GHResponse gr = rp.getRoute(c0.y, c0.x, c1.y, c1.x, searchParams, c0.z == 1.0, extraInfoAggregator);
			routes.add(gr);
			c0 = c1;
		}

		return new RouteResultBuilder().createRouteResult(routes, req, extraInfoAggregator != null ? extraInfoAggregator.getExtras(): null);
	}
	
	public GHResponse getRoute(double lat0, double lon0, double lat1, double lon1, RouteSearchParameters searchParams, boolean directedSegment, PathProcessor pathProcessor) throws Exception {
		RoutingProfile rp = getRouteProfile(lat0, lon0, lat1, lon1, searchParams, directedSegment);

		return rp.getRoute(lat0, lon0, lat1, lon1, searchParams, directedSegment, pathProcessor);
	}
	
	public RoutingProfile getRouteProfile(double lat0, double lon0, double lat1, double lon1, RouteSearchParameters searchParams, boolean directedSegment) throws Exception {
		int profileType = searchParams.getProfileType();
		
		boolean dynamicWeights = (searchParams.hasAvoidAreas() || searchParams.hasAvoidFeatures() || searchParams.getMaximumSpeed() > 0 || (RoutingProfileType.isDriving(profileType) && (searchParams.hasParameters(VehicleParameters.class) || searchParams.getConsiderTraffic())) || (searchParams.getWeightingMethod() == WeightingMethod.SHORTEST || searchParams.getWeightingMethod() == WeightingMethod.RECOMMENDED) || searchParams.getConsiderTurnRestrictions() /*|| RouteExtraInformationFlag.isSet(extraInfo, value) searchParams.getIncludeWaySurfaceInfo()*/);
		boolean chEnabled = !dynamicWeights;
		boolean checkDistance = true;
		
		if (chEnabled == false && mDynamicWeightingMaxDistance > 0.0)
		{
			double dist = CoordTools.calcDistHaversine(lon0, lat0, lon1, lat1);
			if (dist > mDynamicWeightingMaxDistance)
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
		RoutingProfile rp = m_routeProfiles.getRouteProfile(profileType, chEnabled, dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
		
		if (rp == null && checkDistance) {
			if (chEnabled == true) {
				rp = m_routeProfiles.getRouteProfile(profileType, false, dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
				if (rp == null)
					rp = m_routeProfiles.getRouteProfile(profileType, false, !dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
			} else {
				rp = m_routeProfiles.getRouteProfile(profileType, false, !dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
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
		RoutingProfile rp = m_routeProfiles.getRouteProfile(profileType, false, false);
		
		if (rp == null)
			rp = m_routeProfiles.getRouteProfile(profileType, false, true);
		
		return rp.buildIsochrone(parameters);
	}
}
