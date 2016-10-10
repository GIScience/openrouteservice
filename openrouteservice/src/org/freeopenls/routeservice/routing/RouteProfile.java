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

package org.freeopenls.routeservice.routing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.freeopenls.routeservice.graphhopper.extensions.HeavyVehicleAttributes;
import org.freeopenls.routeservice.graphhopper.extensions.ORSGraphHopper;
import org.freeopenls.routeservice.graphhopper.extensions.ORSGraphStorageFactory;
import org.freeopenls.routeservice.graphhopper.extensions.ORSWaySurfaceDescriptor;
import org.freeopenls.routeservice.graphhopper.extensions.ORSWeightingFactory;
import org.freeopenls.routeservice.graphhopper.extensions.edgefilters.*;
import org.freeopenls.routeservice.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import org.freeopenls.routeservice.graphhopper.extensions.util.VehicleRestrictionCodes;
import org.freeopenls.routeservice.graphhopper.extensions.weighting.SteepnessDifficultyWeighting;
import org.freeopenls.routeservice.isochrones.IsochroneMap;
import org.freeopenls.routeservice.isochrones.IsochroneMapBuilder;
import org.freeopenls.routeservice.mapmatching.MapMatcher;
import org.freeopenls.routeservice.mapmatching.RouteSegmentInfo;
import org.freeopenls.routeservice.mapmatching.hmm.HiddenMarkovMapMatcher;
import org.freeopenls.routeservice.routing.configuration.RouteProfileConfiguration;
import org.freeopenls.routeservice.traffic.RealTrafficDataProvider;
import org.freeopenls.routeservice.traffic.TrafficEdgeAnnotator;
import org.freeopenls.tools.CoordTools;
import org.freeopenls.tools.FileUtility;
import org.freeopenls.tools.MemoryUtility;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EdgeFilterSequence;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class RouteProfile {
	private final Logger mLogger = Logger.getLogger(RouteProfile.class.getName());

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

	public RouteProfile(String osmFile, String configRoot, RouteProfileConfiguration rpc, RouteProfilesCollection profiles) throws IOException {
		mRoutePrefs = rpc.GetPreferences();
		mUseCounter = 0;
		mMaxDistance = rpc.MaximumDistance == null ? 0 : rpc.MaximumDistance;
		mMinDistance = rpc.MinimumDistance == null ? 0 : rpc.MinimumDistance;
		mHasDynamicWeights = rpc.DynamicWeighting == null ? false : rpc.DynamicWeighting;
		mHasSurfaceInfo = rpc.StoreSurfaceInformation == null ? false : rpc.StoreSurfaceInformation;
		mHasHillIndex = rpc.StoreHillIndex == null ? false : rpc.StoreHillIndex;
		mUseTrafficInfo = /*mHasDynamicWeights &&*/ hasCarPreferences() ? rpc.UseTrafficInformation : false;

		mGraphHopper = initGraphHopper(osmFile, configRoot, rpc.ConfigFileName, rpc.GraphLocation,
				mHasDynamicWeights, mHasSurfaceInfo, mHasHillIndex,  mUseTrafficInfo, rpc.BBox, profiles);

		mConfigRootPath = configRoot;
		mProfileConfig = rpc;
	}

	public static ORSGraphHopper initGraphHopper(String osmFile, String configRoot, String configFileName,
			String graphLocation, boolean dynamicWeighting, boolean surfaceInfo, boolean hillIndex, boolean useTmc, Envelope bbox, RouteProfilesCollection profiles) throws IOException {
		String graphConfig = FileUtility.combinePaths(new String[] { configRoot, configFileName });
		CmdArgs args = CmdArgs.readFromConfig(graphConfig, "graphhopper.config");
		args.put("osmreader.osm", osmFile);
		args.put("graph.location", graphLocation);

		RouteProfile refProfile = null;
		
		try
		{
			refProfile = profiles.getRouteProfile(RoutePreferenceType.CAR);
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
			if (RoutePreferenceType.isCar(mRoutePrefs[i]))
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

					MemoryUtility.clearMemAndLogRAM(mLogger);

					// Change the content of the graph folder
					String oldLocation = ghOld.getGraphHopperLocation();
					File dstDir = new File(oldLocation);
					File srcDir = new File(gh.getGraphHopperLocation());
					FileUtils.copyDirectory(srcDir, dstDir, true);
					FileUtils.deleteDirectory(srcDir);

					mGraphHopper = initGraphHopper(ghOld.getOSMFile(), mConfigRootPath, mProfileConfig.ConfigFileName,
							mProfileConfig.GraphLocation, mProfileConfig.DynamicWeighting, mProfileConfig.StoreSurfaceInformation, mProfileConfig.StoreHillIndex,
							mProfileConfig.UseTrafficInformation, mProfileConfig.BBox, RouteProfileManager.getInstance().getProfiles());

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

	public IsochroneMap buildIsochroneMap(double lat, double lon, double maxCost, String prefType, String method,
			double interval, double gridSize) throws Exception {
		IsochroneMap result = null;

		waitForUpdateCompletion();

		beginUseGH();

		try {
			String encoderName = getEncoderName(RoutePreferenceType.getFromString(prefType));
			FlagEncoder encoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);
			EdgeFilter edgeFilter = null;
			
			/*if (mUseTrafficInfo && RealTrafficDataProvider.getInstance().isInitialized())
				edgeFilter = new BlockedEdgesEdgeFilter(encoder, RealTrafficDataProvider.getInstance()
						.getBlockedEdges(mGraphHopper.getGraph()));
			else
			*/
				edgeFilter = new DefaultEdgeFilter(encoder);
			
			/*	
					req.setWeighting("TrafficBlockWeighting-" + routePref);
	*/
		
			IsochroneMapBuilder isochroneMapBuilder = new IsochroneMapBuilder(mGraphHopper, edgeFilter);

			result = isochroneMapBuilder.buildMap(lat, lon, maxCost, encoderName, method, interval, gridSize);

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

	public GHResponse getRoute(double lat0, double lon0, double lat1, double lon1, RoutePlan routePlan, PMap props)
			throws Exception {

		GHResponse resp = null;

		waitForUpdateCompletion();

		beginUseGH();

		try {
			int routePref = routePlan.getRoutePreference();
			String vehicle = getEncoderName(routePref);
			EdgeFilter edgeFilter = null;
			FlagEncoder flagEncoder = mGraphHopper.getEncodingManager().getEncoder(vehicle);

			if (routePlan.hasAvoidAreas()) {
				if (vehicle.isEmpty())
					throw new Exception("vehicle parameter is empty.");

				if (!mGraphHopper.getEncodingManager().supports(vehicle)) {
					throw new IllegalArgumentException("Vehicle " + vehicle + " unsupported. " + "Supported are: "
							+ mGraphHopper.getEncodingManager());
				}

				edgeFilter = new AvoidAreasEdgeFilter(flagEncoder, routePlan.getAvoidAreas());
			}

			if (RoutePreferenceType.isCar(routePref)) {
				if (RoutePreferenceType.isHeavyVehicle(routePref)) {
					edgeFilter = createHeavyVehicleEdgeFilter(routePlan, mGraphHopper, flagEncoder, edgeFilter);
					/*
					 * double maxSpeed = 80; if (routePlan.getMaxSpeed() ==
					 * -1) routePlan.setMaxSpeed(maxSpeed);
					 */
				} else if (routePlan.hasVehicleAttributes()) {
					edgeFilter = createWayRestrictionsEdgeFilter(routePlan, mGraphHopper, flagEncoder, edgeFilter);
				}
			} else if (routePref == RoutePreferenceType.WHEELCHAIR) {
				if (routePlan.hasWheelchairAttributes()) {
					edgeFilter = createWheelchairRestrictionsEdgeFilter(routePlan, mGraphHopper, flagEncoder,
							edgeFilter);
				}
			}

			int weightingMethod = routePlan.getWeightingMethod();

			GHRequest req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1));
			req.setVehicle(vehicle);
			req.setAlgorithm("dijkstrabi");
			req.setMaxSpeed(routePlan.getMaxSpeed());

			if (supportWeightingMethod(routePref)) {
				if (weightingMethod == WeightingMethod.FASTEST)
					req.setWeighting("fastest");
				else if (weightingMethod == WeightingMethod.SHORTEST)
					req.setWeighting("shortest");
				else if (weightingMethod == WeightingMethod.RECOMMENDED)
					req.setWeighting("recommended");
			}

			if ((routePref == RoutePreferenceType.BICYCLE_TOUR || routePref == RoutePreferenceType.BICYCLE_MTB)
					&& weightingMethod == WeightingMethod.FASTEST) {
				req.setWeighting("recommended");
			}

			if ((routePref == RoutePreferenceType.BICYCLE_TOUR || (routePref == RoutePreferenceType.HEAVY_VEHICLE && HeavyVehicleAttributes.Hgv == routePlan
					.getVehicleType())) && weightingMethod == WeightingMethod.RECOMMENDED) {
				req.setWeighting("recommended_pref");
			}
			
			boolean bAvoidHills = false;

			if (routePlan.hasAvoidFeatures()) {
				if (RoutePreferenceType.isCar(routePref) || RoutePreferenceType.isBicycle(routePref)
						|| routePref == RoutePreferenceType.PEDESTRIAN
						|| routePref == RoutePreferenceType.WHEELCHAIR) {
					EdgeFilter ef = new AvoidFeaturesEdgeFilter(flagEncoder, routePlan.getAvoidFeatureTypes(),
							mGraphHopper.getGraphHopperStorage());
					edgeFilter = createEdgeFilter(ef, edgeFilter);
					
					if ((routePlan.getAvoidFeatureTypes() & AvoidFeatureFlags.Hills) == AvoidFeatureFlags.Hills)
					{
						req.getHints().put("AvoidHills", true);
						req.getHints().put("SteepnessMaximum", routePlan.getSteepnessMaxValue());
						bAvoidHills = true;
					}
				}
			}
			
			if (!bAvoidHills && routePref == RoutePreferenceType.BICYCLE_ELECTRO)
			{
				req.getHints().put("AvoidHills", true);
				req.getHints().put("SteepnessMaximum", routePlan.getSteepnessMaxValue());
				bAvoidHills = true;
			}
			
			if (!((routePlan.getAvoidFeatureTypes() & AvoidFeatureFlags.Hills) == AvoidFeatureFlags.Hills))
			{
				if (routePlan.getSteepnessDifficultyLevel() >= 0 || routePlan.getSteepnessMaxValue() >= 0)
				{
					req.getHints().put("SteepnessDifficulty", true);
					req.getHints().put("SteepnessDifficultyLevel", routePlan.getSteepnessDifficultyLevel());
					req.getHints().put("SteepnessMaximum", routePlan.getSteepnessMaxValue());
					req.setAlgorithm("dijkstra");
				}
			}
			
			if (routePlan.getUseRealTimeTraffic()/* && mHasDynamicWeights */) {
				if (RoutePreferenceType.isCar(routePref) && weightingMethod != WeightingMethod.SHORTEST
						&& RealTrafficDataProvider.getInstance().isInitialized()) {
					req.getHints().put("TrafficBlockWeighting", true);

					EdgeFilter ef = new BlockedEdgesEdgeFilter(flagEncoder, RealTrafficDataProvider.getInstance()
							.getBlockedEdges(mGraphHopper.getGraphHopperStorage()));
					edgeFilter = createEdgeFilter(ef, edgeFilter);
				}
			}

			if (RoutePreferenceType.isCar(routePref) && RealTrafficDataProvider.getInstance().isInitialized())
				req.setEdgeAnnotator(new TrafficEdgeAnnotator(mGraphHopper.getGraphHopperStorage()));

			if (edgeFilter != null) {
				req.setEdgeFilter(edgeFilter);
			}

			if (routePlan.getSurfaceInformation()) {
				if (mHasSurfaceInfo)
					req.setWaySurfaceDescriptor(new ORSWaySurfaceDescriptor(mGraphHopper.getWaySurfaceStorage()));
				else
					routePlan.setSurfaceInformation(false);
			}

			if (props.has("direct_segment"))
			{
				req.getHints().merge(props);
				resp = mGraphHopper.directRoute(req);
			}
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

	private EdgeFilter createWheelchairRestrictionsEdgeFilter(RoutePlan routePlan, GraphHopper gh,
			FlagEncoder flagEncoder, EdgeFilter edgeFilter) {
		EdgeFilter ef = null;
		double[] wheelchairAttributes = routePlan.getWheelchairAttributes();
		GraphStorage gs = gh.getGraphHopperStorage();
		ef = new WheelchairEdgeFilter(wheelchairAttributes, (WheelchairFlagEncoder) flagEncoder, gs);
		edgeFilter = createEdgeFilter(ef, edgeFilter);
		
		return edgeFilter;
	}

	private EdgeFilter createHeavyVehicleEdgeFilter(RoutePlan routePlan, GraphHopper gh, FlagEncoder flagEncoder,
			EdgeFilter edgeFilter) {
		GraphStorage gs = gh.getGraphHopperStorage();

		int vehicleType = routePlan.getVehicleType();

		float[] vehicleAttrs = routePlan.getVehicleAttributes();
		boolean hasHazmat = routePlan.hasLoadCharacteristic("hazmat");
		
		EdgeFilter ef = null;
		if (vehicleAttrs != null) {
			ArrayList<Integer> idx = new ArrayList<Integer>();

			for (int i = 0; i < VehicleRestrictionCodes.Count; i++) {
				float value = vehicleAttrs[i];
				if (value > 0) {
					idx.add(i);
				}
			}

			ef = new HeavyVehicleEdgeFilter(flagEncoder, vehicleType, hasHazmat, vehicleAttrs,
					idx.toArray(new Integer[idx.size()]), gs) {
			};
			
		} else {
			ef = new HeavyVehicleEdgeFilter(flagEncoder, vehicleType, hasHazmat, gs) {	};
		}
		
		edgeFilter = createEdgeFilter(ef, edgeFilter);

		return edgeFilter;
	}

	private EdgeFilter createWayRestrictionsEdgeFilter(RoutePlan routePlan, GraphHopper gh, FlagEncoder flagEncoder,
			EdgeFilter edgeFilter) {
		GraphStorage gs = gh.getGraphHopperStorage();

		float[] vehicleAttrs = routePlan.getVehicleAttributes();
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

		return edgeFilter;
	}

	private static boolean supportWeightingMethod(int routePref) {
		if (RoutePreferenceType.isCar(routePref) || RoutePreferenceType.isBicycle(routePref) || routePref == RoutePreferenceType.PEDESTRIAN || routePref == RoutePreferenceType.WHEELCHAIR)
			return true;
		else
			return false;
	}

	public static String getEncoderName(int routePref) {
		if (routePref == RoutePreferenceType.CAR)
			return "CAR";
		else if (routePref == RoutePreferenceType.CAR_TMC)
			return "CARTMC";
		else if (routePref == RoutePreferenceType.PEDESTRIAN)
			return "FOOT";
		else if (routePref == RoutePreferenceType.BICYCLE)
			return "BIKE";
		else if (routePref == RoutePreferenceType.BICYCLE_MTB)
			return "MTB";
		else if (routePref == RoutePreferenceType.BICYCLE_RACER)
			return "RACINGBIKE";
		else if (routePref == RoutePreferenceType.BICYCLE_TOUR) // custom
			return "CYCLETOURBIKE";
		else if (routePref == RoutePreferenceType.BICYCLE_SAFETY) // custom
			return "SAFETYBIKE";
		else if (routePref == RoutePreferenceType.BICYCLE_ELECTRO) // custom
			return "BIKE";
		else if (routePref == RoutePreferenceType.MOTORBIKE) // custom
			return "MOTORBIKE";
		else if (routePref == RoutePreferenceType.WHEELCHAIR) // custom
			return "WHEELCHAIR";
		else if (routePref == RoutePreferenceType.ELECTRO_CAR) // custom
			return "EVEHICLE";
		else if (routePref == RoutePreferenceType.HEAVY_VEHICLE) // custom
			return "HEAVYVEHICLE";
		else if (routePref == RoutePreferenceType.TRUCKTRAILER) // custom
			return "HEAVYVEHICLE";
		else if (routePref == RoutePreferenceType.NONMOTORIZED) // custom
			return "NONMOTORIZED";
		else if (routePref == RoutePreferenceType.X_4_WD) // custom
			return "X_4_WD";

		return "UNKNOWN";
	}
	
	public int hashCode()
	{
		return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
	}
}
