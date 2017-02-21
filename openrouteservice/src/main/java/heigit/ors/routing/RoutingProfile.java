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

// Authors M. Rylov

package heigit.ors.routing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.ORSGraphStorageFactory;
import heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageType;
import heigit.ors.routing.parameters.*;
import heigit.ors.routing.graphhopper.extensions.edgefilters.*;
import heigit.ors.isochrones.IsochroneSearchParameters;
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
import heigit.ors.util.CoordTools;
import heigit.ors.util.RuntimeUtility;
import heigit.ors.util.TimeUtility;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EdgeFilterSequence;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.PMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class RoutingProfile {
	private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());
	
	private ORSGraphHopper mGraphHopper;
	private boolean mUseTrafficInfo;
	private Integer[] mRoutePrefs;
	private Integer mUseCounter;
	private boolean mUpdateRun;
	private MapMatcher mMapMatcher;

	private RouteProfileConfiguration mProfileConfig;

	public RoutingProfile(String osmFile, RouteProfileConfiguration rpc, RoutingProfilesCollection profiles) throws IOException {
		mRoutePrefs = rpc.GetProfilesTypes();
		mUseCounter = 0;
		mUseTrafficInfo = /*mHasDynamicWeights &&*/ hasCarPreferences() ? rpc.UseTrafficInformation : false;

		mGraphHopper = initGraphHopper(osmFile, rpc, profiles);

		mProfileConfig = rpc;
	}

	public static ORSGraphHopper initGraphHopper(String osmFile, RouteProfileConfiguration config, RoutingProfilesCollection profiles) throws IOException {
		CmdArgs args = createGHSettings(osmFile, config);

		RoutingProfile refProfile = null;

		try
		{
			refProfile = profiles.getRouteProfile(RoutingProfileType.DRIVING_CAR);
		}
		catch(Exception ex)
		{}

		long startTime = System.currentTimeMillis();
		
		ORSGraphHopper gh = (ORSGraphHopper) new ORSGraphHopper(config.BBox, config.UseTrafficInformation, refProfile).init(args);
		gh.setGraphStorageFactory(new ORSGraphStorageFactory(GraphStorageType.getFomString(config.ExtStorages)));

		gh.importOrLoad();
		gh.setWeightingFactory(new ORSWeightingFactory(RealTrafficDataProvider.getInstance()));

		// Make a stamp which help tracking any changes in the size of OSM file.
		File file = new File(osmFile);
		Path pathTimestamp = Paths.get(config.GraphPath, "stamp.txt");
		File file2 = pathTimestamp.toFile();
		if (!file2.exists())
			Files.write(pathTimestamp, Long.toString(file.length()).getBytes());

		LOGGER.info("Profiles '" + config.Profiles  +"' are loaded in " + TimeUtility.getElapsedTime(startTime, true) + ". Graph location: " + gh.getGraphHopperLocation() + ".");

		return gh;
	}

	private static CmdArgs createGHSettings(String sourceFile, RouteProfileConfiguration config)
	{
		CmdArgs args = new CmdArgs();
		args.put("graph.dataaccess", "RAM_STORE");
		args.put("osmreader.osm", sourceFile);
		args.put("graph.location", config.GraphPath);
		args.put("graph.bytesForFlags", config.EncoderFlagsSize);

		if (config.Instructions == false)
			args.put("instructions", false);
		if (config.ElevationCachPath != null)
		{
			args.put("graph.elevation.provider", config.ElevationProvider);
			args.put("graph.elevation.cachedir", config.ElevationCachPath);
		}

		args.put("prepare.chWeighting", (config.CHWeighting != null) ? config.CHWeighting: "no");

		String flagEncoders = "";
		String[] encoderOpts = !Helper.isEmpty(config.EncoderOptions) ? config.EncoderOptions.split(","): null;
		Integer[] profiles = config.GetProfilesTypes();
		for (int i = 0; i < profiles.length; i++)
		{
			if (encoderOpts == null)
				flagEncoders += RoutingProfileType.getEncoderName(profiles[i]);
			else
				flagEncoders += RoutingProfileType.getEncoderName(profiles[i]) + "|"+encoderOpts[i];
			if (i < profiles.length - 1)
				flagEncoders += ",";
		}

		args.put("graph.flagEncoders", flagEncoders);
		
		args.put("osmreader.wayPointMaxDistance",1);
		args.put("index.highResolution", 500);

		return args;
	}

	public HashMap<Integer, Long> getTmcEdges() {
		return mGraphHopper.getTmcGraphEdges();
	}

	public ORSGraphHopper getGraphhopper() {
		return mGraphHopper;
	}

	public BBox getBounds() {
		return mGraphHopper.getGraphHopperStorage().getBounds();
	}

	public Geometry getEdgeGeometry(int edgeId)
	{
		return getEdgeGeometry(edgeId, 3, Integer.MIN_VALUE);
	}

	public Geometry getEdgeGeometry(int edgeId, int mode, int adjnodeid)
	{
		EdgeIteratorState iter = mGraphHopper.getGraphHopperStorage().getEdgeIteratorState(edgeId, adjnodeid);
		PointList points = iter.fetchWayGeometry(mode);
		if (points.size() > 1)
		{
			Coordinate[] coords = new Coordinate[points.size()];

			for (int i = 0; i < points.size(); i++) {
				double x = points.getLon(i);
				double y = points.getLat(i);
				coords[i] = new Coordinate(x, y);
			}

			return new GeometryFactory().createLineString(coords);
		}

		return null;
	}

	public StorableProperties getGraphProperties() {
		StorableProperties props = mGraphHopper.getGraphHopperStorage().getProperties();
		return props;
	}

	public String getGraphLocation() {
		return mGraphHopper == null ? null : mGraphHopper.getGraphHopperStorage().getDirectory().toString();
	}

	public RouteProfileConfiguration getConfiguration() {
		return mProfileConfig;
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

					mGraphHopper = initGraphHopper(ghOld.getOSMFile(), mProfileConfig, RoutingProfileManager.getInstance().getProfiles());

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

			ex.printStackTrace();
			throw new Exception("Unable to build isochrone map. " + ex.toString());
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
		String algorithm = null;
		PMap props = new PMap();

		if (searchParams.hasAvoidAreas()) {
			if (encoderName.isEmpty())
				throw new Exception("vehicle parameter is empty.");

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
				edgeFilter = createWayRestrictionsEdgeFilter(searchParams, flagEncoder, edgeFilter);
			}
		} else if (profileType == RoutingProfileType.WHEELCHAIR) {
			if (searchParams.hasParameters(WheelchairParameters.class)) {
				edgeFilter = createWheelchairRestrictionsEdgeFilter(searchParams,  flagEncoder,
						edgeFilter);
			}
		}

		boolean bSteepness = false;

		if (searchParams.hasAvoidFeatures() && mode == RouteSearchMode.Routing) {
			if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)
					|| profileType == RoutingProfileType.FOOT_WALKING || profileType == RoutingProfileType.FOOT_HIKING
					|| profileType == RoutingProfileType.WHEELCHAIR) { 
				
				if (searchParams.getAvoidFeatureTypes() != AvoidFeatureFlags.Hills)
				{
					EdgeFilter ef = new AvoidFeaturesEdgeFilter(flagEncoder, searchParams.getAvoidFeatureTypes(),
						mGraphHopper.getGraphHopperStorage());
					edgeFilter = createEdgeFilter(ef, edgeFilter);
				}

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

		if (bSteepness)
			algorithm = "dijkstra";

		if (searchParams.getConsiderTraffic()/* && mHasDynamicWeights */) {
			if (RoutingProfileType.isDriving(profileType) && weightingMethod != WeightingMethod.SHORTEST
					&& RealTrafficDataProvider.getInstance().isInitialized()) {
				props.put("TrafficBlockWeighting", true);

				EdgeFilter ef = new BlockedEdgesEdgeFilter(flagEncoder, RealTrafficDataProvider.getInstance()
						.getBlockedEdges(mGraphHopper.getGraphHopperStorage()));
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

	public boolean canProcessRequest(double lat0, double lon0, double lat1, double lon1) {
		if (mProfileConfig.MaximumDistance > 0) {
			double dist = CoordTools.calcDistHaversine(lon0, lat0, lon1, lat1);
			if (dist >= mProfileConfig.MaximumDistance)
				return true;
			else
				return false;
		}

		if (mProfileConfig.MinimumDistance > 0) {
			double dist = CoordTools.calcDistHaversine(lon0, lat0, lon1, lat1);

			if (dist >= mProfileConfig.MinimumDistance)
				return true;
			else
				return false;
		}

		return true;
	}

	public GHResponse getRoute(double lat0, double lon0, double lat1, double lon1, boolean directedSegment, RouteSearchParameters searchParams, boolean simplifyGeometry, PathProcessor pathProcessor)
			throws Exception {

		GHResponse resp = null; 

		waitForUpdateCompletion();

		beginUseGH();

		try {
			int profileType = searchParams.getProfileType();
			int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = createSearchContext(searchParams, RouteSearchMode.Routing);
            
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
					req.setWeighting("shortest");
				else if (weightingMethod == WeightingMethod.RECOMMENDED)
					req.setWeighting("recommended");
			}

			if ((profileType == RoutingProfileType.CYCLING_TOUR || profileType == RoutingProfileType.CYCLING_MOUNTAIN)
					&& weightingMethod == WeightingMethod.FASTEST) {
				req.setWeighting("recommended");
			}

			if ((profileType == RoutingProfileType.CYCLING_TOUR || (profileType == RoutingProfileType.DRIVING_HGV && HeavyVehicleAttributes.Hgv == searchParams
					.getVehicleType())) && weightingMethod == WeightingMethod.RECOMMENDED) {
				req.setWeighting("recommended_pref");
			}

			if (RoutingProfileType.isDriving(profileType) && RealTrafficDataProvider.getInstance().isInitialized())
				req.setEdgeAnnotator(new TrafficEdgeAnnotator(mGraphHopper.getGraphHopperStorage()));

			if (searchCntx.getEdgeFilter() != null) 
				req.setEdgeFilter(searchCntx.getEdgeFilter());

			if (pathProcessor != null)
				req.setPathProcessor(pathProcessor);

			if (directedSegment)
				resp = mGraphHopper.directRoute(req);
			else 
				resp = mGraphHopper.route(req);

			endUseGH();
		} catch (Exception ex) {
			endUseGH();

			throw ex;
		}

		return resp;
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

	private EdgeFilter createWayRestrictionsEdgeFilter(RouteSearchParameters searchParams, FlagEncoder flagEncoder,
			EdgeFilter edgeFilter) throws Exception {

		throw new Exception("not implemented");
		/*
		GraphStorage gs = mGraphHopper.getGraphHopperStorage();

		float[] vehicleAttrs = searchParams.getVehicleParameters().getAttributes();
		int valuesCount = 0;
		ArrayList<Integer> idx = new ArrayList<Integer>();

		for (int i = 0; i < VehicleRestrictionCodes.Count; i++) {
			float value = vehicleAttrs[i];
			if (value > 0) {
				idx.add(i);
				valuesCount++;
			}
		}

		if (valuesCount == 1) {
			int code = -1;
			float restrictionValue = -1;

			for (int i = 0; i < VehicleRestrictionCodes.Count; i++) {
				if (vehicleAttrs[i] != 0) {
					code = i;
					restrictionValue = vehicleAttrs[i];
					break;
				}
			}

			if (code >= 0) {
				EdgeFilter ef = null;

				switch (code) {
				case 0:// VehicleRestrictionCodes.MaxHeight:
					ef = new VehicleMaxHeightEdgeFilter(flagEncoder, restrictionValue, gs);
					break;
				case 1:// VehicleRestrictionCodes.MaxWeight:
					ef = new VehicleMaxWeightEdgeFilter(flagEncoder, restrictionValue, gs);
					break;
				case 2:// VehicleRestrictionCodes.MaxWidth:
					ef = new VehicleMaxWidthEdgeFilter(flagEncoder, restrictionValue, gs);
					break;
				case 3:// VehicleRestrictionCodes.MaxWeight:
					ef = new VehicleMaxLengthEdgeFilter(flagEncoder, restrictionValue, gs);
					break;
				case 4:// VehicleRestrictionCodes.MaxAxleLoad:
					ef = new VehicleMaxAxleLoadEdgeFilter(flagEncoder, restrictionValue, gs);
					break;
				}

				edgeFilter = createEdgeFilter(ef, edgeFilter);
			}
		} else {
			EdgeFilter ef = new WayMultipleRestrictionsEdgeFilter(flagEncoder, vehicleAttrs,
					idx.toArray(new Integer[idx.size()]), gs);
			edgeFilter = createEdgeFilter(ef, edgeFilter);
		}

		return edgeFilter;*/
	}

	private static boolean supportWeightingMethod(int profileType) {
		if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType) || profileType == RoutingProfileType.FOOT_WALKING || profileType == RoutingProfileType.WHEELCHAIR)
			return true;
		else
			return false;
	}

	public int hashCode()
	{
		return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
	}
}
