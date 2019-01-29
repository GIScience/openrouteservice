/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;

import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.lm.LMAlgoFactoryDecorator;
import com.graphhopper.routing.template.AlternativeRoutingTemplate;
import com.graphhopper.routing.template.RoundTripRoutingTemplate;
import com.graphhopper.routing.template.RoutingTemplate;
import com.graphhopper.routing.template.ViaRoutingTemplate;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.*;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.CmdArgs;
import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.routing.*;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import heigit.ors.routing.graphhopper.extensions.core.CoreAlgoFactoryDecorator;
import heigit.ors.routing.graphhopper.extensions.core.CoreLMAlgoFactoryDecorator;
import heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidBordersCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidFeaturesCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.HeavyVehicleCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.WheelchairCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import heigit.ors.routing.graphhopper.extensions.util.ORSParameters.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.graphhopper.util.Parameters.Algorithms.*;

public class ORSGraphHopper extends GraphHopper {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private GraphProcessContext _procCntx;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds; // one osm id can correspond to multiple edges
	private HashMap<Integer, Long> tmcEdges;

	// A route profile for referencing which is used to extract names of adjacent streets and other objects.
	private RoutingProfile refRouteProfile;

	private final CoreAlgoFactoryDecorator coreFactoryDecorator =  new CoreAlgoFactoryDecorator();

	private final CoreLMAlgoFactoryDecorator coreLMFactoryDecorator = new CoreLMAlgoFactoryDecorator();



	public ORSGraphHopper(GraphProcessContext procCntx, boolean useTmc, RoutingProfile refProfile) {
		_procCntx = procCntx;
		this.refRouteProfile= refProfile;
		this.forDesktop();
//		coreFactoryDecorator.setEnabled(false);
		algoDecorators.clear();
		algoDecorators.add(coreFactoryDecorator);
		algoDecorators.add(coreLMFactoryDecorator);
		algoDecorators.add(getCHFactoryDecorator());
		algoDecorators.add(getLMFactoryDecorator());

		if (useTmc){
			tmcEdges = new HashMap<Integer, Long>();
			osmId2EdgeIds = new HashMap<Long, ArrayList<Integer>>();
		}
		_procCntx.init(this);
	}

	protected DataReader createReader(GraphHopperStorage tmpGraph) {

		return initDataReader(new ORSOSMReader(tmpGraph, _procCntx, tmcEdges, osmId2EdgeIds, refRouteProfile));
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

	public List<Path> calcPaths(GHRequest request, GHResponse ghRsp) {
		if (ghStorage == null || !isFullyLoaded())
			throw new IllegalStateException("Do a successful call to load or importOrLoad before routing");

		if (ghStorage.isClosed())
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
			if (!getEncodingManager().supports(vehicle))
				throw new IllegalArgumentException(
						"Vehicle " + vehicle + " unsupported. " + "Supported are: " + getEncodingManager());

			HintsMap hints = request.getHints();
			String tModeStr = hints.get("traversal_mode", traversalMode.toString());
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

			//TODO
			boolean disableCore = hints.getBool(Core.DISABLE, false);
//			if (!CoreAlgoFactoryDecorator.isDisablingAllowed() && disableLM)
//			throw new IllegalArgumentException("Disabling LM not allowed on the server-side");

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
				routingTemplate = new RoundTripRoutingTemplate(request, ghRsp, getLocationIndex(), getMaxRoundTripRetries());
			else if (ALT_ROUTE.equalsIgnoreCase(algoStr))
				routingTemplate = new AlternativeRoutingTemplate(request, ghRsp, getLocationIndex());
			else
				routingTemplate = new ViaRoutingTemplate(request, ghRsp, getLocationIndex());

			List<Path> altPaths = null;
			int maxRetries = routingTemplate.getMaxRetries();
			Locale locale = request.getLocale();
			Translation tr = getTranslationMap().getWithFallBack(locale);
			for (int i = 0; i < maxRetries; i++) {
				StopWatch sw = new StopWatch().start();
				List<QueryResult> qResults = routingTemplate.lookup(points, request.getMaxSearchDistances(), encoder);
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

					RoutingAlgorithmFactory coreAlgoFactory = coreFactoryDecorator.getDecoratedAlgorithmFactory(new RoutingAlgorithmFactorySimple(), hints);
					weighting = ((PrepareCore) coreAlgoFactory).getWeighting();
					tMode = getCoreFactoryDecorator().getNodeBase();
					queryGraph = new QueryGraph(ghStorage.getGraph(CHGraph.class, weighting));
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

						tMode = getCHFactoryDecorator().getNodeBase();
						queryGraph = new QueryGraph(ghStorage.getGraph(CHGraph.class, weighting));
						queryGraph.lookup(qResults);


					} else {

						checkNonChMaxWaypointDistance(points);
						queryGraph = new QueryGraph(ghStorage);
						queryGraph.lookup(qResults);
						weighting = createWeighting(hints, tMode, encoder, queryGraph, ghStorage);
						ghRsp.addDebugInfo("tmode:" + tMode.toString());
					}
				}

				int maxVisitedNodesForRequest = hints.getInt(Parameters.Routing.MAX_VISITED_NODES, getMaxVisitedNodes());
				if (maxVisitedNodesForRequest > getMaxVisitedNodes())
					throw new IllegalArgumentException(
							"The max_visited_nodes parameter has to be below or equal to:" + getMaxVisitedNodes());

				weighting = createTurnWeighting(queryGraph, weighting, tMode);

				AlgorithmOptions algoOpts = AlgorithmOptions.start().algorithm(algoStr).traversalMode(tMode)
						.weighting(weighting).maxVisitedNodes(maxVisitedNodesForRequest).hints(hints).build();

				if (request.getEdgeFilter() != null)
					algoOpts.setEdgeFilter(request.getEdgeFilter());

//				PathProcessingContext pathProcCntx = new PathProcessingContext(encoder, weighting, tr,
//						request.getEdgeAnnotator(), request.getPathProcessor(), byteBuffer);

				altPaths = routingTemplate.calcPaths(queryGraph, tmpAlgoFactory, algoOpts);

				boolean tmpEnableInstructions = hints.getBool(Parameters.Routing.INSTRUCTIONS, enableInstructions);
				boolean tmpCalcPoints = hints.getBool(Parameters.Routing.CALC_POINTS, isCalcPoints());
				double wayPointMaxDistance = hints.getDouble(Parameters.Routing.WAY_POINT_MAX_DISTANCE, 1d);
				DouglasPeucker peucker = new DouglasPeucker().setMaxDistance(wayPointMaxDistance);
				PathMerger pathMerger = new PathMerger().setCalcPoints(tmpCalcPoints).setDouglasPeucker(peucker)
						.setEnableInstructions(tmpEnableInstructions)
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

	public RouteSegmentInfo getRouteSegment(double[] latitudes, double[] longitudes, String vehicle,
											EdgeFilter edgeFilter) {
		RouteSegmentInfo result = null;

		GHRequest req = new GHRequest();
		for (int i = 0; i < latitudes.length; i++)
			req.addPoint(new GHPoint(latitudes[i], longitudes[i]));

		req.setVehicle(vehicle);
		req.setAlgorithm("dijkstrabi");
		req.setWeighting("fastest");
		// TODO add limit of maximum visited nodes

		if (edgeFilter != null)
			req.setEdgeFilter(edgeFilter);

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
					//	fullEdges.add(edge.getEdge());
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

				//throw new Exception("TODO");
				result = new RouteSegmentInfo(fullEdges, distance, time, new GeometryFactory().createLineString(coords));
			}
		}

		return result;
	}

	private int _minNetworkSize = 200;
	private int _minOneWayNetworkSize = 0;
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
			int ex = ghs.getAllEdges().getMaxId();
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

		if (encodingManager.supports("heavyvehicle")) {
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
			coreFactoryDecorator.createPreparations(gs, traversalMode, coreEdgeFilter);
		if (!isCorePrepared())
			prepareCore();


		coreFactoryDecorator.initEdgeFilter();

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
		if (!coreFactoryDecorator.hasWeightings()) {
			for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
				for (String coreWeightingStr : coreFactoryDecorator.getWeightingsAsStrings()) {
					// ghStorage is null at this point
					Weighting weighting = createWeighting(new HintsMap(coreWeightingStr), traversalMode, encoder, null);
					coreFactoryDecorator.addWeighting(weighting);
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

			ghStorage.freeze();
			coreFactoryDecorator.prepare(ghStorage.getProperties());
			ghStorage.getProperties().put(Core.PREPARE + "done", true);
		}
	}

	private boolean isCorePrepared() {
		return "true".equals(ghStorage.getProperties().get(Core.PREPARE + "done"))
				// remove old property in >0.9
				|| "true".equals(ghStorage.getProperties().get("prepare.done"));
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
				for (String coreWeightingStr : coreFactoryDecorator.getWeightingsAsStrings()) {
					// ghStorage is null at this point
					Weighting weighting = createWeighting(new HintsMap(coreWeightingStr), traversalMode, encoder, null);
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
			ghStorage.freeze();
			if (coreLMFactoryDecorator.loadOrDoWork(ghStorage.getProperties()))
				ghStorage.getProperties().put(ORSParameters.CoreLandmark.PREPARE + "done", true);
		}
	}
}