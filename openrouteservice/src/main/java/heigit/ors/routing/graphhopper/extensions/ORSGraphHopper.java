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
package heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.lm.LMAlgoFactoryDecorator;
import com.graphhopper.routing.template.AlternativeRoutingTemplate;
import com.graphhopper.routing.template.RoundTripRoutingTemplate;
import com.graphhopper.routing.template.RoutingTemplate;
import com.graphhopper.routing.template.ViaRoutingTemplate;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.*;
import com.graphhopper.util.exceptions.PointNotFoundException;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.routing.RoutingProfile;
import heigit.ors.util.CoordTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static com.graphhopper.util.Parameters.Algorithms.*;



public class ORSGraphHopper extends GraphHopper {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private GraphProcessContext _procCntx;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds; // one osm id can correspond to multiple edges
	private HashMap<Integer, Long> tmcEdges;

	private int _minNetworkSize = 200;
	private int _minOneWayNetworkSize = 0;

	// A route profile for referencing which is used to extract names of adjacent streets and other objects.
	private RoutingProfile refRouteProfile;

	public ORSGraphHopper(GraphProcessContext procCntx, RoutingProfile refProfile) {
		_procCntx = procCntx;
		this.refRouteProfile= refProfile;
		this.forDesktop();
		algoDecorators.clear();
		algoDecorators.add(getCHFactoryDecorator());
		algoDecorators.add(getLMFactoryDecorator());

//		if (useTmc){
//			tmcEdges = new HashMap<Integer, Long>();
//			osmId2EdgeIds = new HashMap<Long, ArrayList<Integer>>();
//		}
		_procCntx.init(this);
	}


	public ORSGraphHopper() {
		// used to initialize tests more easily without the need to create GraphProcessContext etc. when they're anyway not used in the tested functions.
	}

	@Override
	public GraphHopper init(CmdArgs args) {
		GraphHopper ret = super.init(args);
		_minNetworkSize = args.getInt("prepare.min_network_size", _minNetworkSize);
		_minOneWayNetworkSize = args.getInt("prepare.min_one_way_network_size", _minOneWayNetworkSize);
		return ret;
	}

	@Override
	protected void cleanUp() {
		logger.info("call cleanUp for '" + getGraphHopperLocation() + "' ");
		GraphHopperStorage ghs = getGraphHopperStorage();
		if (ghs != null) {
			this.logger.info("graph " + ghs.toString() + ", details:" + ghs.toDetailsString());
			int prevNodeCount = ghs.getNodes();
			int ex = ghs.getAllEdges().length();
			List<FlagEncoder> list = getEncodingManager().fetchEdgeEncoders();
			this.logger.info("will create PrepareRoutingSubnetworks with:\r\n"+
					"\tNodeCountBefore: '" + prevNodeCount+"'\r\n"+
					"\tgetAllEdges().getMaxId(): '" + ex+"'\r\n"+
					"\tList<FlagEncoder>: '" + list+"'\r\n"+
					"\tminNetworkSize: '" + _minNetworkSize+"'\r\n"+
					"\tminOneWayNetworkSize: '" + _minOneWayNetworkSize+"'"
			);
		} else {
			this.logger.info("graph GraphHopperStorage is null?!");
		}
		super.cleanUp();
	}


	protected DataReader createReader(GraphHopperStorage tmpGraph) {

		return initDataReader(new ORSOSMReader(tmpGraph, _procCntx, refRouteProfile));
	}

	public boolean load( String graphHopperFolder )
	{
		boolean res = super.load(graphHopperFolder);


		return res;
	}

	protected void flush()
	{
		super.flush();
	}

	@SuppressWarnings("unchecked")
	public GraphHopper importOrLoad() {
		GraphHopper gh = super.importOrLoad();


		if ((tmcEdges != null) && (osmId2EdgeIds !=null)) {
			java.nio.file.Path path = Paths.get(gh.getGraphHopperLocation(), "edges_ors_traffic");

			if ((tmcEdges.size() == 0) || (osmId2EdgeIds.size()==0)) {
				// try to load TMC edges from file.

				try {
					File file = path.toFile();

					if (file.exists())
					{
						FileInputStream fis = new FileInputStream(path.toString());
						ObjectInputStream ois = new ObjectInputStream(fis);
						tmcEdges = (HashMap<Integer, Long>)ois.readObject();
						osmId2EdgeIds = (HashMap<Long, ArrayList<Integer>>)ois.readObject();
						ois.close();
						fis.close();
						System.out.printf("Serialized HashMap data is saved in trafficEdges");
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				catch(ClassNotFoundException c)
				{
					System.out.println("Class not found");
					c.printStackTrace();
				}
			} else {
				// save TMC edges if needed.
				try {
					FileOutputStream fos = new FileOutputStream(path.toString());
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(tmcEdges);
					oos.writeObject(osmId2EdgeIds);
					oos.close();
					fos.close();
					System.out.printf("Serialized HashMap data is saved in trafficEdges");
				} catch (IOException ioe) {
					ioe.printStackTrace();
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
			String tModeStr = hints.get("traversal_mode", getTraversalMode().toString());
			TraversalMode tMode = TraversalMode.fromString(tModeStr);
			if (hints.has(Parameters.Routing.EDGE_BASED))
				tMode = hints.getBool(Parameters.Routing.EDGE_BASED, false) ? TraversalMode.EDGE_BASED_2DIR
						: TraversalMode.NODE_BASED;

			FlagEncoder encoder = getEncodingManager().getEncoder(vehicle);

			boolean disableCH = hints.getBool(Parameters.CH.DISABLE, false);
			if (!getCHFactoryDecorator().isDisablingAllowed() && disableCH)
				throw new IllegalArgumentException("Disabling CH not allowed on the server-side");

			boolean disableLM = hints.getBool(Parameters.Landmark.DISABLE, false);
			if (!getLMFactoryDecorator().isDisablingAllowed() && disableLM)
				throw new IllegalArgumentException("Disabling LM not allowed on the server-side");

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

			EdgeFilter edgeFilter = edgeFilterFactory.createEdgeFilter(hints, encoder, getGraphHopperStorage());
			routingTemplate.setEdgeFilter(edgeFilter);

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

//					tMode = getCHFactoryDecorator().getNodeBase();  this method simply returned NODE_BASED, when we implement support for edge-based CH this will probably have to be fixed or removed
					tMode = TraversalMode.NODE_BASED;
					queryGraph = new QueryGraph(getGraphHopperStorage().getGraph(CHGraph.class, weighting));
					queryGraph.lookup(qResults);


				} else {

					checkNonChMaxWaypointDistance(points);
					queryGraph = new QueryGraph(getGraphHopperStorage());
					queryGraph.lookup(qResults);
					weighting = createWeighting(hints, tMode, encoder, queryGraph);
					ghRsp.addDebugInfo("tmode:" + tMode.toString());
				}

				int maxVisitedNodesForRequest = hints.getInt(Parameters.Routing.MAX_VISITED_NODES, getMaxVisitedNodes());
				if (maxVisitedNodesForRequest > getMaxVisitedNodes())
					throw new IllegalArgumentException(
							"The max_visited_nodes parameter has to be below or equal to:" + getMaxVisitedNodes());

				weighting = createTurnWeighting(queryGraph, weighting, tMode);

				AlgorithmOptions algoOpts = AlgorithmOptions.start().algorithm(algoStr).traversalMode(tMode)
						.weighting(weighting).maxVisitedNodes(maxVisitedNodesForRequest).hints(hints).build();
				
				algoOpts.setEdgeFilter(edgeFilter);
				
				altPaths = routingTemplate.calcPaths(queryGraph, tmpAlgoFactory, algoOpts);

				boolean tmpEnableInstructions = hints.getBool(Parameters.Routing.INSTRUCTIONS, getEncodingManager().isEnableInstructions());
				boolean tmpCalcPoints = hints.getBool(Parameters.Routing.CALC_POINTS, isCalcPoints());
				double wayPointMaxDistance = hints.getDouble(Parameters.Routing.WAY_POINT_MAX_DISTANCE, 1d);
				DouglasPeucker peucker = new DouglasPeucker().setMaxDistance(wayPointMaxDistance);
				PathMerger pathMerger = new PathMerger().setCalcPoints(tmpCalcPoints).setDouglasPeucker(peucker)
                        .setEnableInstructions(tmpEnableInstructions)
                        .setPathProcessor(pathProcessorFactory.createPathProcessor(hints, getGraphHopperStorage(), encoder))
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

			List<EdgeIteratorState> fullEdges = new ArrayList<EdgeIteratorState>();
			List<String> edgeNames = new ArrayList<String>();
			PointList fullPoints = PointList.EMPTY;
			long time = 0;
			double distance = 0;
			for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
				Path path = paths.get(pathIndex);
				time += path.getTime();

				for (EdgeIteratorState edge : path.calcEdges()) {
					fullEdges.add(edge);
					edgeNames.add(edge.getName());
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


	}

}