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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import heigit.ors.routing.graphhopper.extensions.GraphProcessContext;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.ORSGraphStorageFactory;
import heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.parameters.*;
import heigit.ors.routing.graphhopper.extensions.edgefilters.*;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneMapBuilderFactory;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.mapmatching.MapMatcher;
import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.mapmatching.hmm.HiddenMarkovMapMatcher;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.routing.traffic.TrafficEdgeAnnotator;
import heigit.ors.util.RuntimeUtility;
import heigit.ors.util.TimeUtility;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EdgeFilterSequence;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;

import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;

public class RoutingProfile 
{
	private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());
	private static int profileIdentifier = 0;
	private static final Object lockObj = new Object();

	private ORSGraphHopper mGraphHopper;
	private boolean mUseTrafficInfo;
	private Integer[] mRoutePrefs;
	private Integer mUseCounter;
	private boolean mUpdateRun;
	private MapMatcher mMapMatcher;

	private RouteProfileConfiguration _config;

	public RoutingProfile(String osmFile, RouteProfileConfiguration rpc, RoutingProfilesCollection profiles, RoutingProfileLoadContext loadCntx) throws Exception {
		mRoutePrefs = rpc.getProfilesTypes();
		mUseCounter = 0;
		mUseTrafficInfo = /*mHasDynamicWeights &&*/ hasCarPreferences() ? rpc.getUseTrafficInformation() : false;

		mGraphHopper = initGraphHopper(osmFile, rpc, profiles, loadCntx);

		_config = rpc;
	}

	public static ORSGraphHopper initGraphHopper(String osmFile, RouteProfileConfiguration config, RoutingProfilesCollection profiles, RoutingProfileLoadContext loadCntx) throws Exception {
		CmdArgs args = createGHSettings(osmFile, config);

		RoutingProfile refProfile = null;

		try
		{
			refProfile = profiles.getRouteProfile(RoutingProfileType.DRIVING_CAR);
		}
		catch(Exception ex)
		{}

		int profileId = 0;
		synchronized (lockObj) {
			profileIdentifier++;
			profileId = profileIdentifier;
		}

		long startTime = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled())
			LOGGER.info(String.format("[%d] Building/loading graphs....", profileId));

		GraphProcessContext gpc = new GraphProcessContext(config);

		ORSGraphHopper gh = (ORSGraphHopper) new ORSGraphHopper(gpc, config.getUseTrafficInformation(), refProfile);

		ORSDefaultFlagEncoderFactory flagEncoderFactory = new ORSDefaultFlagEncoderFactory();
		gh.setFlagEncoderFactory(flagEncoderFactory);
		gh.setFlagEncoderFactory(flagEncoderFactory);

		gh.init(args);
		
		gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
		gh.setWeightingFactory(new ORSWeightingFactory(RealTrafficDataProvider.getInstance()));

		if (!Helper.isEmpty(config.getElevationProvider()) && !Helper.isEmpty(config.getElevationCachePath()))
		{
			ElevationProvider elevProvider = loadCntx.getElevationProvider(config.getElevationProvider(), config.getElevationCachePath(), config.getElevationDataAccess(), config.getElevationCacheClear());
			gh.setElevationProvider(elevProvider);
		}

		gh.importOrLoad();

		if (LOGGER.isInfoEnabled())
		{
			EncodingManager encodingMgr = gh.getEncodingManager();
			LOGGER.info(String.format("[%d] FlagEncoders: %s, bits used %d/%d.", profileId, encodingMgr.fetchEdgeEncoders().size(), encodingMgr.getUsedBitsForFlags(), encodingMgr.getBytesForFlags()*8));
			GraphHopperStorage graph = gh.getGraphHopperStorage();
			LOGGER.info(String.format("[%d] Capacity: main - %s, ext. storages - %s.", profileId, RuntimeUtility.getMemorySize(graph.getCapacity()), RuntimeUtility.getMemorySize(GraphStorageUtils.getCapacity(graph.getExtension()))));
			LOGGER.info(String.format("[%d] Total time: %s.", profileId, TimeUtility.getElapsedTime(startTime, true)));
			LOGGER.info(String.format("[%d] Finished at: %s.", profileId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
			LOGGER.info("                              ");
		}

		// Make a stamp which help tracking any changes in the size of OSM file.
		File file = new File(osmFile);
		Path pathTimestamp = Paths.get(config.getGraphPath(), "stamp.txt");
		File file2 = pathTimestamp.toFile();
		if (!file2.exists())
			Files.write(pathTimestamp, Long.toString(file.length()).getBytes());

		return gh;
	}

	public long getCapacity()
	{
		GraphHopperStorage graph = mGraphHopper.getGraphHopperStorage();
		return graph.getCapacity() + GraphStorageUtils.getCapacity(graph.getExtension());
	}

	private static CmdArgs createGHSettings(String sourceFile, RouteProfileConfiguration config)
	{
		CmdArgs args = new CmdArgs();
		args.put("graph.dataaccess", "RAM_STORE");
		args.put("datareader.file", sourceFile);
		args.put("graph.location", config.getGraphPath());
		args.put("graph.bytesForFlags", config.getEncoderFlagsSize());

		if (config.getInstructions() == false)
			args.put("instructions", false);
		if (config.getElevationProvider() != null&& config.getElevationCachePath() != null)
		{
			args.put("graph.elevation.provider", config.getElevationProvider());
			args.put("graph.elevation.cachedir", config.getElevationCachePath());
			args.put("graph.elevation.dataaccess", config.getElevationDataAccess());
		}

		args.put("prepare.ch.weightings", (config.getCHWeighting() != null) ? config.getCHWeighting() : "no");
		args.put("prepare.ch.threads", config.getCHThreads());

		String flagEncoders = "";
		String[] encoderOpts = !Helper.isEmpty(config.getEncoderOptions()) ? config.getEncoderOptions().split(",") : null;
		Integer[] profiles = config.getProfilesTypes();
		for (int i = 0; i < profiles.length; i++)
		{
			if (encoderOpts == null)
				flagEncoders += RoutingProfileType.getEncoderName(profiles[i]);
			else
				flagEncoders += RoutingProfileType.getEncoderName(profiles[i]) + "|"+encoderOpts[i];
			if (i < profiles.length - 1)
				flagEncoders += ",";
		}

		args.put("graph.flagEncoders", flagEncoders.toLowerCase());

		args.put("osmreader.wayPointMaxDistance",1);
		args.put("index.highResolution", 500);

		return args;
	}

	public HashMap<Integer, Long> getTmcEdges() {
		return mGraphHopper.getTmcGraphEdges();
	}

	public HashMap<Long, ArrayList<Integer>> getOsmId2edgeIds() {
		return mGraphHopper.getOsmId2EdgeIds();
	}

	public ORSGraphHopper getGraphhopper() {
		return mGraphHopper;
	}

	public BBox getBounds() {
		return mGraphHopper.getGraphHopperStorage().getBounds();
	}

	public StorableProperties getGraphProperties() {
		StorableProperties props = mGraphHopper.getGraphHopperStorage().getProperties();
		return props;
	}

	public String getGraphLocation() {
		return mGraphHopper == null ? null : mGraphHopper.getGraphHopperStorage().getDirectory().toString();
	}

	public RouteProfileConfiguration getConfiguration() {
		return _config;
	}

	public Integer[] getPreferences() {
		return mRoutePrefs;
	}

	public boolean hasCarPreferences() {
		for (int i = 0; i < mRoutePrefs.length; i++) {
			if (RoutingProfileType.isDriving(mRoutePrefs[i]))
				return true;
		}

		return false;
	}


	public boolean isCHEnabled() {
		return mGraphHopper != null && mGraphHopper.isCHEnabled();
	}

	public boolean useTrafficInformation() {
		return mUseTrafficInfo;
	}

	public void close() {
		mGraphHopper.close();
	}

	private synchronized boolean isGHUsed() {
		return mUseCounter > 0;
	}

	private synchronized void beginUseGH() {
		mUseCounter++;
	}

	private synchronized void endUseGH() {
		mUseCounter--;
	}

	public void updateGH(GraphHopper gh) throws Exception {
		if (gh == null)
			throw new Exception("GraphHopper instance is null.");

		try {
			mUpdateRun = true;
			while (true) {
				if (!isGHUsed()) {
					GraphHopper ghOld = mGraphHopper;

					ghOld.close();
					ghOld.clean();

					gh.close();
					// gh.clean(); // do not remove on-disk files, we need to
					// copy them as follows

					RuntimeUtility.clearMemory(LOGGER);

					// Change the content of the graph folder
					String oldLocation = ghOld.getGraphHopperLocation();
					File dstDir = new File(oldLocation);
					File srcDir = new File(gh.getGraphHopperLocation());
					FileUtils.copyDirectory(srcDir, dstDir, true);
					FileUtils.deleteDirectory(srcDir);

					RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();

					mGraphHopper = initGraphHopper(ghOld.getDataReaderFile(), _config, RoutingProfileManager.getInstance().getProfiles(), loadCntx);

					loadCntx.release();

					break;
				}

				Thread.sleep(2000);
			}
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
		}

		mUpdateRun = false;
	}

	private void waitForUpdateCompletion() throws Exception {
		if (mUpdateRun) {
			long startTime = System.currentTimeMillis();

			while (mUpdateRun) {
				long curTime = System.currentTimeMillis();
				if (curTime - startTime > 600000) {
					throw new Exception("The route profile is currently being updated.");
				}

				Thread.sleep(1000);
			}
		}
	}

	public IsochroneMap buildIsochrone(IsochroneSearchParameters parameters) throws Exception {
		IsochroneMap result = null;

		waitForUpdateCompletion();

		beginUseGH();

		try {
			RouteSearchContext searchCntx = createSearchContext(parameters.getRouteParameters(), RouteSearchMode.Isochrones);

			IsochroneMapBuilderFactory isochroneMapBuilderFactory = new IsochroneMapBuilderFactory(searchCntx);
			result = isochroneMapBuilderFactory.buildMap(parameters);

			endUseGH();
		} catch (Exception ex) {
			endUseGH();

			LOGGER.error(ex);

			throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to build an isochrone map.");
		}

		return result;
	}

	private RouteSearchContext createSearchContext(RouteSearchParameters searchParams, RouteSearchMode mode) throws Exception
	{
		int profileType = searchParams.getProfileType();
		int weightingMethod = searchParams.getWeightingMethod();
		String encoderName = RoutingProfileType.getEncoderName(profileType);
		EdgeFilter edgeFilter = null;
		FlagEncoder flagEncoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);
		//String algorithm = null;
		PMap props = new PMap();

		if (searchParams.hasAvoidAreas()) {
			if (encoderName.isEmpty())
				throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "vehicle parameter is empty.");

			if (!mGraphHopper.getEncodingManager().supports(encoderName)) {
				throw new IllegalArgumentException("Vehicle " + encoderName + " unsupported. " + "Supported are: "
						+ mGraphHopper.getEncodingManager());
			}

			edgeFilter = new AvoidAreasEdgeFilter(flagEncoder, searchParams.getAvoidAreas());
		}

		if (RoutingProfileType.isDriving(profileType)) {
			if (RoutingProfileType.isHeavyVehicle(profileType)) {
				edgeFilter = createHeavyVehicleEdgeFilter(searchParams, flagEncoder, edgeFilter);
			} else if (searchParams.hasParameters(VehicleParameters.class)) {
				//edgeFilter = createWayRestrictionsEdgeFilter(searchParams, flagEncoder, edgeFilter);
			}
		} else if (profileType == RoutingProfileType.WHEELCHAIR) {
			if (searchParams.hasParameters(WheelchairParameters.class)) {
				edgeFilter = createWheelchairRestrictionsEdgeFilter(searchParams,  flagEncoder,
						edgeFilter);
			}
		}

		boolean bSteepness = false;

		if (searchParams.hasAvoidFeatures() ) {
			if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)
					|| profileType == RoutingProfileType.FOOT_WALKING || profileType == RoutingProfileType.FOOT_HIKING
					|| profileType == RoutingProfileType.WHEELCHAIR) { 

				if (searchParams.getAvoidFeatureTypes() != AvoidFeatureFlags.Hills)
				{
					EdgeFilter ef = new AvoidFeaturesEdgeFilter(flagEncoder, searchParams.getAvoidFeatureTypes(),
							mGraphHopper.getGraphHopperStorage());
					edgeFilter = createEdgeFilter(ef, edgeFilter);
				}

				if (mode == RouteSearchMode.Routing)
				{
					if ((searchParams.getAvoidFeatureTypes() & AvoidFeatureFlags.Hills) == AvoidFeatureFlags.Hills)
					{
						props.put("AvoidHills", true);

						if (searchParams.hasParameters(CyclingParameters.class))
						{
							CyclingParameters cyclingParams = (CyclingParameters)searchParams.getProfileParameters();

							props.put("SteepnessMaximum", cyclingParams.getMaximumGradient());
						}

						bSteepness = true;
					}
				}
			}
		}

		if (!((searchParams.getAvoidFeatureTypes() & AvoidFeatureFlags.Hills) == AvoidFeatureFlags.Hills))
		{
			if (searchParams.hasParameters(CyclingParameters.class))
			{
				CyclingParameters cyclingParams = (CyclingParameters)searchParams.getProfileParameters();

				if (cyclingParams.getDifficultyLevel() >= 0 || cyclingParams.getMaximumGradient() > 0)
				{
					if (mode == RouteSearchMode.Routing)
					{
						props.put("SteepnessDifficulty", true);
						props.put("SteepnessDifficultyLevel", cyclingParams.getDifficultyLevel());
						props.put("SteepnessMaximum", cyclingParams.getMaximumGradient());
						bSteepness = true;
					}
					else
					{
						EdgeFilter ef = new AvoidSteepnessEdgeFilter(flagEncoder, mGraphHopper.getGraphHopperStorage(), cyclingParams.getMaximumGradient());
						edgeFilter = createEdgeFilter(ef, edgeFilter);
					}
				}
			}
		}

		if (searchParams.hasParameters(WalkingParameters.class)) {
			WalkingParameters walkingParams = (WalkingParameters)searchParams.getProfileParameters();
			if (walkingParams.getGreenRouting() && mode == RouteSearchMode.Routing) {
				props.put("GreenRouting", true);
			}
		}

		/*	if (bSteepness)
			algorithm = "dijkstra";*/

		if (searchParams.getConsiderTraffic()/* && mHasDynamicWeights */) {
			if (RoutingProfileType.isDriving(profileType) && weightingMethod != WeightingMethod.SHORTEST
					&& RealTrafficDataProvider.getInstance().isInitialized()) {
				props.put("TrafficBlockWeighting", true);

				EdgeFilter ef = new BlockedEdgesEdgeFilter(flagEncoder, RealTrafficDataProvider.getInstance()
						.getBlockedEdges(mGraphHopper.getGraphHopperStorage()), RealTrafficDataProvider.getInstance()
						.getHeavyVehicleBlockedEdges(mGraphHopper.getGraphHopperStorage()));

				edgeFilter = createEdgeFilter(ef, edgeFilter);
			}
		}

		if (edgeFilter == null)
			edgeFilter = new DefaultEdgeFilter(flagEncoder);

		RouteSearchContext searchCntx = new RouteSearchContext(mGraphHopper, edgeFilter, flagEncoder);
		searchCntx.setProperties(props);

		return searchCntx;		
	}

	public RouteSegmentInfo[] getMatchedSegments(double lat0, double lon0, double lat1, double lon1, double searchRadius, boolean bothDirections)
			throws Exception {
		RouteSegmentInfo[] rsi = null;

		waitForUpdateCompletion();

		beginUseGH();

		try {
			rsi = getMatchedSegmentsInternal(lat0, lon0, lat1, lon1, searchRadius, null, bothDirections);

			endUseGH();
		} catch (Exception ex) {
			endUseGH();

			throw ex;
		}

		return rsi;
	}

	private RouteSegmentInfo[] getMatchedSegmentsInternal(double lat0, double lon0, double lat1, double lon1,
			double searchRadius, EdgeFilter edgeFilter, boolean bothDirections) {
		if (mMapMatcher ==  null)
		{
			mMapMatcher = new HiddenMarkovMapMatcher();
			mMapMatcher.setGraphHopper(mGraphHopper);
		}

		mMapMatcher.setSearchRadius(searchRadius);
		mMapMatcher.setEdgeFilter(edgeFilter);

		return mMapMatcher.match(lat0, lon0, lat1, lon1, bothDirections);
	}

	public boolean canProcessRequest(double totalDistance, double longestSegmentDistance, int wayPoints) {
		double maxDistance = (_config.getMaximumDistance() > 0) ? _config.getMaximumDistance(): Double.MAX_VALUE;
		int maxWayPoints = (_config.getMaximumWayPoints() > 0) ? _config.getMaximumWayPoints(): Integer.MAX_VALUE;
		
    	return totalDistance <= maxDistance && wayPoints <= maxWayPoints;
	}
	
	public GHResponse getRoute(double lat0, double lon0, double lat1, double lon1, boolean directedSegment, RouteSearchParameters searchParams, boolean simplifyGeometry, RouteProcessContext routeProcCntx)
			throws Exception {

		GHResponse resp = null; 

		waitForUpdateCompletion();

		beginUseGH();

		try {
			int profileType = searchParams.getProfileType();
			int weightingMethod = searchParams.getWeightingMethod();
			RouteSearchContext searchCntx = createSearchContext(searchParams, RouteSearchMode.Routing);

			boolean flexibleMode = false;
			GHRequest req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1));
			req.setVehicle(searchCntx.getEncoder().toString());
			req.setAlgorithm("dijkstrabi");
			req.setMaxSpeed(searchParams.getMaximumSpeed());
			req.setSimplifyGeometry(simplifyGeometry);

			PMap props = searchCntx.getProperties();
			if (props != null && props.size() > 0)
				req.getHints().merge(props);

			if (supportWeightingMethod(profileType)) {
				if (weightingMethod == WeightingMethod.FASTEST)
					req.setWeighting("fastest");
				else if (weightingMethod == WeightingMethod.SHORTEST)
				{
					req.setWeighting("shortest");
					flexibleMode = true;
				}
				else if (weightingMethod == WeightingMethod.RECOMMENDED)
				{
					req.setWeighting("recommended");
					flexibleMode = true;
				}
			} 

			if ((profileType == RoutingProfileType.CYCLING_TOUR || profileType == RoutingProfileType.CYCLING_MOUNTAIN)
					&& weightingMethod == WeightingMethod.FASTEST) {
				req.setWeighting("recommended");
				flexibleMode = true;
			}

			if ((profileType == RoutingProfileType.CYCLING_TOUR || (profileType == RoutingProfileType.DRIVING_HGV && HeavyVehicleAttributes.HGV == searchParams
					.getVehicleType())) && weightingMethod == WeightingMethod.RECOMMENDED) {
				req.setWeighting("recommended_pref");
				flexibleMode = true;
			}

			if (RoutingProfileType.isDriving(profileType) && RealTrafficDataProvider.getInstance().isInitialized())
				req.setEdgeAnnotator(new TrafficEdgeAnnotator(mGraphHopper.getGraphHopperStorage()));

			if (searchCntx.getEdgeFilter() != null) 
				req.setEdgeFilter(searchCntx.getEdgeFilter());

			if (routeProcCntx.getPathProcessor() != null)
				req.setPathProcessor(routeProcCntx.getPathProcessor());
			
			if (useDynamicWeights(searchParams) || flexibleMode)
				req.getHints().put("CH.Disable", true);
			
			/*if (directedSegment)
				resp = mGraphHopper.directRoute(req); NOTE IMPLEMENTED!!!
			else */
				resp = mGraphHopper.route(req, routeProcCntx.getArrayBuffer());

			endUseGH();
		} catch (Exception ex) {
			endUseGH();

			LOGGER.error(ex);
			
			throw new InternalServerException(RoutingErrorCodes.UNKNOWN, String.format("Unable to compute a route between coordinates %s-%s", Double.toString(lon0) + ", " + Double.toString(lat0),  Double.toString(lon1) + ", " + Double.toString(lat1)));
		}

		return resp;
	}
	
	private boolean useDynamicWeights(RouteSearchParameters searchParams)
	{
		boolean dynamicWeights = (searchParams.hasAvoidAreas() || searchParams.hasAvoidFeatures() || searchParams.getMaximumSpeed() > 0 || (RoutingProfileType.isDriving(searchParams.getProfileType()) && (searchParams.hasParameters(VehicleParameters.class) || searchParams.getConsiderTraffic())) || (searchParams.getWeightingMethod() == WeightingMethod.SHORTEST || searchParams.getWeightingMethod() == WeightingMethod.RECOMMENDED) || searchParams.getConsiderTurnRestrictions() /*|| RouteExtraInformationFlag.isSet(extraInfo, value) searchParams.getIncludeWaySurfaceInfo()*/);

		return dynamicWeights;
	}

	private EdgeFilter createEdgeFilter(EdgeFilter edgeFilter, EdgeFilter seq) {
		if (seq != null && seq instanceof EdgeFilterSequence) {
			EdgeFilterSequence seqFilter = (EdgeFilterSequence) seq;
			seqFilter.addFilter(edgeFilter);
			return seqFilter;
		} else {
			ArrayList<EdgeFilter> edgeFilters = new ArrayList<EdgeFilter>();
			edgeFilters.add(edgeFilter);
			if (seq != null)
				edgeFilters.add(seq);
			else
				return edgeFilter;
			EdgeFilterSequence seqFilter = new EdgeFilterSequence(edgeFilters);
			return seqFilter;
		}
	}

	private EdgeFilter createWheelchairRestrictionsEdgeFilter(RouteSearchParameters searchParams, 
			FlagEncoder flagEncoder, EdgeFilter edgeFilter) throws Exception {
		if (searchParams.hasParameters(WheelchairParameters.class))
		{
			EdgeFilter ef = null;
			GraphStorage gs = mGraphHopper.getGraphHopperStorage();
			ef = new WheelchairEdgeFilter((WheelchairParameters)searchParams.getProfileParameters(), (WheelchairFlagEncoder) flagEncoder, gs);
			edgeFilter = createEdgeFilter(ef, edgeFilter);
		}
		return edgeFilter;
	}

	private EdgeFilter createHeavyVehicleEdgeFilter(RouteSearchParameters searchParams, FlagEncoder flagEncoder,
			EdgeFilter edgeFilter) 
	{
		if (searchParams.hasParameters(VehicleParameters.class))
		{
			GraphStorage gs = mGraphHopper.getGraphHopperStorage();

			int vehicleType = searchParams.getVehicleType();
			VehicleParameters vehicleParams = (VehicleParameters)searchParams.getProfileParameters();

			if (vehicleParams.hasAttributes())
			{
				EdgeFilter ef = new HeavyVehicleEdgeFilter(flagEncoder, vehicleType, vehicleParams, gs) {	};
				edgeFilter = createEdgeFilter(ef, edgeFilter);
			}
		}

		return edgeFilter;
	}

	private static boolean supportWeightingMethod(int profileType) {
		if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType) || profileType == RoutingProfileType.FOOT_WALKING || profileType == RoutingProfileType.WHEELCHAIR)
			return true;
		else
			return false;
	}

	public Geometry getEdgeGeometry(int edgeId) 	
	{ 		
		return getEdgeGeometry(edgeId, 3, Integer.MIN_VALUE); 	
	}

	public Geometry getEdgeGeometry(int edgeId, int mode, int adjnodeid) 	
	{ 	
		EdgeIteratorState iter = mGraphHopper.getGraphHopperStorage().getEdgeIteratorState(edgeId, adjnodeid); 	
		PointList points = iter.fetchWayGeometry(mode); 	
		if (points.size() > 1) 		{ 		
			Coordinate[] coords = new Coordinate[points.size()]; 	
			for (int i = 0; i < points.size(); i++) { 		
				double x = points.getLon(i); 			
				double y = points.getLat(i); 	
				coords[i] = new Coordinate(x, y); 	
			} 		
			return new GeometryFactory().createLineString(coords); 		} 	
		return null; 
	}

	public int hashCode()
	{
		return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
	}
}
