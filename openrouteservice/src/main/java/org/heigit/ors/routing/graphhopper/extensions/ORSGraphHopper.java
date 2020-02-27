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
import com.graphhopper.util.exceptions.PointNotFoundException;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.routing.RoutingProfileCategory;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreAlgoFactoryDecorator;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMAlgoFactoryDecorator;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidBordersCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidFeaturesCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.HeavyVehicleCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.WheelchairCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.util.CoordTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static com.graphhopper.routing.weighting.TurnWeighting.INFINITE_U_TURN_COSTS;
import static com.graphhopper.util.Parameters.Algorithms.*;



public class ORSGraphHopper extends GraphHopper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ORSGraphHopper.class);

	private GraphProcessContext processContext;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds; // one osm id can correspond to multiple edges
	private HashMap<Integer, Long> tmcEdges;

	private int minNetworkSize = 200;
	private int minOneWayNetworkSize = 0;

	private final CoreAlgoFactoryDecorator coreFactoryDecorator =  new CoreAlgoFactoryDecorator();

	private final CoreLMAlgoFactoryDecorator coreLMFactoryDecorator = new CoreLMAlgoFactoryDecorator();

	public ORSGraphHopper(GraphProcessContext procCntx) {
		processContext = procCntx;
		forDesktop();
		algoDecorators.clear();
		algoDecorators.add(coreFactoryDecorator);
		algoDecorators.add(coreLMFactoryDecorator);
		algoDecorators.add(getCHFactoryDecorator());
		algoDecorators.add(getLMFactoryDecorator());
		processContext.init(this);
	}


	public ORSGraphHopper() {
		// used to initialize tests more easily without the need to create GraphProcessContext etc. when they're anyway not used in the tested functions.
	}

	@Override
	public GraphHopper init(CmdArgs args) {
		GraphHopper ret = super.init(args);
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
				throw new IllegalStateException("No routing algorithm set.");

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

			PathProcessor pathProcessor = pathProcessorFactory.createPathProcessor(request.getAdditionalHints(), encoder, getGraphHopperStorage());
			ghRsp.addReturnObject(pathProcessor);

			List<Path> altPaths = null;
			int maxRetries = routingTemplate.getMaxRetries();
			Locale locale = request.getLocale();
			Translation tr = getTranslationMap().getWithFallBack(locale);
			for (int i = 0; i < maxRetries; i++) {
				StopWatch sw = new StopWatch().start();
				List<QueryResult> qResults = routingTemplate.lookup(points, encoder);
				double[] radiuses = request.getMaxSearchDistances();
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

				int uTurnCosts = hints.getInt(Parameters.Routing.U_TURN_COSTS, INFINITE_U_TURN_COSTS);
				weighting = createTurnWeighting(queryGraph, weighting, tMode, uTurnCosts);
				if (weighting instanceof TurnWeighting)
	                ((TurnWeighting)weighting).setInORS(true);

				weighting = createTimeDependentAccessWeighting(weighting, tMode, algoStr);

				String departureTimeString = hints.get("departure", "");

				if (!departureTimeString.isEmpty() && weighting.isTimeDependent()) {
					LocalDateTime localDateTime = LocalDateTime.parse(departureTimeString);
					GHPoint3D snappedPoint = qResults.get(0).getSnappedPoint();
					String timeZoneId = "Europe/Berlin";//timeZoneMap.getOverlappingTimeZone(snappedPoint.lat, snappedPoint.lon).get().getZoneId();
					hints.put("departure", localDateTime.atZone(ZoneId.of(timeZoneId)).toInstant());
				} else {
					hints.remove("departure");
				}

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
						.setPathProcessor(pathProcessor)
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

		if (encodingManager.hasEncoder("heavyvehicle")) {
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
}
