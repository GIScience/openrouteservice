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
import heigit.ors.routing.util.AvoidFeatureFlags;
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
import heigit.ors.util.FileUtility;
import heigit.ors.util.RuntimeUtility;

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
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class RoutingProfile {
	private final Logger mLogger = Logger.getLogger(RoutingProfile.class.getName());

	private ORSGraphHopper mGraphHopper;
	private double mMaxDistance;
	private double mMinDistance;
	private boolean mHasDynamicWeights;
	private boolean mHasSurfaceInfo;
	private boolean mHasHillIndex;
	private boolean mUseTrafficInfo;
	private Integer[] mRoutePrefs;
	private Integer mUseCounter;
	private boolean mUpdateRun;
	private MapMatcher mMapMatcher;

	private RouteProfileConfiguration mProfileConfig;
	private String mConfigRootPath;

	public RoutingProfile(String osmFile, String configRoot, RouteProfileConfiguration rpc, RoutingProfilesCollection profiles) throws IOException {
		mRoutePrefs = rpc.GetProfiles();
		mUseCounter = 0;
		mMaxDistance = rpc.MaximumDistance == null ? 0 : rpc.MaximumDistance;
		mMinDistance = rpc.MinimumDistance == null ? 0 : rpc.MinimumDistance;
		mHasDynamicWeights = rpc.DynamicWeighting == null ? false : rpc.DynamicWeighting;
		mHasSurfaceInfo = rpc.SurfaceInformation == null ? false : rpc.SurfaceInformation;
		mHasHillIndex = rpc.HillIndex == null ? false : rpc.HillIndex;
		mUseTrafficInfo = /*mHasDynamicWeights &&*/ hasCarPreferences() ? rpc.UseTrafficInformation : false;

		mGraphHopper = initGraphHopper(osmFile, configRoot, rpc.ConfigPath, rpc.GraphPath,
				mHasDynamicWeights, mHasSurfaceInfo, mHasHillIndex,  mUseTrafficInfo, rpc.BBox, profiles);

		mConfigRootPath = configRoot;
		mProfileConfig = rpc;
	}

	public static ORSGraphHopper initGraphHopper(String osmFile, String configRoot, String configFileName,
			String graphLocation, boolean dynamicWeighting, boolean surfaceInfo, boolean hillIndex, boolean useTmc, Envelope bbox, RoutingProfilesCollection profiles) throws IOException {
		String graphConfig = FileUtility.combinePaths(new String[] { configRoot, configFileName });
		CmdArgs args = CmdArgs.readFromConfig(graphConfig, "graphhopper.config");
		args.put("osmreader.osm", osmFile);
		args.put("graph.location", graphLocation);

		RoutingProfile refProfile = null;

		try
		{
			refProfile = profiles.getRouteProfile(RoutingProfileType.DRIVING_CAR);
		}
		catch(Exception ex)
		{}

		ORSGraphHopper gh = (ORSGraphHopper) new ORSGraphHopper(bbox, surfaceInfo, hillIndex, useTmc, refProfile).init(args);
		gh.setGraphStorageFactory(new ORSGraphStorageFactory(dynamicWeighting, surfaceInfo, hillIndex));

		gh.importOrLoad();
		gh.setWeightingFactory(new ORSWeightingFactory(RealTrafficDataProvider.getInstance()));

		// Make a stamp which help tracking any changes in the size of OSM file.
		File file = new File(osmFile);
		Path pathTimestamp = Paths.get(graphLocation, "stamp.txt");
		File file2 = pathTimestamp.toFile();
		if (!file2.exists())
			Files.write(pathTimestamp, Long.toString(file.length()).getBytes());

		return gh;
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

	public String getConfigRootPath() {
		return mConfigRootPath;
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

	public boolean hasDynamicWeights() {
		return mHasDynamicWeights;
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

					RuntimeUtility.clearMemAndLogRAM(mLogger);

					// Change the content of the graph folder
					String oldLocation = ghOld.getGraphHopperLocation();
					File dstDir = new File(oldLocation);
					File srcDir = new File(gh.getGraphHopperLocation());
					FileUtils.copyDirectory(srcDir, dstDir, true);
					FileUtils.deleteDirectory(srcDir);

					mGraphHopper = initGraphHopper(ghOld.getOSMFile(), mConfigRootPath, mProfileConfig.ConfigPath,
							mProfileConfig.GraphPath, mProfileConfig.DynamicWeighting, mProfileConfig.SurfaceInformation, mProfileConfig.HillIndex,
							mProfileConfig.UseTrafficInformation, mProfileConfig.BBox, RoutingProfileManager.getInstance().getProfiles());

					break;
				}

				Thread.sleep(2000);
			}
		} catch (Exception ex) {
			mLogger.error(ex.getMessage());
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
			String encoderName = RoutingProfileType.getEncoderName(parameters.getRouteParameters().getProfileType());
			FlagEncoder encoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);
			EdgeFilter edgeFilter = new DefaultEdgeFilter(encoder);

			IsochroneMapBuilderFactory isochroneMapBuilderFactory = new IsochroneMapBuilderFactory(mGraphHopper, edgeFilter, encoderName);
			result = isochroneMapBuilderFactory.buildMap(parameters);

			endUseGH();
		} catch (Exception ex) {
			endUseGH();

			ex.printStackTrace();
			throw new Exception("Unable to build isochrone map. " + ex.toString());
		}

		return result;
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
		if (mMaxDistance > 0) {
			double dist = CoordTools.calcDistHaversine(lon0, lat0, lon1, lat1);
			if (dist >= mMaxDistance)
				return true;
			else
				return false;
		}

		if (mMinDistance > 0) {
			double dist = CoordTools.calcDistHaversine(lon0, lat0, lon1, lat1);

			if (dist >= mMinDistance)
				return true;
			else
				return false;
		}

		return true;
	}

	public GHResponse getRoute(double lat0, double lon0, double lat1, double lon1, RouteSearchParameters searchParams, boolean directedSegment, PathProcessor pathProcessor)
			throws Exception {

		GHResponse resp = null; 

		waitForUpdateCompletion();

		beginUseGH();

		try {
			int profileType = searchParams.getProfileType();
			String encoderName = RoutingProfileType.getEncoderName(profileType);
			EdgeFilter edgeFilter = null;
			FlagEncoder flagEncoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);

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
					/*
					 * double maxSpeed = 80; if (routePlan.getMaxSpeed() ==
					 * -1) routePlan.setMaxSpeed(maxSpeed);
					 */
				} else if (searchParams.hasParameters(VehicleParameters.class)) {
					edgeFilter = createWayRestrictionsEdgeFilter(searchParams, flagEncoder, edgeFilter);
				}
			} else if (profileType == RoutingProfileType.WHEELCHAIR) {
				if (searchParams.hasParameters(WheelchairParameters.class)) {
					edgeFilter = createWheelchairRestrictionsEdgeFilter(searchParams,  flagEncoder,
							edgeFilter);
				}
			}

			int weightingMethod = searchParams.getWeightingMethod();

			GHRequest req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1));
			req.setVehicle(encoderName);
			req.setAlgorithm("dijkstrabi");
			req.setMaxSpeed(searchParams.getMaximumSpeed());

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

			boolean bSteepness = false;

			if (searchParams.hasAvoidFeatures()) {
				if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)
						|| profileType == RoutingProfileType.FOOT_WALKING || profileType == RoutingProfileType.FOOT_HIKING
						|| profileType == RoutingProfileType.WHEELCHAIR) { 
					EdgeFilter ef = new AvoidFeaturesEdgeFilter(flagEncoder, searchParams.getAvoidFeatureTypes(),
							mGraphHopper.getGraphHopperStorage());
					edgeFilter = createEdgeFilter(ef, edgeFilter);

					if ((searchParams.getAvoidFeatureTypes() & AvoidFeatureFlags.Hills) == AvoidFeatureFlags.Hills)
					{
						req.getHints().put("AvoidHills", true);

						if (searchParams.hasParameters(CyclingParameters.class))
						{
							CyclingParameters cyclingParams = (CyclingParameters)searchParams.getProfileParameters();

							req.getHints().put("SteepnessMaximum", cyclingParams.getMaximumGradient());
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

					if (cyclingParams.getDifficultyLevel() >= 0 || cyclingParams.getMaximumGradient() >= 0)
					{
						req.getHints().put("SteepnessDifficulty", true);
						req.getHints().put("SteepnessDifficultyLevel", cyclingParams.getDifficultyLevel());
						req.getHints().put("SteepnessMaximum", cyclingParams.getMaximumGradient());
						bSteepness = true;
					}
				}
			}

			if (bSteepness)
				req.setAlgorithm("dijkstra");

			if (searchParams.getConsiderTraffic()/* && mHasDynamicWeights */) {
				if (RoutingProfileType.isDriving(profileType) && weightingMethod != WeightingMethod.SHORTEST
						&& RealTrafficDataProvider.getInstance().isInitialized()) {
					req.getHints().put("TrafficBlockWeighting", true);

					EdgeFilter ef = new BlockedEdgesEdgeFilter(flagEncoder, RealTrafficDataProvider.getInstance()
							.getBlockedEdges(mGraphHopper.getGraphHopperStorage()));
					edgeFilter = createEdgeFilter(ef, edgeFilter);
				}
			}

			if (RoutingProfileType.isDriving(profileType) && RealTrafficDataProvider.getInstance().isInitialized())
				req.setEdgeAnnotator(new TrafficEdgeAnnotator(mGraphHopper.getGraphHopperStorage()));

			if (edgeFilter != null) {
				req.setEdgeFilter(edgeFilter);
			}

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
