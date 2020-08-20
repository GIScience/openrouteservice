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

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.CHAlgoFactoryDecorator;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.lm.LMAlgoFactoryDecorator;
import com.graphhopper.routing.template.AlternativeRoutingTemplate;
import com.graphhopper.routing.template.RoundTripRoutingTemplate;
import com.graphhopper.routing.template.RoutingTemplate;
import com.graphhopper.routing.template.ViaRoutingTemplate;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHProfile;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.*;
import com.graphhopper.util.exceptions.ConnectionNotFoundException;
import com.graphhopper.util.exceptions.PointNotFoundException;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.fastisochrones.Contour;
import org.heigit.ors.fastisochrones.Eccentricity;
import org.heigit.ors.isochrones.IsochroneWeightingFactory;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.fastisochrones.partitioning.FastIsochroneFactory;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfileCategory;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreAlgoFactoryDecorator;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMAlgoFactoryDecorator;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidFeaturesEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidBordersCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidFeaturesCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.HeavyVehicleCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.WheelchairCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.MaximumSpeedCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import org.heigit.ors.routing.graphhopper.extensions.weighting.MaximumSpeedWeighting;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.CoordTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static com.graphhopper.routing.weighting.TurnWeighting.INFINITE_U_TURN_COSTS;
import static com.graphhopper.util.Parameters.Algorithms.*;



public class ORSGraphHopper extends GraphHopper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ORSGraphHopper.class);

	private GraphProcessContext processContext;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds; // one osm id can correspond to multiple edges
	private HashMap<Integer, Long> tmcEdges;
	private Eccentricity eccentricity;

	private int minNetworkSize = 200;
	private int minOneWayNetworkSize = 0;

	private final CoreAlgoFactoryDecorator coreFactoryDecorator =  new CoreAlgoFactoryDecorator();
	private final CoreLMAlgoFactoryDecorator coreLMFactoryDecorator = new CoreLMAlgoFactoryDecorator();
	private final FastIsochroneFactory fastIsochroneFactory = new FastIsochroneFactory();

	private double maximumSpeedLowerBound;

	public ORSGraphHopper(GraphProcessContext procCntx) {
		processContext = procCntx;
		forDesktop();
		algoDecorators.clear();
		algoDecorators.add(coreFactoryDecorator);
		algoDecorators.add(coreLMFactoryDecorator);
		algoDecorators.add(getCHFactoryDecorator());
		algoDecorators.add(getLMFactoryDecorator());
		processContext.init(this);
		maximumSpeedLowerBound = procCntx.getMaximumSpeedLowerBound();

	}


	public ORSGraphHopper() {
		// used to initialize tests more easily without the need to create GraphProcessContext etc. when they're anyway not used in the tested functions.
	}

	@Override
	public GraphHopper init(CmdArgs args) {
		GraphHopper ret = super.init(args);
		fastIsochroneFactory.init(args);
		minNetworkSize = args.getInt("prepare.min_network_size", minNetworkSize);
		minOneWayNetworkSize = args.getInt("prepare.min_one_way_network_size", minOneWayNetworkSize);

		return ret;
	}

	@Override
	protected void cleanUp() {
		if (LOGGER.isInfoEnabled())
			LOGGER.info(String.format("call cleanUp for '%s' ", getGraphHopperLocation()));
		GraphHopperStorage ghs = getGraphHopperStorage();
		if (ghs != null) {
			if (LOGGER.isInfoEnabled())
				LOGGER.info(String.format("graph %s, details:%s", ghs.toString(), ghs.toDetailsString()));
			int prevNodeCount = ghs.getNodes();
			int ex = ghs.getAllEdges().length();
			List<FlagEncoder> list = getEncodingManager().fetchEdgeEncoders();
			if (LOGGER.isInfoEnabled())
				LOGGER.info(String.format("will create PrepareRoutingSubnetworks with:%n\tNodeCountBefore: '%d'%n\tgetAllEdges().getMaxId(): '%d'%n\tList<FlagEncoder>: '%s'%n\tminNetworkSize: '%d'%n\tminOneWayNetworkSize: '%d'", prevNodeCount, ex, list, minNetworkSize, minOneWayNetworkSize)
			);
		} else {
			LOGGER.info("graph GraphHopperStorage is null?!");
		}
		super.cleanUp();
	}

	@Override
	protected DataReader createReader(GraphHopperStorage tmpGraph) {
		return initDataReader(new ORSOSMReader(tmpGraph, processContext));
	}

	@SuppressWarnings("unchecked")
	@Override
	public GraphHopper importOrLoad() {
		GraphHopper gh = super.importOrLoad();

		if ((tmcEdges != null) && (osmId2EdgeIds !=null)) {
			java.nio.file.Path path = Paths.get(gh.getGraphHopperLocation(), "edges_ors_traffic");

			if ((tmcEdges.size() == 0) || (osmId2EdgeIds.size()==0)) {
				// try to load TMC edges from file.

				File file = path.toFile();
				if (file.exists()) {
					try (FileInputStream fis = new FileInputStream(path.toString());
						 ObjectInputStream ois = new ObjectInputStream(fis)) {
						tmcEdges = (HashMap<Integer, Long>)ois.readObject();
						osmId2EdgeIds = (HashMap<Long, ArrayList<Integer>>)ois.readObject();
						LOGGER.info("Serialized HashMap data is saved in trafficEdges");
					} catch (IOException ioe) {
						LOGGER.error(Arrays.toString(ioe.getStackTrace()));
					}
					catch(ClassNotFoundException c) {
						LOGGER.error("Class not found");
						LOGGER.error(Arrays.toString(c.getStackTrace()));
					}
				}
			} else {
				// save TMC edges if needed.
				try (FileOutputStream fos = new FileOutputStream(path.toString());
					 ObjectOutputStream oos = new ObjectOutputStream(fos)){
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
	public List<Path> calcPaths(GHRequest request, GHResponse ghRsp) {
		if (getGraphHopperStorage() == null || !isFullyLoaded())
			throw new IllegalStateException("Do a successful call to load or importOrLoad before routing");

		if (getGraphHopperStorage().isClosed())
			throw new IllegalStateException("You need to create a new GraphHopper instance as it is already closed");

		// default handling
		String vehicle = request.getVehicle();
		if (vehicle.isEmpty()) {
			vehicle = getDefaultVehicle().toString();
			request.setVehicle(vehicle);
		}

		Lock readLock = getReadWriteLock().readLock();
		readLock.lock();
		try {
			if (!getEncodingManager().hasEncoder(vehicle))
				throw new IllegalArgumentException(
						"Vehicle " + vehicle + " unsupported. " + "Supported are: " + getEncodingManager());

			HintsMap hints = request.getHints();
			String tModeStr = hints.get("traversal_mode", TraversalMode.EDGE_BASED.name());
			TraversalMode tMode = TraversalMode.fromString(tModeStr);
			if (hints.has(Parameters.Routing.EDGE_BASED))
				tMode = hints.getBool(Parameters.Routing.EDGE_BASED, false) ? TraversalMode.EDGE_BASED
						: TraversalMode.NODE_BASED;

			FlagEncoder encoder = getEncodingManager().getEncoder(vehicle);

			boolean disableCH = hints.getBool(Parameters.CH.DISABLE, false);
			if (!getCHFactoryDecorator().isDisablingAllowed() && disableCH)
				throw new IllegalArgumentException("Disabling CH not allowed on the server-side");

			boolean disableLM = hints.getBool(Parameters.Landmark.DISABLE, false);
			if (!getLMFactoryDecorator().isDisablingAllowed() && disableLM)
				throw new IllegalArgumentException("Disabling LM not allowed on the server-side");

			//TODO
			boolean disableCore = hints.getBool(ORSParameters.Core.DISABLE, false);

			String algoStr = request.getAlgorithm();
			if (algoStr.isEmpty())
				algoStr = getCHFactoryDecorator().isEnabled() && !disableCH
						&& !(getLMFactoryDecorator().isEnabled() && !disableLM) ? DIJKSTRA_BI : ASTAR_BI;

			List<GHPoint> points = request.getPoints();
			// TODO Maybe we should think about a isRequestValid method that checks all that stuff that we could do to fail fast
			// For example see #734
			checkIfPointsAreInBounds(points);

			RoutingTemplate routingTemplate;
			if (ROUND_TRIP.equalsIgnoreCase(algoStr))
				routingTemplate = new RoundTripRoutingTemplate(request, ghRsp, getLocationIndex(), getEncodingManager(), getMaxRoundTripRetries());
			else if (ALT_ROUTE.equalsIgnoreCase(algoStr))
				routingTemplate = new AlternativeRoutingTemplate(request, ghRsp, getLocationIndex(), getEncodingManager());
			else
				routingTemplate = new ViaRoutingTemplate(request, ghRsp, getLocationIndex(), getEncodingManager());

			EdgeFilter edgeFilter = edgeFilterFactory.createEdgeFilter(request.getAdditionalHints(), encoder, getGraphHopperStorage());
			routingTemplate.setEdgeFilter(edgeFilter);

			for (int c = 0; c < request.getHints().getInt("alternative_route.max_paths", 1); c++) {
				ghRsp.addReturnObject(pathProcessorFactory.createPathProcessor(request.getAdditionalHints(), encoder, getGraphHopperStorage()));
			}
			List<PathProcessor> ppList = new ArrayList<>();
			for (Object returnObject : ghRsp.getReturnObjects()) {
				if (returnObject instanceof PathProcessor) {
					ppList.add((PathProcessor)returnObject);
				}
			}

			List<Path> altPaths = null;
			int maxRetries = routingTemplate.getMaxRetries();
			Locale locale = request.getLocale();
			Translation tr = getTranslationMap().getWithFallBack(locale);
			for (int i = 0; i < maxRetries; i++) {
				StopWatch sw = new StopWatch().start();
				List<QueryResult> qResults = routingTemplate.lookup(points, encoder);
				double[] radiuses = request.getMaxSearchDistances();
				checkAvoidBorders(processContext, request, qResults);
				if (points.size() == qResults.size()) {
					for (int placeIndex = 0; placeIndex < points.size(); placeIndex++) {
						QueryResult qr = qResults.get(placeIndex);
						if ((radiuses != null) && qr.isValid() && (qr.getQueryDistance() > radiuses[placeIndex]) && (radiuses[placeIndex] != -1.0)) {
							ghRsp.addError(new PointNotFoundException("Cannot find point " + placeIndex + ": " + points.get(placeIndex) + " within a radius of " + radiuses[placeIndex] + " meters.", placeIndex));
						}
					}
				}
				ghRsp.addDebugInfo("idLookup:" + sw.stop().getSeconds() + "s");
				if (ghRsp.hasErrors())
					return Collections.emptyList();

				RoutingAlgorithmFactory tmpAlgoFactory = getAlgorithmFactory(hints);
				Weighting weighting;
				QueryGraph queryGraph;

				if (coreFactoryDecorator.isEnabled() && !disableCore) {
					boolean forceCHHeading = hints.getBool(Parameters.CH.FORCE_HEADING, false);
					if (!forceCHHeading && request.hasFavoredHeading(0))
						throw new IllegalArgumentException(
								"Heading is not (fully) supported for CHGraph. See issue #483");

					// if LM is enabled we have the LMFactory with the CH algo!
					RoutingAlgorithmFactory chAlgoFactory = tmpAlgoFactory;
					if (tmpAlgoFactory instanceof CoreLMAlgoFactoryDecorator.CoreLMRAFactory)
						chAlgoFactory = ((CoreLMAlgoFactoryDecorator.CoreLMRAFactory) tmpAlgoFactory).getDefaultAlgoFactory();

					if (chAlgoFactory instanceof PrepareCore)
						weighting = ((PrepareCore) chAlgoFactory).getWeighting();
					else
						throw new IllegalStateException(
								"Although CH was enabled a non-CH algorithm factory was returned " + tmpAlgoFactory);

					RoutingAlgorithmFactory coreAlgoFactory = coreFactoryDecorator.getDecoratedAlgorithmFactory(new RoutingAlgorithmFactorySimple(), hints);
					CHProfile chProfile = ((PrepareCore) coreAlgoFactory).getCHProfile();

					queryGraph = new QueryGraph(getGraphHopperStorage().getCHGraph(chProfile));
					queryGraph.lookup(qResults);
				}
				else{
					if (getCHFactoryDecorator().isEnabled() && !disableCH) {
						boolean forceCHHeading = hints.getBool(Parameters.CH.FORCE_HEADING, false);
						if (!forceCHHeading && request.hasFavoredHeading(0))
							throw new IllegalArgumentException(
									"Heading is not (fully) supported for CHGraph. See issue #483");

						// if LM is enabled we have the LMFactory with the CH algo!
						RoutingAlgorithmFactory chAlgoFactory = tmpAlgoFactory;
						if (tmpAlgoFactory instanceof LMAlgoFactoryDecorator.LMRAFactory)
							chAlgoFactory = ((LMAlgoFactoryDecorator.LMRAFactory) tmpAlgoFactory).getDefaultAlgoFactory();

						if (chAlgoFactory instanceof PrepareContractionHierarchies)
							weighting = ((PrepareContractionHierarchies) chAlgoFactory).getWeighting();
						else
							throw new IllegalStateException(
									"Although CH was enabled a non-CH algorithm factory was returned " + tmpAlgoFactory);

						tMode = TraversalMode.NODE_BASED;
						queryGraph = new QueryGraph(getGraphHopperStorage().getCHGraph(((PrepareContractionHierarchies) chAlgoFactory).getCHProfile()));
						queryGraph.lookup(qResults);
					} else {
						checkNonChMaxWaypointDistance(points);
						queryGraph = new QueryGraph(getGraphHopperStorage());
						queryGraph.lookup(qResults);
						weighting = createWeighting(hints, encoder, queryGraph);
						ghRsp.addDebugInfo("tmode:" + tMode.toString());
					}
				}

				int maxVisitedNodesForRequest = hints.getInt(Parameters.Routing.MAX_VISITED_NODES, getMaxVisitedNodes());
				if (maxVisitedNodesForRequest > getMaxVisitedNodes())
					throw new IllegalArgumentException(
							"The max_visited_nodes parameter has to be below or equal to:" + getMaxVisitedNodes());


				if(hints.has("maximum_speed")) {
					weighting = new MaximumSpeedWeighting(encoder, hints, weighting, maximumSpeedLowerBound);
				}


				int uTurnCosts = hints.getInt(Parameters.Routing.U_TURN_COSTS, INFINITE_U_TURN_COSTS);
				weighting = createTurnWeighting(queryGraph, weighting, tMode, uTurnCosts);
				if (weighting instanceof TurnWeighting)
	                ((TurnWeighting)weighting).setInORS(true);

				AlgorithmOptions algoOpts = AlgorithmOptions.start().algorithm(algoStr).traversalMode(tMode)
						.weighting(weighting).maxVisitedNodes(maxVisitedNodesForRequest).hints(hints).build();

				algoOpts.setEdgeFilter(edgeFilter);

				altPaths = routingTemplate.calcPaths(queryGraph, tmpAlgoFactory, algoOpts);

				String date = getGraphHopperStorage().getProperties().get("datareader.data.date");
				if (Helper.isEmpty(date)) {
					date = getGraphHopperStorage().getProperties().get("datareader.import.date");
				}
				ghRsp.getHints().put("data.date", date);

				boolean tmpEnableInstructions = hints.getBool(Parameters.Routing.INSTRUCTIONS, getEncodingManager().isEnableInstructions());
				boolean tmpCalcPoints = hints.getBool(Parameters.Routing.CALC_POINTS, isCalcPoints());
				double wayPointMaxDistance = hints.getDouble(Parameters.Routing.WAY_POINT_MAX_DISTANCE, 1d);
				DouglasPeucker peucker = new DouglasPeucker().setMaxDistance(wayPointMaxDistance);
				PathMerger pathMerger = new PathMerger().setCalcPoints(tmpCalcPoints).setDouglasPeucker(peucker)
                        .setEnableInstructions(tmpEnableInstructions)
						.setPathProcessor(ppList.toArray(new PathProcessor[]{}))
						.setSimplifyResponse(isSimplifyResponse() && wayPointMaxDistance > 0);

				if (routingTemplate.isReady(pathMerger, tr))
					break;
			}

			return altPaths;

		} catch (IllegalArgumentException ex) {
			ghRsp.addError(ex);
			return Collections.emptyList();
		} finally {
			readLock.unlock();
		}
	}

	public RouteSegmentInfo getRouteSegment(double[] latitudes, double[] longitudes, String vehicle) {
		RouteSegmentInfo result = null;

		GHRequest req = new GHRequest();
		for (int i = 0; i < latitudes.length; i++)
			req.addPoint(new GHPoint(latitudes[i], longitudes[i]));

		req.setVehicle(vehicle);
		req.setAlgorithm("dijkstrabi");
		req.setWeighting("fastest");
		// TODO add limit of maximum visited nodes


		GHResponse resp = new GHResponse();

		List<Path> paths = this.calcPaths(req, resp);

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
	 * @param processContext Used to get the bordersReader to check isOpen for avoid Controlled. Currently not used
	 * @param request To get the avoid borders setting
	 * @param queryResult To get the edges of the queries and check which country they're in
	 */
	private void checkAvoidBorders(GraphProcessContext processContext, GHRequest request, List<QueryResult> queryResult) {
		/* Avoid borders */
		ORSPMap params = (ORSPMap)request.getAdditionalHints();
		if (params == null) {
			params = new ORSPMap();
		}
		boolean isRouteable = true;

		if (params.hasObj("avoid_borders")) {
				RouteSearchParameters routeSearchParameters = (RouteSearchParameters) params.getObj("avoid_borders");
				//Avoiding All borders
				if(routeSearchParameters.hasAvoidBorders() && routeSearchParameters.getAvoidBorders() == BordersExtractor.Avoid.ALL) {
					List<Integer> edgeIds =  new ArrayList<>();
					for (int placeIndex = 0; placeIndex < queryResult.size(); placeIndex++) {
						edgeIds.add(queryResult.get(placeIndex).getClosestEdge().getEdge());
					}
					BordersExtractor bordersExtractor = new BordersExtractor(GraphStorageUtils.getGraphExtension(getGraphHopperStorage(), BordersGraphStorage.class), null);
					isRouteable = bordersExtractor.isSameCountry(edgeIds);
				}
				//TODO Avoiding CONTROLLED borders
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
			throw new ConnectionNotFoundException("Route not found due to avoiding borders", Collections.<String, Object>emptyMap());

	}

    public GHResponse constructFreeHandRoute(GHRequest request) {
        LineString directRouteGeometry = constructFreeHandRouteGeometry(request);
        PathWrapper directRoutePathWrapper = constructFreeHandRoutePathWrapper(directRouteGeometry);
        GHResponse directRouteResponse = new GHResponse();
        directRouteResponse.add(directRoutePathWrapper);
        directRouteResponse.getHints().put("skipped_segment", "true");
        return directRouteResponse;
    }

    private PathWrapper constructFreeHandRoutePathWrapper(LineString lineString) {
        PathWrapper pathWrapper = new PathWrapper();
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
        Instruction startInstruction = new Instruction(Instruction.REACHED_VIA, "free hand route", new InstructionAnnotation(0, ""), startPointList);
        Instruction endInstruction = new Instruction(Instruction.FINISH, "end of free hand route", new InstructionAnnotation(0, ""), endPointList);
        instructions.add(0, startInstruction);
        instructions.add(1, endInstruction);
        pathWrapper.setDistance(distance);
        pathWrapper.setAscend(0.0);
        pathWrapper.setDescend(0.0);
        pathWrapper.setTime(0);
        pathWrapper.setInstructions(instructions);
        pathWrapper.setWaypoints(wayPointList);
        pathWrapper.setPoints(pointList);
        pathWrapper.setRouteWeight(0.0);
        pathWrapper.setDescription(new ArrayList<>());
        pathWrapper.setImpossible(false);
        startInstruction.setDistance(distance);
        startInstruction.setTime(0);
        return pathWrapper;
    }

    private LineString constructFreeHandRouteGeometry(GHRequest request){
        Coordinate start = new Coordinate();
        Coordinate end = new Coordinate();
        start.x = request.getPoints().get(0).getLat();
        start.y = request.getPoints().get(0).getLon();
        end.x = request.getPoints().get(1).getLat();
        end.y = request.getPoints().get(1).getLon();
        Coordinate[] coords = new Coordinate[]{start, end};
        return new GeometryFactory().createLineString(coords);
    }


    public HashMap<Integer, Long> getTmcGraphEdges() {
        return tmcEdges;
    }

    public HashMap<Long, ArrayList<Integer>> getOsmId2EdgeIds() {
        return osmId2EdgeIds;
    }

	/**
	 * Does the preparation and creates the location index
	 */
	@Override
	public void postProcessing() {
		super.postProcessing();

		GraphHopperStorage gs = getGraphHopperStorage();

		EncodingManager encodingManager = getEncodingManager();

		int routingProfileCategory = RoutingProfileCategory.getFromEncoder(encodingManager);

		/* Initialize edge filter sequence */

		EdgeFilterSequence coreEdgeFilter = new EdgeFilterSequence();
		/* Heavy vehicle filter */

		if (encodingManager.hasEncoder(FlagEncoderNames.HEAVYVEHICLE)) {
			coreEdgeFilter.add(new HeavyVehicleCoreEdgeFilter(gs));
		}

		/* Avoid features */

		if ((routingProfileCategory & (RoutingProfileCategory.DRIVING | RoutingProfileCategory.CYCLING | RoutingProfileCategory.WALKING | RoutingProfileCategory.WHEELCHAIR)) != 0) {
			coreEdgeFilter.add(new AvoidFeaturesCoreEdgeFilter(gs, routingProfileCategory));
		}

		/* Avoid borders of some form */

		if ((routingProfileCategory & (RoutingProfileCategory.DRIVING | RoutingProfileCategory.CYCLING)) != 0) {
			coreEdgeFilter.add(new AvoidBordersCoreEdgeFilter(gs));
		}

		if (routingProfileCategory == RoutingProfileCategory.WHEELCHAIR) {
			coreEdgeFilter.add(new WheelchairCoreEdgeFilter(gs));
		}

		/* Maximum Speed Filter */
		if ((routingProfileCategory & RoutingProfileCategory.DRIVING) !=0 ) {
			FlagEncoder flagEncoder = null;
			if(encodingManager.hasEncoder(FlagEncoderNames.HEAVYVEHICLE)) {
				flagEncoder = getEncodingManager().getEncoder(FlagEncoderNames.HEAVYVEHICLE);
				coreEdgeFilter.add(new MaximumSpeedCoreEdgeFilter(flagEncoder, maximumSpeedLowerBound));
			}
			else if(encodingManager.hasEncoder(FlagEncoderNames.CAR_ORS)) {
				flagEncoder = getEncodingManager().getEncoder(FlagEncoderNames.CAR_ORS);
				coreEdgeFilter.add(new MaximumSpeedCoreEdgeFilter(flagEncoder, maximumSpeedLowerBound));
			}
		}

		/* End filter sequence initialization */

		//Create the core
		if(coreFactoryDecorator.isEnabled())
			coreFactoryDecorator.createPreparations(gs, coreEdgeFilter);
		if (!isCorePrepared())
			prepareCore();

		//Create the landmarks in the core
		if (coreLMFactoryDecorator.isEnabled())
			coreLMFactoryDecorator.createPreparations(gs, super.getLocationIndex());
		loadOrPrepareCoreLM();

		if(fastIsochroneFactory.isEnabled()) {
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
				/* Initialize edge filter sequence for fast isochrones*/
				calculateContours();
				List<CHProfile> chProfiles = new ArrayList<>();
				for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
					for (String coreWeightingStr : fastIsochroneFactory.getFastisochroneProfileStrings()) {
						Weighting weighting = createWeighting(new HintsMap(coreWeightingStr).put("isochroneWeighting", "true"), encoder, null);
						chProfiles.add(new CHProfile(weighting, TraversalMode.NODE_BASED, INFINITE_U_TURN_COSTS, "isocore"));
					}
				}

				for (CHProfile chProfile : chProfiles) {
					for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
						calculateCellProperties(chProfile.getWeighting(), encoder, fastIsochroneFactory.getIsochroneNodeStorage(), fastIsochroneFactory.getCellStorage());
					}
				}
			}
		}

	}


	/**
	 * Enables or disables core calculation.
	 */
	public GraphHopper setCoreEnabled(boolean enable) {
		ensureNotLoaded();
		coreFactoryDecorator.setEnabled(enable);
		return this;
	}

	public final boolean isCoreEnabled() {
		return coreFactoryDecorator.isEnabled();
	}

	public void initCoreAlgoFactoryDecorator() {
		if (!coreFactoryDecorator.hasCHProfiles()) {
			for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
				for (String coreWeightingStr : coreFactoryDecorator.getCHProfileStrings()) {
					// ghStorage is null at this point
					Weighting weighting = createWeighting(new HintsMap(coreWeightingStr), encoder, null);
					coreFactoryDecorator.addCHProfile(new CHProfile(weighting, TraversalMode.NODE_BASED, INFINITE_U_TURN_COSTS, CHProfile.TYPE_CORE));
				}
			}
		}
	}
	public final CoreAlgoFactoryDecorator getCoreFactoryDecorator() {
		return coreFactoryDecorator;
	}

	protected void prepareCore() {
		boolean tmpPrepare = coreFactoryDecorator.isEnabled();
		if (tmpPrepare) {
			ensureWriteAccess();

			getGraphHopperStorage().freeze();
			coreFactoryDecorator.prepare(getGraphHopperStorage().getProperties());
			getGraphHopperStorage().getProperties().put(ORSParameters.Core.PREPARE + "done", true);
		}
	}

	private boolean isCorePrepared() {
		return "true".equals(getGraphHopperStorage().getProperties().get(ORSParameters.Core.PREPARE + "done"))
				// remove old property in >0.9
				|| "true".equals(getGraphHopperStorage().getProperties().get("prepare.done"));
	}

	/**
	 * Enables or disables core calculation.
	 */
	public GraphHopper setCoreLMEnabled(boolean enable) {
		ensureNotLoaded();
		coreLMFactoryDecorator.setEnabled(enable);
		return this;
	}

	public final boolean isCoreLMEnabled() {
		return coreLMFactoryDecorator.isEnabled();
	}

	public void initCoreLMAlgoFactoryDecorator() {
		if (!coreLMFactoryDecorator.hasWeightings()) {
			for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
				for (String coreWeightingStr : coreFactoryDecorator.getCHProfileStrings()) {
					// ghStorage is null at this point
					Weighting weighting = createWeighting(new HintsMap(coreWeightingStr), encoder, null);
					coreLMFactoryDecorator.addWeighting(weighting);
				}
			}
		}
	}


	/**
	 * For landmarks it is required to always call this method: either it creates the landmark data or it loads it.
	 */
	protected void loadOrPrepareCoreLM() {
		boolean tmpPrepare = coreLMFactoryDecorator.isEnabled();
		if (tmpPrepare) {
			ensureWriteAccess();
			getGraphHopperStorage().freeze();
			if (coreLMFactoryDecorator.loadOrDoWork(getGraphHopperStorage().getProperties()))
				getGraphHopperStorage().getProperties().put(ORSParameters.CoreLandmark.PREPARE + "done", true);
		}
	}

	public final boolean isCHAvailable(String weighting) {
		CHAlgoFactoryDecorator chFactoryDecorator = getCHFactoryDecorator();
		if (chFactoryDecorator.isEnabled() && chFactoryDecorator.hasCHProfiles()) {
			for (CHProfile chProfile : chFactoryDecorator.getCHProfiles()) {
				if (weighting.equals(chProfile.getWeighting().getName()))
					return true;
			}
		}
		return false;
	}

	public final boolean isLMAvailable(String weighting) {
		LMAlgoFactoryDecorator lmFactoryDecorator = getLMFactoryDecorator();
		if (lmFactoryDecorator.isEnabled()) {
			List<String> weightings = lmFactoryDecorator.getWeightingsAsStrings();
			return weightings.contains(weighting);
		}
		return false;
	}

	public final boolean isCoreAvailable(String weighting) {
		CoreAlgoFactoryDecorator coreFactoryDecorator = getCoreFactoryDecorator();
		if (coreFactoryDecorator.isEnabled() && coreFactoryDecorator.hasCHProfiles()) {
			for (CHProfile chProfile : coreFactoryDecorator.getCHProfiles()) {
				if (weighting.equals(chProfile.getWeighting().getName()))
					return true;
			}
		}
		return false;
	}
	public final boolean isFastIsochroneAvailable(RouteSearchContext searchContext, TravelRangeType travelRangeType) {
		if(eccentricity != null && eccentricity.isAvailable(IsochroneWeightingFactory.createIsochroneWeighting(searchContext, travelRangeType)))
		    return true;
		return false;
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

	private void calculateContours(){
		if(fastIsochroneFactory.getCellStorage().isContourPrepared())
			return;
		Contour contour = new Contour(getGraphHopperStorage(), getGraphHopperStorage().getNodeAccess(), fastIsochroneFactory.getIsochroneNodeStorage(), fastIsochroneFactory.getCellStorage());
		contour.calculateContour();
	}

	private void calculateCellProperties(Weighting weighting, FlagEncoder flagEncoder, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage){
		if (eccentricity == null)
			eccentricity = new Eccentricity(getGraphHopperStorage(), getLocationIndex(), isochroneNodeStorage, cellStorage);
		if(!eccentricity.loadExisting(weighting)) {
			eccentricity.calcEccentricities(weighting, flagEncoder);
			eccentricity.calcBorderNodeDistances(weighting, flagEncoder);
		}
	}

	public Eccentricity getEccentricity(){
		return eccentricity;
	}
}
