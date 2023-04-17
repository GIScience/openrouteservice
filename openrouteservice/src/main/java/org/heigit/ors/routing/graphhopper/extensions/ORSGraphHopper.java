/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.*;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.gtfs.GraphHopperGtfs;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.Router;
import com.graphhopper.routing.RouterConfig;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.ch.CHPreparationHandler;
import com.graphhopper.routing.lm.LMPreparationHandler;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHConfig;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.*;
import com.graphhopper.util.details.PathDetailsBuilderFactory;
import com.graphhopper.util.exceptions.ConnectionNotFoundException;
import com.graphhopper.util.shapes.GHPoint;
import org.geotools.feature.SchemaException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.fastisochrones.Contour;
import org.heigit.ors.fastisochrones.Eccentricity;
import org.heigit.ors.fastisochrones.partitioning.FastIsochroneFactory;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.mapmatching.MapMatcher;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.mapmatching.hmm.HiddenMarkovMapMatcher;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.core.*;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidFeaturesEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.HeavyVehicleEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.TrafficEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.HereTrafficGraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.routing.graphhopper.extensions.weighting.HgvAccessWeighting;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.CoordTools;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;


public class ORSGraphHopper extends GraphHopperGtfs {
	private static final Logger LOGGER = LoggerFactory.getLogger(ORSGraphHopper.class);
	public static final String KEY_DEPARTURE = "departure";
	public static final String KEY_ARRIVAL = "arrival";

	private GraphProcessContext processContext;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds; // one osm id can correspond to multiple edges
	private HashMap<Integer, Long> tmcEdges;
	private Eccentricity eccentricity;
    private TrafficEdgeFilter trafficEdgeFilter;

	private int minNetworkSize = 200;
	private int minOneWayNetworkSize = 0;

	private final CorePreparationHandler corePreparationHandler =  new CorePreparationHandler();
	private final CoreLMPreparationHandler coreLMPreparationHandler = new CoreLMPreparationHandler();
	private final FastIsochroneFactory fastIsochroneFactory = new FastIsochroneFactory();

    private MapMatcher mMapMatcher;

	public GraphHopperConfig getConfig() {
		return config;
	}

	private GraphHopperConfig config;

	public ORSGraphHopper(GraphProcessContext procCntx) {
		processContext = procCntx;
		processContext.init(this);
    }


    public ORSGraphHopper() {
        // used to initialize tests more easily without the need to create GraphProcessContext etc. when they're anyway not used in the tested functions.
    }

	@Override
	public GraphHopper init(GraphHopperConfig ghConfig) {
		GraphHopper ret = super.init(ghConfig);

		if (ghConfig instanceof ORSGraphHopperConfig) {
			ORSGraphHopperConfig orsConfig = (ORSGraphHopperConfig) ghConfig;
			corePreparationHandler.init(orsConfig);
			coreLMPreparationHandler.init(orsConfig);
		}

		fastIsochroneFactory.init(ghConfig);

		minNetworkSize = ghConfig.getInt("prepare.min_network_size", minNetworkSize);
		minOneWayNetworkSize = ghConfig.getInt("prepare.min_one_way_network_size", minOneWayNetworkSize);
		config = ghConfig;
		return ret;
	}

	@Override
	protected void cleanUp() {
		if (LOGGER.isInfoEnabled())
			LOGGER.info(String.format("call cleanUp for '%s' ", getGraphHopperLocation()));
		GraphHopperStorage ghs = getGraphHopperStorage();
		if (ghs != null) {
			if (LOGGER.isInfoEnabled())
				LOGGER.info(String.format("graph %s, details:%s", ghs, ghs.toDetailsString()));
			int prevNodeCount = ghs.getNodes();
			int ex = ghs.getAllEdges().length();
			List<FlagEncoder> list = getEncodingManager().fetchEdgeEncoders();
			if (LOGGER.isInfoEnabled())
				LOGGER.info(String.format("will create PrepareRoutingSubnetworks with:%n\tNodeCountBefore: '%d'%n\tgetAllEdges().getMaxId(): '%d'%n\tList<FlagEncoder>: '%s'%n\tminNetworkSize: '%d'%n\tminOneWayNetworkSize: '%d'", prevNodeCount, ex, list, minNetworkSize, minOneWayNetworkSize)
			);
			ghs.getProperties().put("elevation", hasElevation());
		} else {
			LOGGER.info("graph GraphHopperStorage is null?!");
		}
		super.cleanUp();
	}

	@Override
	protected  OSMReader createOSMReader() {
		return new ORSOSMReader(getGraphHopperStorage(), processContext);
	}

	@Override
	public GraphHopper importOrLoad() {
		GraphHopper gh = super.importOrLoad();

        if ((tmcEdges != null) && (osmId2EdgeIds != null)) {
            java.nio.file.Path path = Paths.get(gh.getGraphHopperLocation(), "edges_ors_traffic");

            if ((tmcEdges.size() == 0) || (osmId2EdgeIds.size() == 0)) {
                // try to load TMC edges from file.

                File file = path.toFile();
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(path.toString());
                         ObjectInputStream ois = new ObjectInputStream(fis)) {
                        tmcEdges = (HashMap<Integer, Long>) ois.readObject();
                        osmId2EdgeIds = (HashMap<Long, ArrayList<Integer>>) ois.readObject();
                        LOGGER.info("Serialized HashMap data is saved in trafficEdges");
                    } catch (IOException ioe) {
                        LOGGER.error(Arrays.toString(ioe.getStackTrace()));
                    } catch (ClassNotFoundException c) {
                        LOGGER.error("Class not found");
                        LOGGER.error(Arrays.toString(c.getStackTrace()));
                    }
                }
            } else {
                // save TMC edges if needed.
                try (FileOutputStream fos = new FileOutputStream(path.toString());
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(tmcEdges);
                    oos.writeObject(osmId2EdgeIds);
                    LOGGER.info("Serialized HashMap data is saved in trafficEdges");
                } catch (IOException ioe) {
                    LOGGER.error(Arrays.toString(ioe.getStackTrace()));
                }
            }
        }

        return gh;
    }

	@Override
	protected Router doCreateRouter(GraphHopperStorage ghStorage, LocationIndex locationIndex, Map<String, Profile> profilesByName,
									PathDetailsBuilderFactory pathBuilderFactory, TranslationMap trMap, RouterConfig routerConfig,
									WeightingFactory weightingFactory, Map<String, RoutingCHGraph> chGraphs, Map<String, LandmarkStorage> landmarks) {
		ORSRouter r = new ORSRouter(ghStorage, locationIndex, profilesByName, pathBuilderFactory, trMap, routerConfig, weightingFactory, chGraphs, landmarks);
		r.setEdgeFilterFactory(new ORSEdgeFilterFactory());
		r.setPathProcessorFactory(pathProcessorFactory);

		if (!(ghStorage instanceof ORSGraphHopperStorage))
			throw new IllegalStateException("Expected an instance of ORSGraphHopperStorage");

		Map<String, RoutingCHGraph> coreGraphs = new LinkedHashMap<>();
		for (com.graphhopper.config.CHProfile chProfile : corePreparationHandler.getCHProfiles()) {
			String chGraphName = corePreparationHandler.getPreparation(chProfile.getProfile()).getCHConfig().getName();
			coreGraphs.put(chProfile.getProfile(), ((ORSGraphHopperStorage) ghStorage).getCoreGraph(chGraphName));
		}
		r.setCoreGraphs(coreGraphs);

		Map<String, PrepareCoreLandmarks> coreLandmarks = new LinkedHashMap<>();
		for (PrepareLandmarks preparation : coreLMPreparationHandler.getPreparations()) {
			coreLandmarks.put(preparation.getLMConfig().getName(), (PrepareCoreLandmarks) preparation);
		}
		r.setCoreLandmarks(coreLandmarks);

		return r;
	}

	@Override
	protected WeightingFactory createWeightingFactory() {
		return new ORSWeightingFactory(getGraphHopperStorage(), getEncodingManager());
	}

    public RouteSegmentInfo getRouteSegment(double[] latitudes, double[] longitudes, String vehicle) {
        RouteSegmentInfo result = null;

        GHRequest req = new GHRequest();
        for (int i = 0; i < latitudes.length; i++)
            req.addPoint(new GHPoint(latitudes[i], longitudes[i]));

		req.setAlgorithm("dijkstrabi");
		req.getHints().putObject("weighting", "fastest");

        GHResponse resp = new GHResponse();

		// TODO Postponed till MapMatcher implementation: need to create a router here? Can we maybe remove the whole class ORSGraphHopper?
		// List<Path> paths = this.calcPaths(req, resp);
		List<Path> paths = new ArrayList<>(); // stub to make compile temporarily

        if (!resp.hasErrors()) {

            List<EdgeIteratorState> fullEdges = new ArrayList<>();
            PointList fullPoints = PointList.EMPTY;
            long time = 0;
            double distance = 0;
            for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
                Path path = paths.get(pathIndex);
                time += path.getTime();

                for (EdgeIteratorState edge : path.calcEdges()) {
                    fullEdges.add(edge);
                }

                PointList tmpPoints = path.calcPoints();

                if (fullPoints.isEmpty())
                    fullPoints = new PointList(tmpPoints.size(), tmpPoints.is3D());

                fullPoints.add(tmpPoints);

                distance += path.getDistance();
            }

            if (fullPoints.size() > 1) {
                Coordinate[] coords = new Coordinate[fullPoints.size()];

                for (int i = 0; i < fullPoints.size(); i++) {
                    double x = fullPoints.getLon(i);
                    double y = fullPoints.getLat(i);
                    coords[i] = new Coordinate(x, y);
                }

                result = new RouteSegmentInfo(fullEdges, distance, time, new GeometryFactory().createLineString(coords));
            }
        }

        return result;
    }

	/**
	 * Check whether the route processing has to start. If avoid all borders is set and the routing points are in different countries,
	 * there is no need to even start routing.
	 * @param request To get the avoid borders setting
	 * @param queryResult To get the edges of the queries and check which country they're in
	 */
	private void checkAvoidBorders(GHRequest request, List<Snap> queryResult) {
		/* Avoid borders */
		PMap params = request.getAdditionalHints();
		if (params == null) {
			params = new PMap();
		}
		boolean isRouteable = true;

		if (params.has("avoid_borders")) {
				RouteSearchParameters routeSearchParameters = params.getObject("avoid_borders", new RouteSearchParameters());
				//Avoiding All borders
				if(routeSearchParameters.hasAvoidBorders() && routeSearchParameters.getAvoidBorders() == BordersExtractor.Avoid.ALL) {
					List<Integer> edgeIds =  new ArrayList<>();
					for (int placeIndex = 0; placeIndex < queryResult.size(); placeIndex++) {
						edgeIds.add(queryResult.get(placeIndex).getClosestEdge().getEdge());
					}
					BordersExtractor bordersExtractor = new BordersExtractor(GraphStorageUtils.getGraphExtension(getGraphHopperStorage(), BordersGraphStorage.class), null);
					isRouteable = bordersExtractor.isSameCountry(edgeIds);
				}
				//TODO Refactoring : Avoiding CONTROLLED borders
				//Currently this is extremely messy, as for some reason the READER stores data in addition to the BordersStorage.
				//At the same time, it is not possible to get isOpen from the Reader via ids, because it only takes Strings. But there are no Strings in the Storage.
				//So no controlled borders for now until this whole thing is refactored and the Reader is an actual reader and not a storage.

//				if(routeSearchParameters.hasAvoidBorders() && routeSearchParameters.getAvoidBorders() == BordersExtractor.Avoid.CONTROLLED) {
//					GraphStorageBuilder countryBordersReader;
//					if(processContext.getStorageBuilders().size() > 0) {
//						countryBordersReader = processContext.getStorageBuilders().get(0);
//						int i = 1;
//						while (i < processContext.getStorageBuilders().size() && !(countryBordersReader instanceof CountryBordersReader)) {
//							countryBordersReader = processContext.getStorageBuilders().get(i);
//							i++;
//						}
//
//						List<Integer> edgeIds = new ArrayList<>();
//						for (int placeIndex = 0; placeIndex < queryResult.size(); placeIndex++) {
//							edgeIds.add(queryResult.get(placeIndex).getClosestEdge().getEdge());
//						}
//						BordersExtractor bordersExtractor = new BordersExtractor(GraphStorageUtils.getGraphExtension(getGraphHopperStorage(), BordersGraphStorage.class), null);
//						if (!bordersExtractor.isSameCountry(edgeIds)) {
//							isRouteable == ((CountryBordersReader) countryBordersReader).isOpen(id0, id1)
//							...
//						}
//					}
//				}
			}
		if(!isRouteable)
			throw new ConnectionNotFoundException("Route not found due to avoiding borders", Collections.emptyMap());

    }

    public GHResponse constructFreeHandRoute(GHRequest request) {
        LineString directRouteGeometry = constructFreeHandRouteGeometry(request);
        ResponsePath directRoutePathWrapper = constructFreeHandRoutePathWrapper(directRouteGeometry);
        GHResponse directRouteResponse = new GHResponse();
        directRouteResponse.add(directRoutePathWrapper);
        directRouteResponse.getHints().putObject("skipped_segment", true);
        return directRouteResponse;
    }

    private ResponsePath constructFreeHandRoutePathWrapper(LineString lineString) {
		ResponsePath responsePath = new ResponsePath();
        PointList pointList = new PointList();
        PointList startPointList = new PointList();
        PointList endPointList = new PointList();
        PointList wayPointList = new PointList();
        Coordinate startCoordinate = lineString.getCoordinateN(0);
        Coordinate endCoordinate = lineString.getCoordinateN(1);
        double distance = CoordTools.calcDistHaversine(startCoordinate.x, startCoordinate.y, endCoordinate.x, endCoordinate.y);
        pointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        pointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        wayPointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        wayPointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        startPointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        endPointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        Translation translation = new TranslationMap.TranslationHashMap(new Locale(""));
        InstructionList instructions = new InstructionList(translation);
        Instruction startInstruction = new Instruction(Instruction.REACHED_VIA, "free hand route", startPointList);
        Instruction endInstruction = new Instruction(Instruction.FINISH, "end of free hand route", endPointList);
        instructions.add(0, startInstruction);
        instructions.add(1, endInstruction);
        responsePath.setDistance(distance);
        responsePath.setAscend(0.0);
        responsePath.setDescend(0.0);
        responsePath.setTime(0);
        responsePath.setInstructions(instructions);
        responsePath.setWaypoints(wayPointList);
        responsePath.setPoints(pointList);
        responsePath.setRouteWeight(0.0);
        responsePath.setDescription(new ArrayList<>());
        responsePath.setImpossible(false);
        startInstruction.setDistance(distance);
        startInstruction.setTime(0);
        return responsePath;
    }

    private LineString constructFreeHandRouteGeometry(GHRequest request) {
        Coordinate start = new Coordinate();
        Coordinate end = new Coordinate();
        start.x = request.getPoints().get(0).getLat();
        start.y = request.getPoints().get(0).getLon();
        end.x = request.getPoints().get(1).getLat();
        end.y = request.getPoints().get(1).getLon();
        Coordinate[] coords = new Coordinate[]{start, end};
        return new GeometryFactory().createLineString(coords);
    }

    private void matchTraffic() {
        // Do the graph extension post-processing
        // Reserved for processes that need a fully initiated graph e.g. for match making
        if (getGraphHopperStorage() != null && processContext != null && processContext.getStorageBuilders() != null) {
            for (GraphStorageBuilder graphStorageBuilder : processContext.getStorageBuilders()) {
                if (graphStorageBuilder instanceof HereTrafficGraphStorageBuilder) {
                    try {
                        ((HereTrafficGraphStorageBuilder) graphStorageBuilder).postProcess(this);
                    } catch (SchemaException e) {
                        LOGGER.error("Error building the here traffic storage.");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

	private void addTrafficSpeedCalculator(LMPreparationHandler lmPreparationHandler) {
		if (isTrafficEnabled())
			ORSWeightingFactory.addTrafficSpeedCalculator(lmPreparationHandler.getWeightings(), getGraphHopperStorage());
	}

    /**
	 * Does the preparation and creates the location index
	 *
	 * @param closeEarly release resources as early as possible
	 */
	@Override
	protected void postProcessing(boolean closeEarly) {
		super.postProcessing(closeEarly);

		//Create the core
		GraphHopperStorage gs = getGraphHopperStorage();
		if(corePreparationHandler.isEnabled())
			corePreparationHandler.setProcessContext(processContext).createPreparations(gs);
		if (isCorePrepared()) {
			// check loaded profiles
			for (CHProfile profile : corePreparationHandler.getCHProfiles()) {
				if (!getProfileVersion(profile.getProfile()).isEmpty() && !getProfileVersion(profile.getProfile()).equals("" + profilesByName.get(profile.getProfile()).getVersion()))
					throw new IllegalArgumentException("Core preparation of " + profile.getProfile() + " already exists in storage and doesn't match configuration");
			}
		} else {
			prepareCore(closeEarly);
		}

		//Create the landmarks in the core
		if (coreLMPreparationHandler.isEnabled()) {
			initCoreLMPreparationHandler();
			coreLMPreparationHandler.createPreparations(gs, super.getLocationIndex());
			addTrafficSpeedCalculator(coreLMPreparationHandler);
		}
		loadOrPrepareCoreLM();

		if (fastIsochroneFactory.isEnabled()) {
			EdgeFilterSequence partitioningEdgeFilter = new EdgeFilterSequence();
			try {
				partitioningEdgeFilter.add(new AvoidFeaturesEdgeFilter(AvoidFeatureFlags.FERRIES, getGraphHopperStorage()));
			} catch (Exception e) {
				LOGGER.debug(e.getLocalizedMessage());
			}
			fastIsochroneFactory.createPreparation(gs, partitioningEdgeFilter);

			if (!isPartitionPrepared())
				preparePartition();
			else {
				fastIsochroneFactory.setExistingStorages();
				fastIsochroneFactory.getCellStorage().loadExisting();
				fastIsochroneFactory.getIsochroneNodeStorage().loadExisting();
			}
			//No fast isochrones without partition
			if (isPartitionPrepared()) {
				// Initialize edge filter sequence for fast isochrones
				calculateContours();
				List<Profile> profiles = fastIsochroneFactory.getFastIsochroneProfiles();
				for (Profile profile : profiles) {
					Weighting weighting = ((ORSWeightingFactory) createWeightingFactory()).createIsochroneWeighting(profile, new PMap(profile.getName()).putObject("isochroneWeighting", "true"));

					for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
						calculateCellProperties(weighting, partitioningEdgeFilter, encoder, fastIsochroneFactory.getIsochroneNodeStorage(), fastIsochroneFactory.getCellStorage());
					}
				}
			}
		}
	}

	@Override
	protected void postProcessingHook() {
		matchTraffic();

		if (getLMPreparationHandler().isEnabled())
			addTrafficSpeedCalculator(getLMPreparationHandler());
	}

    //TODO Refactoring : This is a duplication with code in RoutingProfile and should probably be moved to a status keeping class.
    private boolean hasCHProfile(String profileName) {
		return contains(getGraphHopperStorage().getCHGraphNames(), profileName);
    }

	private boolean hasCoreProfile(String profileName) {
		if (getGraphHopperStorage() instanceof ORSGraphHopperStorage) {
			List<String> profiles = ((ORSGraphHopperStorage) getGraphHopperStorage()).getCoreGraphNames();
			return contains(profiles, profileName);
		}
		return false;
	}

	private boolean hasLMProfile(String profileName) {
		List<String> profiles = getLMPreparationHandler().getLMConfigs().stream().map((lmConfig) -> lmConfig.getName()).collect(Collectors.toList());
		return contains(profiles, profileName);
	}

	private boolean contains(List<String> profiles, String profileName) {
		for (String profile : profiles) {
			if (profileName.equals(profile))
				return true;
		}
		return false;
	}

	public final boolean isCoreEnabled() {
		return corePreparationHandler.isEnabled();
	}

	public final CorePreparationHandler getCorePreparationHandler() {
		return corePreparationHandler;
	}

	@Override
	protected void initCHPreparationHandler() {
		CHPreparationHandler chPreparationHandler = getCHPreparationHandler();
		if (chPreparationHandler.hasCHConfigs()) {
			return;
		}

		for (CHProfile chProfile : chPreparationHandler.getCHProfiles()) {
			Profile profile = profilesByName.get(chProfile.getProfile());
			Weighting weighting = createWeighting(profile, new PMap());

			if (profile.getVehicle().equals(FlagEncoderNames.HEAVYVEHICLE)) {
				HeavyVehicleAttributesGraphStorage hgvStorage = GraphStorageUtils.getGraphExtension(getGraphHopperStorage(), HeavyVehicleAttributesGraphStorage.class);
				EdgeFilter hgvEdgeFilter = new HeavyVehicleEdgeFilter(HeavyVehicleAttributes.HGV, null, hgvStorage);
				weighting = new HgvAccessWeighting(weighting, hgvEdgeFilter);
			}

			if (profile.isTurnCosts()) {
				chPreparationHandler.addCHConfig(CHConfig.edgeBased(profile.getName(), weighting));
			} else {
				chPreparationHandler.addCHConfig(CHConfig.nodeBased(profile.getName(), weighting));
			}
		}
	}

	protected void loadORS() {
		List<CHConfig> chConfigs;
		if (corePreparationHandler.isEnabled()) {
			initCorePreparationHandler();
			chConfigs = corePreparationHandler.getCHConfigs();
		} else {
			chConfigs = emptyList();
		}

		if (getGraphHopperStorage() instanceof ORSGraphHopperStorage)
			((ORSGraphHopperStorage) getGraphHopperStorage()).addCoreGraphs(chConfigs);
		else
			throw new IllegalStateException("Expected an instance of ORSGraphHopperStorage");
	}

	private void initCorePreparationHandler() {
		if (corePreparationHandler.hasCHConfigs()) {
			return;
		}

		for (com.graphhopper.config.CHProfile chProfile : corePreparationHandler.getCHProfiles()) {
			Profile profile = profilesByName.get(chProfile.getProfile());
			corePreparationHandler.addCHConfig(new CHConfig(profile.getName(), createWeighting(profile, new PMap()), profile.isTurnCosts(), CHConfig.TYPE_CORE));
		}
	}

	private void initCoreLMPreparationHandler() {
		if (coreLMPreparationHandler.hasLMProfiles())
			return;

		CoreLMOptions coreLMOptions = coreLMPreparationHandler.getCoreLMOptions();
		coreLMOptions.createRestrictionFilters(getGraphHopperStorage());

		for (LMProfile lmProfile : coreLMPreparationHandler.getLMProfiles()) {
			if (lmProfile.usesOtherPreparation())
				continue;
			Profile profile = profilesByName.get(lmProfile.getProfile());
			Weighting weighting = createWeighting(profile, new PMap(), true);
			for (LMEdgeFilterSequence edgeFilter : coreLMOptions.getFilters()) {
				CoreLMConfig coreLMConfig = new CoreLMConfig(profile.getName(), weighting);
				coreLMConfig.setEdgeFilter(edgeFilter);
				coreLMPreparationHandler.addLMConfig(coreLMConfig);
			}
		}
	}

	protected void prepareCore(boolean closeEarly) {
		for (CHProfile profile : corePreparationHandler.getCHProfiles()) {
			if (!getProfileVersion(profile.getProfile()).isEmpty()
					&& !getProfileVersion(profile.getProfile()).equals("" + profilesByName.get(profile.getProfile()).getVersion()))
				throw new IllegalArgumentException("Core preparation of " + profile.getProfile() + " already exists in storage and doesn't match configuration");
		}
		if (isCoreEnabled()) {
			ensureWriteAccess();
			GraphHopperStorage ghStorage = getGraphHopperStorage();
			ghStorage.freeze();
			corePreparationHandler.prepare(ghStorage.getProperties(), closeEarly);
			ghStorage.getProperties().put(ORSParameters.Core.PREPARE + "done", true);
			for (CHProfile profile : corePreparationHandler.getCHProfiles()) {
				// potentially overwrite existing keys from CH/LM
				setProfileVersion(profile.getProfile(), profilesByName.get(profile.getProfile()).getVersion());
			}
		}
	}

    private boolean isCorePrepared() {
        return "true".equals(getGraphHopperStorage().getProperties().get(ORSParameters.Core.PREPARE + "done"))
                // remove old property in >0.9
                || "true".equals(getGraphHopperStorage().getProperties().get("prepare.done"));
    }

	public final boolean isCoreLMEnabled() {
		return coreLMPreparationHandler.isEnabled();
	}

	/**
	 * For landmarks it is required to always call this method: either it creates the landmark data or it loads it.
	 */
	protected void loadOrPrepareCoreLM() {
		boolean tmpPrepare = coreLMPreparationHandler.isEnabled();
		if (tmpPrepare) {
			ensureWriteAccess();
			getGraphHopperStorage().freeze();
			if (coreLMPreparationHandler.loadOrDoWork(getGraphHopperStorage().getProperties(), false))
				getGraphHopperStorage().getProperties().put(ORSParameters.CoreLandmark.PREPARE + "done", true);
		}
	}

    //TODO Refactoring : This is a duplication with code in RoutingProfile and should probably be moved to a status keeping class.
    public final boolean isCHAvailable(String profileName) {
        return getCHPreparationHandler().isEnabled() && hasCHProfile(profileName);
    }

    public final boolean isLMAvailable(String profileName) {
		return getLMPreparationHandler().isEnabled() && hasLMProfile(profileName);
	}

	public final boolean isCoreAvailable(String profileName) {
		return getCorePreparationHandler().isEnabled() && hasCoreProfile(profileName);
	}

	public final boolean isFastIsochroneAvailable(RouteSearchContext searchContext, TravelRangeType travelRangeType) {
		return eccentricity != null && eccentricity.isAvailable(ORSWeightingFactory.createIsochroneWeighting(searchContext, travelRangeType));
	}

    /**
     * Partitioning
     */
    public final FastIsochroneFactory getFastIsochroneFactory() {
        return fastIsochroneFactory;
    }

    protected void preparePartition() {
        if (fastIsochroneFactory.isEnabled()) {
            ensureWriteAccess();

            getGraphHopperStorage().freeze();
            fastIsochroneFactory.prepare(getGraphHopperStorage().getProperties());
            getGraphHopperStorage().getProperties().put(ORSParameters.FastIsochrone.PREPARE + "done", true);
        }
    }

    private boolean isPartitionPrepared() {
        return "true".equals(getGraphHopperStorage().getProperties().get(ORSParameters.FastIsochrone.PREPARE + "done"));
    }

    private void calculateContours() {
        if (fastIsochroneFactory.getCellStorage().isContourPrepared())
            return;
        Contour contour = new Contour(getGraphHopperStorage(), getGraphHopperStorage().getNodeAccess(), fastIsochroneFactory.getIsochroneNodeStorage(), fastIsochroneFactory.getCellStorage());
        contour.calculateContour();
    }

    private void calculateCellProperties(Weighting weighting, EdgeFilter edgeFilter, FlagEncoder flagEncoder, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        if (eccentricity == null)
            eccentricity = new Eccentricity(getGraphHopperStorage(), getLocationIndex(), isochroneNodeStorage, cellStorage);
        if (!eccentricity.loadExisting(weighting)) {
            eccentricity.calcEccentricities(weighting, edgeFilter, flagEncoder);
            eccentricity.calcBorderNodeDistances(weighting, edgeFilter, flagEncoder);
        }
    }

    public Eccentricity getEccentricity() {
        return eccentricity;
    }

    public RouteSegmentInfo[] getMatchedSegmentsInternal(Geometry geometry,
                                                         double originalTrafficLinkLength,
                                                         int trafficLinkFunctionalClass,
                                                         boolean bothDirections,
                                                         int matchingRadius) {
        if (mMapMatcher == null || mMapMatcher.getClass() != HiddenMarkovMapMatcher.class) {
            mMapMatcher = new HiddenMarkovMapMatcher();
            if (this.getGraphHopperStorage() != null) {
                mMapMatcher.setGraphHopper(this);
            }
        } else {
            mMapMatcher.clear();
        }

        if (trafficEdgeFilter == null) {
            trafficEdgeFilter = new TrafficEdgeFilter(getGraphHopperStorage());
        }
        trafficEdgeFilter.setHereFunctionalClass(trafficLinkFunctionalClass);
        mMapMatcher.setEdgeFilter(trafficEdgeFilter);

        RouteSegmentInfo[] routeSegmentInfos;
        mMapMatcher.setSearchRadius(matchingRadius);
        routeSegmentInfos = matchInternalSegments(geometry, originalTrafficLinkLength, bothDirections);
        for (RouteSegmentInfo routeSegmentInfo : routeSegmentInfos) {
            if (routeSegmentInfo != null) {
                return routeSegmentInfos;
            }
        }
        return routeSegmentInfos;
    }

    private RouteSegmentInfo[] matchInternalSegments(Geometry geometry, double originalTrafficLinkLength, boolean bothDirections) {

        if (trafficEdgeFilter == null || !trafficEdgeFilter.getClass().equals(TrafficEdgeFilter.class)) {
            return new RouteSegmentInfo[]{};
        }
        org.locationtech.jts.geom.Coordinate[] locations = geometry.getCoordinates();
        int originalFunctionalClass = trafficEdgeFilter.getHereFunctionalClass();
        RouteSegmentInfo[] match = mMapMatcher.match(locations, bothDirections);
        match = validateRouteSegment(originalTrafficLinkLength, match);

        if (match.length <= 0 && (originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS1.value && originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS1LINK.value)) {
            // Test a higher functional class based from the original class
//            ((TrafficEdgeFilter) edgeFilter).setHereFunctionalClass(originalFunctionalClass);
            trafficEdgeFilter.higherFunctionalClass();
            mMapMatcher.setEdgeFilter(trafficEdgeFilter);
            match = mMapMatcher.match(locations, bothDirections);
            match = validateRouteSegment(originalTrafficLinkLength, match);
        }
        if (match.length <= 0 && (originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value && originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value)) {
            // Try matching in the next lower functional class.
            trafficEdgeFilter.setHereFunctionalClass(originalFunctionalClass);
            trafficEdgeFilter.lowerFunctionalClass();
            mMapMatcher.setEdgeFilter(trafficEdgeFilter);
            match = mMapMatcher.match(locations, bothDirections);
            match = validateRouteSegment(originalTrafficLinkLength, match);
        }
        if (match.length <= 0 && (originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value && originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value)) {
            // But always try UNCLASSIFIED before. CLASS5 hast way too many false-positives!
            trafficEdgeFilter.setHereFunctionalClass(TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value);
            mMapMatcher.setEdgeFilter(trafficEdgeFilter);
            match = mMapMatcher.match(locations, bothDirections);
            match = validateRouteSegment(originalTrafficLinkLength, match);
        }
        if (match.length <= 0 && (originalFunctionalClass == TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value || originalFunctionalClass == TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value || originalFunctionalClass == TrafficRelevantWayType.RelevantWayTypes.CLASS1.value)) {
            // If the first tested class was unclassified, try CLASS5. But always try UNCLASSIFIED before. CLASS5 hast way too many false-positives!
            trafficEdgeFilter.setHereFunctionalClass(TrafficRelevantWayType.RelevantWayTypes.CLASS5.value);
            mMapMatcher.setEdgeFilter(trafficEdgeFilter);
            match = mMapMatcher.match(locations, bothDirections);
            match = validateRouteSegment(originalTrafficLinkLength, match);
        }
        return match;
    }


    private RouteSegmentInfo[] validateRouteSegment(double originalTrafficLinkLength, RouteSegmentInfo[] routeSegmentInfo) {
        if (routeSegmentInfo == null || routeSegmentInfo.length == 0)
            // Cases that shouldn't happen while matching Here data correctly. Return empty array to potentially restart the matching.
            return new RouteSegmentInfo[]{};
        int nullCounter = 0;
        for (int i = 0; i < routeSegmentInfo.length; i++) {
            if (routeSegmentInfo[i] == null || routeSegmentInfo[i].getEdgesStates() == null) {
                nullCounter += 1;
                break;
            }
            RouteSegmentInfo routeSegment = routeSegmentInfo[i];
            if (routeSegment.getDistance() > (originalTrafficLinkLength * 1.8)) {
                // Worst case scenario!
                routeSegmentInfo[i] = null;
                nullCounter += 1;
            }
        }

        if (nullCounter == routeSegmentInfo.length)
            return new RouteSegmentInfo[]{};
        else
            return routeSegmentInfo;
    }

    public boolean isTrafficEnabled() {
        return GraphStorageUtils.getGraphExtension(getGraphHopperStorage(), TrafficGraphStorage.class) != null;
    }
	public long getMemoryUsage() {
		long mem = 0;
		if (getLMPreparationHandler().isEnabled()) {
			mem += getLMPreparationHandler().getPreparations().stream().mapToLong(lm -> lm.getLandmarkStorage().getCapacity()).sum();
		}
		if (isCoreEnabled()) {
			// core CH preparations are handled in ORSGraphHopperStorage.getCapacity()
			mem += coreLMPreparationHandler.getPreparations().stream().mapToLong(lm -> lm.getLandmarkStorage().getCapacity()).sum();
		}
		if (fastIsochroneFactory.isEnabled()) {
			mem += fastIsochroneFactory.getCapacity();
		}
		return mem + getGraphHopperStorage().getCapacity();
	}
}
