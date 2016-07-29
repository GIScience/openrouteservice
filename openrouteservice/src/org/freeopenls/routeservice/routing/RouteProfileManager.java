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

package org.freeopenls.routeservice.routing;

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
import org.freeopenls.routeservice.graphhopper.extensions.flagencoders.*;
import org.freeopenls.routeservice.isochrones.IsochroneMap;
import org.freeopenls.routeservice.routing.configuration.RouteManagerConfiguration;
import org.freeopenls.routeservice.routing.configuration.RouteProfileConfiguration;
import org.freeopenls.routeservice.traffic.RealTrafficDataProvider;
import org.freeopenls.tools.CoordTools;
import org.freeopenls.tools.MemoryUtility;
import org.freeopenls.tools.TimeUtility;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.DistanceCalcEarth;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class RouteProfileManager {
	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(RouteProfileManager.class.getName());

	private RouteProfilesCollection m_routeProfiles;
	private RouteProfilesUpdater m_profileUpdater;
	private static RouteProfileManager mInstance;
	private double mDynamicWeightingMaxDistance = 0; 

	public static synchronized RouteProfileManager getInstance() throws IOException {
		if (mInstance == null)
		{
			mInstance = new RouteProfileManager();
			mInstance.initialize("../GraphGH.properties.xml");
		}
		
		return mInstance;
	}

	public RouteProfileManager() {
	}
	
	
	private void registerFlagEncoders()
	{
		// Register all custom EdgeFlagaEncoders here.
		EncodingManager.registerDefaultEdgeFlagEncoder("wheelchair", new WheelchairFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("safetybike", new SafetyBikeFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("cycletourbike", new CycleTourBikeFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("offroadvehicle", new OffRoadVehicleFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("cartmc", new CarTmcFlagEncoder());
		EncodingManager.registerDefaultEdgeFlagEncoder("heavyvehicle", new HeavyVehicleFlagEncoder());
		// EncodingManager.registerDefaultEdgeFlagEncoder("ELECTRO_VEHICLE",
	}
	
	public void prepareGraphs(String graphProps)
	{
		mLogger.info("*  Start preparing RouteProfileManager ...");
		MemoryUtility.logRAMInformations(mLogger);

		long startTime = System.currentTimeMillis();
		
		try
		{
			registerFlagEncoders();
			
			RouteManagerConfiguration rmc = RouteManagerConfiguration.loadFromFile(graphProps);
			RouteProfilesCollection coll = new RouteProfilesCollection();
			int nRouteInstances = rmc.RoutePreferences.length;
			
			for (int i = 0; i < nRouteInstances; i++) {
				RouteProfileConfiguration rpc = rmc.RoutePreferences[i];
				if (!rpc.Enabled)
					continue;

				mLogger.info("Preparing route profile in "  + rpc.GraphLocation + " ...");

				RouteProfile rp = new RouteProfile(rmc.OsmFile, rmc.ConfigPathsRoot, rpc, coll);
				rp.close();
				
				mLogger.info("Done.");
			}
			
	 	    mLogger.info("*  Graphs were prepaired in " + TimeUtility.getElapsedTime(startTime, true) + ".");
		}
		catch(Exception ex)
		{
			mLogger.error(ex);
		}
	
		MemoryUtility.clearMemAndLogRAM(mLogger);
	}
	
	public void initialize(String graphProps) {

		mLogger.info("*  Start preparing RouteProfileManager ...");
		MemoryUtility.logRAMInformations(mLogger);

		long startTime = System.currentTimeMillis();
		
		try {
			RouteManagerConfiguration rmc = RouteManagerConfiguration.loadFromFile(graphProps);
			mDynamicWeightingMaxDistance = rmc.DynamicWeightingMaxDistance;
			
			if ("PrepareGraphs".equalsIgnoreCase(rmc.Mode)) {
				prepareGraphs(graphProps);
			} else {
				registerFlagEncoders();

				m_routeProfiles = new RouteProfilesCollection();
				int nRouteInstances = rmc.RoutePreferences.length;

				ExecutorService executor = Executors.newFixedThreadPool(rmc.InitializationThreads);
				List<Future<RouteProfile>> list = new ArrayList<Future<RouteProfile>>();
				for (int i = 0; i < nRouteInstances; i++) {
					RouteProfileConfiguration rpc = rmc.RoutePreferences[i];
					if (!rpc.Enabled)
						continue;

					Integer[] routePrefs = rpc.GetPreferences();

					if (routePrefs != null) {
						Callable<RouteProfile> worker = new RouteProfileLoader(rmc.OsmFile, rmc.ConfigPathsRoot, rpc,
								m_routeProfiles);
						Future<RouteProfile> submit = executor.submit(worker);
						list.add(submit);
					}
				}

				// now retrieve the result
				for (Future<RouteProfile> future : list) {
					try {
						RouteProfile rp = future.get();

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
					m_profileUpdater = new RouteProfilesUpdater(rmc.UpdateConfig, m_routeProfiles);
					m_profileUpdater.start();
				}
				
				mLogger.info("*  RouteProfileManager is ready. Took " + TimeUtility.getElapsedTime(startTime, true) + ".");
			}
		} catch (Exception ex) {
			mLogger.error(ex);
		}
		
		MemoryUtility.clearMemAndLogRAM(mLogger);
	}

	public void destroy() {
		if (m_profileUpdater != null)
			m_profileUpdater.destroy();
		
		if (RealTrafficDataProvider.getInstance().isInitialized())
			RealTrafficDataProvider.getInstance().destroy();

		m_routeProfiles.destroy();
	}
	
	public RouteProfilesCollection getProfiles()
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
	
	public GHResponse getPath(RoutePlan routePlan, double lat0, double lon0, double lat1, double lon1, short code) throws Exception {
		int routePref = routePlan.getRoutePreference();
		
		boolean dynamicWeights = (routePlan.hasAvoidAreas() || routePlan.hasAvoidFeatures() || routePlan.getMaxSpeed() > 0 || (RoutePreferenceType.isCar(routePref) && (routePlan.hasVehicleAttributes() || routePlan.getUseRealTimeTraffic())) || (routePlan.getWeightingMethod() == WeightingMethod.SHORTEST || routePlan.getWeightingMethod() == WeightingMethod.RECOMMENDED) || routePlan.getSupportTurnRestrictions() || routePlan.getSurfaceInformation());
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
				routePlan.setWeightingMethod(WeightingMethod.FASTEST);
				routePlan.setMaxSpeed(-1);
				routePlan.setAvoidAreas(new ArrayList<Polygon>());
				routePlan.setAvoidFeatureTypes(-1);
				routePlan.setSupportTurnRestrictions(false);
				routePlan.setLoadCharacteristics(null);
			}
		}

		RouteProfile rp = m_routeProfiles.getRouteProfile(routePref, chEnabled, dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
		
		if (rp == null && checkDistance) {
			if (chEnabled == true) {
				rp = m_routeProfiles.getRouteProfile(routePref, false, dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
				if (rp == null)
					rp = m_routeProfiles.getRouteProfile(routePref, false, !dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
			} else {
				rp = m_routeProfiles.getRouteProfile(routePref, false, !dynamicWeights, lat0, lon0, lat1, lon1, checkDistance);
			}
		}

		if (rp == null) {
			if (checkDistance)
				throw new Exception("Unable to get an appropriate route profile instance for RoutePreference = " + RoutePreferenceType.getName(routePref));
			else
				throw new Exception("The requested route is too long and therefore cann't be processed due to a lack of resources.");
		}

		return rp.getRoute(lat0, lon0, lat1, lon1, routePlan, code);
	}
	
	public IsochroneMap buildIsochroneMap(double cx, double cy, double maxCost, String prefType, String method, double interval, double gridSize) throws Exception
	{
		int routePref = RoutePreferenceType.getFromString(prefType);
		RouteProfile rp = m_routeProfiles.getRouteProfile(routePref, false, false);
		
		if (rp == null)
			rp = m_routeProfiles.getRouteProfile(routePref, false, true);
		
		return rp.buildIsochroneMap(cx, cy, maxCost, prefType, method, interval, gridSize);
	}
}
