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
package org.heigit.ors.isochrones.builders.fast;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.HikeFlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import org.apache.log4j.Logger;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.fastisochrones.BaseGraphFilter;
import org.heigit.ors.fastisochrones.FastIsochroneAlgorithm;
import org.heigit.ors.isochrones.Isochrone;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.isochrones.builders.IsochroneMapBuilder;
import org.heigit.ors.isochrones.builders.concaveballs.PointItemVisitor;
import org.heigit.ors.partitioning.BorderNodeDistanceStorage;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.EccentricityStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSEdgeFilterFactory;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidFeaturesEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.CHEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FootFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.ORSAbstractFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import org.heigit.ors.util.GeomUtility;
import org.opensphere.geometry.algorithm.ConcaveHull;

import java.util.*;

import static org.heigit.ors.partitioning.FastIsochroneParameters.CONTOUR__USE_SUPERCELLS;
import static org.heigit.ors.partitioning.FastIsochroneParameters.PART__DEBUG;
import static org.heigit.ors.partitioning.FastIsochroneParameters.PART__MIN_CELL_NODES_NUMBER;

public class FastIsochroneMapBuilder implements IsochroneMapBuilder
{
	private final Logger LOGGER = Logger.getLogger(FastIsochroneMapBuilder.class.getName());

	private Envelope searchEnv = new Envelope();
	private GeometryFactory _geomFactory;
	private PointItemVisitor visitor = null;
	private TreeSet<Coordinate> _treeSet = new TreeSet<>();
	private RouteSearchContext _searchContext;
	private Graph graph;
	private CellStorage cellStorage;
	private IsochroneNodeStorage isochroneNodeStorage;
	private EccentricityStorage eccentricityStorage;
	private BorderNodeDistanceStorage borderNodeDistanceStorage;

	private double searchWidth = 0.0007;
	private double pointWidth = 0.0005;
	private double visitorThreshold = 0.0013;

	private int minEdgeLengthLimit = 400;
	private int maxEdgeLengthLimit = Integer.MAX_VALUE;

	private boolean BUFFERED_OUTPUT = true;

	public void initialize(RouteSearchContext searchContext) {
		_geomFactory = new GeometryFactory();
		_searchContext = searchContext;
		cellStorage = ((ORSGraphHopper) _searchContext.getGraphHopper()).getPartitioningFactoryDecorator().getCellStorage();
		isochroneNodeStorage = ((ORSGraphHopper) _searchContext.getGraphHopper()).getPartitioningFactoryDecorator().getIsochroneNodeStorage();
	}

	public IsochroneMap compute(IsochroneSearchParameters parameters) throws Exception {
		StopWatch swTotal = null;
		StopWatch sw = null;
		if (LOGGER.isDebugEnabled())
		{
			swTotal = new StopWatch();
			swTotal.start();
			sw = new StopWatch();
			sw.start();
		}

		// 1. Find all graph edges for a given cost.
		double maxSpeed = _searchContext.getEncoder().getMaxSpeed();

		if (_searchContext.getEncoder() instanceof FootFlagEncoder || _searchContext.getEncoder() instanceof HikeFlagEncoder) {
			// in the GH FootFlagEncoder, the maximum speed is set to 15km/h which is way too high
			maxSpeed = 4;
		}

		if (_searchContext.getEncoder() instanceof WheelchairFlagEncoder) {
			maxSpeed = WheelchairFlagEncoder.MEAN_SPEED;
		}

		double meanSpeed = maxSpeed;
        if (_searchContext.getEncoder() instanceof ORSAbstractFlagEncoder) {
            meanSpeed = ((ORSAbstractFlagEncoder) _searchContext.getEncoder()).getMeanSpeed();
        }

		double metersPerSecond = maxSpeed / 3.6;
		// only needed for reachfactor property
		double meanMetersPerSecond = meanSpeed / 3.6;

		Weighting weighting = null;

		if (parameters.getRangeType() == TravelRangeType.TIME)
		{
			weighting = new FastestWeighting(_searchContext.getEncoder());
//			weighting = ((ORSGraphHopper) _searchContext.getGraphHopper())
//					.getIsochroneCoreFactoryDecorator()
//					.getDecoratedAlgorithmFactory(new FastestWeighting(_searchContext.getEncoder()))
//					.getWeighting();
		}
		else
		{
			weighting = new ShortestWeighting(_searchContext.getEncoder());
//			weighting = ((ORSGraphHopper) _searchContext.getGraphHopper())
//					.getIsochroneCoreFactoryDecorator()
//					.getDecoratedAlgorithmFactory(new ShortestWeighting(_searchContext.getEncoder()))
//					.getWeighting();
		}

		eccentricityStorage = ((ORSGraphHopper) _searchContext.getGraphHopper()).getEccentricity().getEccentricityStorage(weighting);
		borderNodeDistanceStorage = ((ORSGraphHopper) _searchContext.getGraphHopper()).getEccentricity().getBorderNodeDistanceStorage(weighting);


		Coordinate loc = parameters.getLocation();
		ORSEdgeFilterFactory edgeFilterFactory = new ORSEdgeFilterFactory();
		EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
		EdgeFilter edgeFilter = edgeFilterFactory.createEdgeFilter(_searchContext.getProperties(), _searchContext.getEncoder(), _searchContext.getGraphHopper().getGraphHopperStorage());
		edgeFilterSequence.add(edgeFilter);
		edgeFilterSequence.add(new CHEdgeFilter(new AvoidFeaturesEdgeFilter(AvoidFeatureFlags.FERRIES, _searchContext.getGraphHopper().getGraphHopperStorage()), true));
		QueryResult res = _searchContext.getGraphHopper().getLocationIndex().findClosest(loc.y, loc.x, edgeFilterSequence);
		//Needed to get the cell of the start point (preprocessed information, so no info on virtual nodes)
		int nonvirtualClosestNode = res.getClosestNode();
		this.graph = _searchContext.getGraphHopper().getGraphHopperStorage().getIsochroneGraph(weighting);
//		QueryGraph queryGraph = new QueryGraph(_searchContext.getGraphHopper().getGraphHopperStorage().getIsochroneGraph(weighting));
//		graph.lookup(res);
//		this.graph = queryGraph;

        //This calculates the nodes that are within the limit
		//Currently only support for Node based
		if(!(_searchContext.getGraphHopper() instanceof ORSGraphHopper))
			throw new IllegalStateException("Unable to run fast isochrones without ORSGraphhopper");

		int nRanges = parameters.getRanges().length;
		double prevCost = 0;
		IsochroneMap isochroneMap = null;

		for (int i = 0; i < nRanges; i++) {
			FastIsochroneAlgorithm fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
					graph,
					new PreparationWeighting(weighting),
					TraversalMode.NODE_BASED,
					cellStorage,
					isochroneNodeStorage,
					eccentricityStorage,
					borderNodeDistanceStorage,
					edgeFilterSequence);
			fastIsochroneAlgorithm.setOriginalFrom(nonvirtualClosestNode);
			fastIsochroneAlgorithm.calcIsochroneNodes(nonvirtualClosestNode, parameters.getRanges()[i]);

			Set<Geometry> isochroneGeometries = new HashSet<>();

			//Add all fully reachable cell geometries
			handleFullyReachableCells(isochroneGeometries, fastIsochroneAlgorithm.getFullyReachableCells());

			GHPoint3D snappedPosition = res.getSnappedPoint();

			AccessibilityMap edgeMap = new AccessibilityMap(fastIsochroneAlgorithm.getBestWeightMap(), snappedPosition);

			final Coordinate snappedLoc = (snappedPosition == null) ? parameters.getLocation() : new Coordinate(snappedPosition.lon, snappedPosition.lat);

			if(isochroneMap == null) isochroneMap = new IsochroneMap(parameters.getTravellerId(), snappedLoc);

			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Find edges: " + sw.stop().getSeconds());
			}

			if (edgeMap.isEmpty())
				return isochroneMap;

			_treeSet = new TreeSet();

			List<Coordinate> isoPoints = new ArrayList((int)(1.2*edgeMap.getMap().size()));

			double isoValue = parameters.getRanges()[i];
			double isochronesDifference = parameters.getRanges()[i];
			if (i > 0)
				isochronesDifference = parameters.getRanges()[i] - parameters.getRanges()[i-1];

			float smoothingFactor = parameters.getSmoothingFactor();
			TravelRangeType isochroneType = parameters.getRangeType();

			final double maxRadius;
			double meanRadius = 0;
			switch (isochroneType) {
				case DISTANCE:
					maxRadius = isoValue;
					meanRadius = isoValue;
					break;
				case TIME:
					maxRadius = metersPerSecond * isoValue;
					meanRadius = meanMetersPerSecond * isoValue;
					isochronesDifference = metersPerSecond * isochronesDifference;
					break;
				default:
					maxRadius = isoValue;
					meanRadius = isoValue;
					break;
			}

			//Build concave hulls of all active cells individually
			StopWatch swActiveCell = new StopWatch();
			StopWatch swActiveCellSeparate = new StopWatch();
			StopWatch swActiveCellBuild = new StopWatch();


			if (LOGGER.isDebugEnabled()) {
				swActiveCell.start();
			}

			for(Map.Entry<Integer, IntObjectMap<SPTEntry>> activeCell : fastIsochroneAlgorithm.getActiveCellMaps().entrySet()) {
					swActiveCellSeparate.start();
					//Find disconnected sub-cells of active cells to avoid geometric problems
					Set<GHIntObjectHashMap<SPTEntry>> disconnectedActiveCells = separateDisconnected(activeCell.getValue());
					swActiveCellSeparate.stop();
					swActiveCellBuild.start();
					for (GHIntObjectHashMap<SPTEntry> splitMap : disconnectedActiveCells) {
						if(splitMap.size() < PART__MIN_CELL_NODES_NUMBER)
							continue;
						GeometryCollection points = buildIsochrone(new AccessibilityMap(splitMap, snappedPosition), new ArrayList<>(), new ArrayList<Coordinate>(), snappedLoc.x, snappedLoc.y, isoValue, prevCost, isochronesDifference, 0.85);
						createPolyFromPoints(isochroneGeometries, points, maxRadius, smoothingFactor);
					}
					swActiveCellBuild.stop();

			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Separate disconnected: " + swActiveCellSeparate.stop().getSeconds());
				LOGGER.debug("Build active cells: " + swActiveCellBuild.stop().getSeconds());
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Build all active cell concave hull: " + swActiveCell.stop().getSeconds());
				swActiveCell = new StopWatch();
				swActiveCell.start();
			}

			//Make a union of all now existing polygons to reduce coordinate list
			Geometry preprocessedGeometry = UnaryUnionOp.union(isochroneGeometries);

			//Build the final isochrone from the outlines of the remaining unified polygons
			List<Double> contourCoordinates = new ArrayList<>();
			for(int j = 0; j < preprocessedGeometry.getNumGeometries(); j++) {
				if(!(preprocessedGeometry.getGeometryN(j) instanceof Polygon)){
					for(Coordinate coordinate : preprocessedGeometry.getGeometryN(j).getCoordinates()){
						contourCoordinates.add(coordinate.y);
						contourCoordinates.add(coordinate.x);
					}
					continue;
				}

				for(Coordinate coordinate : ((Polygon)preprocessedGeometry.getGeometryN(j)).getExteriorRing().getCoordinates()){
					contourCoordinates.add(coordinate.y);
					contourCoordinates.add(coordinate.x);
				}
			}
			GeometryCollection points = buildIsochrone(new AccessibilityMap(new GHIntObjectHashMap<>(0), snappedPosition), contourCoordinates, isoPoints, loc.x, loc.y, isoValue, prevCost, isochronesDifference, 1);
			addIsochrone(isochroneMap, points, isoValue, maxRadius, meanRadius, smoothingFactor);
			prevCost = isoValue;

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Build final concave hull: " + swActiveCell.stop().getSeconds());
			}
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Total time: " + swTotal.stop().getSeconds());

		return isochroneMap;
	}

	/**
	 * Converts the smoothing factor into a distance (which can be used in algorithms for generating isochrone polygons).
	 * The distance value returned is dependent on the radius and smoothing factor.
	 *
	 * @param smoothingFactor	A factor that should be used in the smoothing process. Lower numbers produce a smaller
	 *                          distance (and so likely a more detailed polygon)
	 * @param maxRadius			The maximum radius of the isochrone (in metres)
	 * @return
	 */
	private double convertSmoothingFactorToDistance(float smoothingFactor, double maxRadius)
	{
		double MINIMUM_DISTANCE = 0.006;

		if (smoothingFactor == -1) {
			// No user defined smoothing factor, so use defaults

			// For shorter isochrones, we want to use a smaller minimum distance else we get inaccurate results
			if (maxRadius < 5000)
				return MINIMUM_DISTANCE;

			// Use a default length (~1000m)
			return 0.010;
		}

		double intervalDegrees = GeomUtility.metresToDegrees(maxRadius);
		double maxLength = (intervalDegrees / 100f) * smoothingFactor;

		if (maxLength < MINIMUM_DISTANCE)
			maxLength = MINIMUM_DISTANCE;
		return maxLength;
	}

	private void createPolyFromPoints(Set<Geometry> activeCellHulls, GeometryCollection points, double maxRadius, float smoothingFactor)
	{
		if (points.isEmpty())
			return;
		LinearRing ring = null;
		Geometry concaveHull;
		try {
			ConcaveHull ch = new ConcaveHull(points, convertSmoothingFactorToDistance(smoothingFactor, maxRadius), false);
			concaveHull = ch.getConcaveHull();
			if (concaveHull instanceof Polygon) {
				ring = (LinearRing) ((Polygon)concaveHull).getExteriorRing();
				List<Coordinate> coordinates = new ArrayList<Coordinate>(ring.getNumPoints());
				for (int i = 0; i < ring.getNumPoints(); i++) {
					coordinates.add(ring.getCoordinateN(i));
					if (i < ring.getNumPoints() - 1) {
						splitEdge(ring.getPointN(i).getY(),
								ring.getPointN(i + 1).getY(),
								ring.getPointN(i).getX(),
								ring.getPointN(i + 1).getX(),
								coordinates,
								minEdgeLengthLimit,
								maxEdgeLengthLimit);
					}
				}
				coordinates.add(ring.getCoordinateN(0));
				concaveHull = _geomFactory.createPolygon(coordinates.toArray(new Coordinate[0]));
				activeCellHulls.add(concaveHull);
			}
			else
				activeCellHulls.add(concaveHull);
		}
		catch (Exception e){
			if(PART__DEBUG) LOGGER.debug(e.getMessage());
			return;
		}
	}

	private void addIsochrone(IsochroneMap isochroneMap, GeometryCollection points, double isoValue, double maxRadius, double meanRadius, float smoothingFactor)
	{
		if (points.isEmpty())
			return;
		Polygon poly = null;
		try {
			ConcaveHull ch = new ConcaveHull(points, convertSmoothingFactorToDistance(smoothingFactor, maxRadius), false);
			Geometry geom = ch.getConcaveHull();

			if (geom instanceof GeometryCollection) {
				GeometryCollection geomColl = (GeometryCollection) geom;
				if (geomColl.isEmpty())
					return;
			}

			poly = (Polygon) geom;
		}
		catch (Exception e){
			return;
		}
		isochroneMap.addIsochrone(new Isochrone(poly, isoValue, meanRadius));
	}

	public Boolean addPoint(List<Coordinate> points, Quadtree tree, double lon, double lat, boolean checkNeighbours) {
		if (checkNeighbours)
		{
			visitor.setPoint(lon, lat);
			searchEnv.init(lon - searchWidth, lon + searchWidth, lat - searchWidth, lat + searchWidth);
			tree.query(searchEnv, visitor);
			if (!visitor.isNeighbourFound())
			{
				Coordinate p = new Coordinate(lon, lat);

				if (!_treeSet.contains(p))
				{
					Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
					tree.insert(env, p);
					points.add(p);
					_treeSet.add(p);

					return true;
				}
			}
		}
		else
		{
			Coordinate p = new Coordinate(lon, lat);
			if (!_treeSet.contains(p))
			{
				Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
				tree.insert(env, p);
				points.add(p);
				_treeSet.add(p);

				return true;
			}
		}

		return false;
	}

	public Boolean addPoint(List<Coordinate> points, Quadtree tree, TreeSet _treeSet, double lon, double lat, double searchWidth, double pointWidth, boolean checkNeighbours) {
		if (checkNeighbours)
		{
			visitor.setPoint(lon, lat);
			searchEnv.init(lon - searchWidth, lon + searchWidth, lat - searchWidth, lat + searchWidth);
			tree.query(searchEnv, visitor);
			if (!visitor.isNeighbourFound())
			{
				Coordinate p = new Coordinate(lon, lat);

				if (!_treeSet.contains(p))
				{
					Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
					tree.insert(env, p);
					points.add(p);
					_treeSet.add(p);

					return true;
				}
			}
		}
		else
		{
			Coordinate p = new Coordinate(lon, lat);
			if (!_treeSet.contains(p))
			{
				Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
				tree.insert(env, p);
				points.add(p);
				_treeSet.add(p);

				return true;
			}
		}

		return false;
	}

	private void addBufferPoints(List<Coordinate> points, Quadtree tree, double lon0, double lat0, double lon1,
			double lat1, boolean addLast, boolean checkNeighbours, double bufferSize) {
		double dx = (lon0 - lon1);
		double dy = (lat0 - lat1);
		double norm_length = Math.sqrt((dx * dx) + (dy * dy));
		double scale = bufferSize /norm_length;

		double dx2 = -dy*scale;
		double dy2 = dx*scale;

		addPoint(points, tree, lon0 + dx2, lat0 + dy2, checkNeighbours);
		addPoint(points, tree, lon0 - dx2, lat0 - dy2, checkNeighbours);

		// add a middle point if two points are too far from each other
		if (norm_length > 2*bufferSize)
		{
			addPoint(points, tree, (lon0 + lon1)/2.0 + dx2, (lat0 + lat1)/2.0 + dy2, checkNeighbours);
			addPoint(points, tree, (lon0 + lon1)/2.0 - dx2, (lat0 + lat1)/2.0 - dy2, checkNeighbours);
		}

		if (addLast) {
			 addPoint(points, tree, lon1 + dx2, lat1 + dy2, checkNeighbours);
			 addPoint(points, tree, lon1 - dx2, lat1 - dy2, checkNeighbours);
		}
	}

	private GeometryCollection buildIsochrone(AccessibilityMap edgeMap, List<Double> contourCoordinates, List<Coordinate> points, double lon, double lat,
			double isolineCost, double prevCost, double isochronesDifference, double detailedGeomFactor) {
		IntObjectMap<SPTEntry> map = edgeMap.getMap();
//		points.clear();
		_treeSet.clear();

		GraphHopperStorage graph = _searchContext.getGraphHopper().getGraphHopperStorage();
		NodeAccess nodeAccess = graph.getNodeAccess();
		Quadtree qtree = new Quadtree();

		int maxNodeId = graph.getNodes();


		SPTEntry goalEdge;

		DistanceCalc dcFast = new DistancePlaneProjection();
		double bufferSize = 0.0018;
		visitor = new PointItemVisitor(lon, lat, visitorThreshold);
		double detailedZone = isolineCost * detailedGeomFactor;

		double defaultSearchWidth = 0.0008;
		double defaultPointWidth =  0.005;
		double defaultVisitorThreshold = 0.0035;

		// make results a bit more precise for regions with low data density
		if (map.size() < 10000)
		{
			defaultSearchWidth = 0.0008;
			defaultPointWidth = 0.005;
			defaultVisitorThreshold = 0.0025;
		}

		boolean useHighDetail = map.size() < 1000 || isochronesDifference < 1000;

//		if (useHighDetail) {
//			bufferSize = 0.00018;
//			defaultVisitorThreshold = 0.000005;
//		}

		int nodeId, edgeId;

		searchWidth = defaultSearchWidth;
		visitorThreshold = defaultVisitorThreshold;
		pointWidth = defaultPointWidth;

		visitor.setThreshold(visitorThreshold);

		for (IntObjectCursor<SPTEntry> entry : map) {
			goalEdge = entry.value;
			edgeId = goalEdge.originalEdge;

			if (edgeId == -1)
				continue;

			nodeId = goalEdge.adjNode;

			if (nodeId == -1 || nodeId > maxNodeId)
				continue;

			EdgeIteratorState iter = this.graph.getEdgeIteratorState(edgeId, nodeId);
			if(((CHEdgeIteratorState) iter).isShortcut())
				continue;

			double maxCost = (goalEdge.weight);
			double minCost = (goalEdge.parent.weight);

			// ignore all edges that have been considered in the previous step. We do not want to do this for small
			// isochrones as the edge may have more than one range on it in that case
			if (minCost < prevCost && isochronesDifference > 1000)
				continue;

			// edges that are fully inside of the isochrone
			if (isolineCost >= maxCost) {
				// This checks for dead end edges, but we need to include those in small areas to provide realistic
				// results
				if (goalEdge.edge == -2 && !useHighDetail)
				{
					//addPoint(points, qtree, nodeAccess.getLon(nodeId), nodeAccess.getLat(nodeId), true);
				}
				else
				{
					double edgeDist = iter.getDistance();
					if (((maxCost >= detailedZone) || edgeDist > 300))
					{
						boolean detailedShape = (edgeDist > 300);
						// always use mode=3, since other ones do not provide correct results
						PointList pl = iter.fetchWayGeometry(3);
						int size = pl.getSize();
						if (size > 0) {
							double lat0 = pl.getLat(0);
							double lon0 = pl.getLon(0);
							double lat1, lon1;

							if (detailedShape && BUFFERED_OUTPUT)
							{
								for (int i = 1; i < size; ++i) {
									lat1 = pl.getLat(i);
									lon1 = pl.getLon(i);

									addBufferPoints(points, qtree, lon0, lat0, lon1, lat1, goalEdge.edge < 0 && i == size - 1, true, bufferSize);

									lon0 = lon1;
									lat0 = lat1;
								}
							}
							else
							{
								for (int i = 1; i < size; ++i) {
									lat1 = pl.getLat(i);
									lon1 = pl.getLon(i);

									addPoint(points, qtree, lon0, lat0, true);
									if (i == size -1)
										addPoint(points, qtree, lon1, lat1, true);

									lon0 = lon1;
									lat0 = lat1;
								}
							}
						}
					} else {
						addPoint(points, qtree, nodeAccess.getLon(nodeId), nodeAccess.getLat(nodeId), true);
					}
				}
			} else {
				if ((minCost < isolineCost && maxCost >= isolineCost))
				{

					PointList pl = iter.fetchWayGeometry(3);

					int size = pl.getSize();
					if (size > 0) {
						double edgeCost = maxCost - minCost;
						double edgeDist = iter.getDistance();
						double costPerMeter = edgeCost / edgeDist;
						double distPolyline = 0.0;

						double lat0 = pl.getLat(0);
						double lon0 = pl.getLon(0);
						double lat1, lon1;

						for (int i = 1; i < size; ++i) {
							lat1 = pl.getLat(i);
							lon1 = pl.getLon(i);

							distPolyline += dcFast.calcDist(lat0, lon0, lat1, lon1);

							if (BUFFERED_OUTPUT)
							{
								double distCost = minCost + distPolyline * costPerMeter;
								if (distCost >= isolineCost) {
									double segLength = (1 - (distCost - isolineCost) / edgeCost);
									double lon2 = lon0 + segLength * (lon1 - lon0);
									double lat2 = lat0 + segLength * (lat1 - lat0);

									addBufferPoints(points, qtree, lon0, lat0, lon2, lat2, true, false, bufferSize);

									break;
								} else {
									addBufferPoints(points, qtree, lon0, lat0, lon1, lat1, false, true, bufferSize);
								}
							}
							else
							{
								addPoint(points, qtree, lon0, lat0, true);
							}

							lat0 = lat1;
							lon0 = lon1;
						}
					}
				}
			}
		}
		int mapPointCount = points.size();

		int j = 0;
		while (j < contourCoordinates.size()){
			double latitude = contourCoordinates.get(j);
			j++;
			double longitude = contourCoordinates.get(j);
			j++;
			addPoint(points, qtree, longitude, latitude, true);
		}
		if(PART__DEBUG) System.out.println("# of points in map: " + map.size() + ", #p from map " + mapPointCount + ", #p from contours " + (points.size() - mapPointCount));

		Geometry[] geometries = new Geometry[points.size()];

		for (int i = 0;i < points.size();++i)
		{
			Coordinate c = points.get(i);
			geometries[i] = _geomFactory.createPoint(c);
		}

		return new GeometryCollection(geometries, _geomFactory);
	}

	private void handleFullyReachableCells(Set<Geometry> isochroneGeometries, Set<Integer> fullyReachableCells){
		//printing for debug
		if(PART__DEBUG) System.out.println("{" +
				"  \"type\": \"FeatureCollection\"," +
				"  \"features\": [");
		Set<Integer> reachableCellsAndSuperCells = new HashSet<>();
		if(CONTOUR__USE_SUPERCELLS) {
			Set<Integer> reachableSuperCells = new HashSet<>();
			for (int cellId : fullyReachableCells) {
				int superCell = cellStorage.getSuperCellOfCell(cellId);
				if (superCell != -1 && fullyReachableCells.containsAll(Arrays.asList(cellStorage.getCellsOfSuperCell(superCell))))
					reachableSuperCells.add(superCell);
				else {
					reachableCellsAndSuperCells.add(cellId);
				}
			}
			for (int cellId : reachableSuperCells) {
				int superCell = cellStorage.getSuperCellOfCell(cellId);
				if (superCell != -1 && reachableSuperCells.containsAll(Arrays.asList(cellStorage.getCellsOfSuperCell(superCell)))) {
					reachableCellsAndSuperCells.add(superCell);
				} else {
					reachableCellsAndSuperCells.add(cellId);
				}
			}
		}

		if(!CONTOUR__USE_SUPERCELLS)
			reachableCellsAndSuperCells = fullyReachableCells;

		for (int cellId : reachableCellsAndSuperCells) {
			addCellPolygon(cellId, isochroneGeometries);
		}
		if(PART__DEBUG) System.out.println("]}");
	}

	private void addCellPolygon(int cellId, Set<Geometry> isochronePolygons){
		List<Double> coordinates = cellStorage.getCellContourOrder(cellId);
		Coordinate[] cArray = new Coordinate[(int)Math.ceil(coordinates.size() / 2)];

		for(int n = cArray.length - 1; n >= 0 ; n--){
			cArray[cArray.length - 1 - n] = new Coordinate(coordinates.get(2 * n + 1).floatValue(), coordinates.get(2 * n).floatValue());
		}
		Polygon polygon = _geomFactory.createPolygon(cArray);
		if(polygon.isValid()) {
			isochronePolygons.add(polygon);
		}
		else
			System.out.println("Poly of cell " + cellId + " is invalid at size " + cArray.length);
		if(PART__DEBUG)printCell(cellStorage.getCellContourOrder(cellId), cellId);
	}

	//DEBUG
	private void printCell(List coordinates, int cellId){
		if(coordinates.size() < 3)
			return;
		System.out.print("{\"type\": \"Feature\",\"properties\": {\"name\": \"" + cellId + "\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[");
		int i;
		for (i = coordinates.size() - 2; i > 0; i-=2){
			System.out.print("[" + String.valueOf(coordinates.get(i + 1)).substring(0, Math.min(8, String.valueOf(coordinates.get(i + 1)).length())) + "," + String.valueOf(coordinates.get(i)).substring(0,Math.min(8, String.valueOf(coordinates.get(i)).length())) + "],");
		}
		System.out.print("[" + String.valueOf(coordinates.get(coordinates.size() - 1)).substring(0,Math.min(8, String.valueOf(coordinates.get(coordinates.size() - 1)).length())) + "," + String.valueOf(coordinates.get(coordinates.size() - 2)).substring(0,Math.min(8, String.valueOf(coordinates.get(coordinates.size() - 2)).length())) + "]");

		System.out.println("]]}},");
	}

	//DEBUG
	private void printNodes(IntHashSet nodes, int cellId){
		System.out.print("{\"type\": \"MultiPoint\",\"coordinates\": [");
		int i;
		int[] intNodes = nodes.toArray();
		for (i = 0; i < intNodes.length; i++){
			System.out.print("[" + String.valueOf(_searchContext.getGraphHopper().getGraphHopperStorage().getNodeAccess().getLon(intNodes[i])).substring(0, Math.min(8, String.valueOf(_searchContext.getGraphHopper().getGraphHopperStorage().getNodeAccess().getLon(intNodes[i])).length()))
					+ "," + String.valueOf(_searchContext.getGraphHopper().getGraphHopperStorage().getNodeAccess().getLat(intNodes[i])).substring(0,Math.min(8, String.valueOf(_searchContext.getGraphHopper().getGraphHopperStorage().getNodeAccess().getLat(intNodes[i])).length())) + "],");
		}
		System.out.println("]]}");
	}

	/*
		Calculates the distance between two coordinates in meters
 	*/
	public static double distance(double lat1, double lat2, double lon1,
								  double lon2) {
		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		distance = Math.pow(distance, 2);

		return Math.sqrt(distance);
	}


	private void splitEdge(double lat0, double lat1, double lon0, double lon1, List<Coordinate> coordinates, double minlim, double maxlim){
		double dist = distance(lat0, lat1, lon0, lon1);

		if(dist > minlim && dist < maxlim){
			int n = (int) Math.ceil(dist / minlim);
			for (int i = 1; i < n; i++){
				coordinates.add(new Coordinate((lon0 + i * (lon1 - lon0) / n), (lat0 + i * (lat1 - lat0) / n) ));
			}
		}
	}
	private Set<GHIntObjectHashMap<SPTEntry>> separateDisconnected(IntObjectMap<SPTEntry> map){
		Set<GHIntObjectHashMap<SPTEntry>> disconnectedCells = new HashSet<>();
		EdgeExplorer edgeExplorer = graph.createEdgeExplorer(new BaseGraphFilter());
		Queue<Integer> queue = new ArrayDeque<>();
		IntHashSet visitedNodes = new IntHashSet(map.size());
		for(IntObjectCursor<SPTEntry> entry : map) {
			if(visitedNodes.contains(entry.key))
				continue;
			visitedNodes.add(entry.key);
			queue.offer(entry.key);

			GHIntObjectHashMap<SPTEntry> connectedCell = new GHIntObjectHashMap();

			while (!queue.isEmpty()) {
				int currentNode = queue.poll();
				connectedCell.put(currentNode, map.get(currentNode));
				EdgeIterator edgeIterator = edgeExplorer.setBaseNode(currentNode);

				while (edgeIterator.next()) {
					int nextNode = edgeIterator.getAdjNode();
					if (visitedNodes.contains(nextNode) || !map.containsKey(nextNode))
						continue;
					queue.offer(nextNode);
					connectedCell.put(nextNode, map.get(nextNode));
					visitedNodes.add(nextNode);
				}
			}
			disconnectedCells.add(connectedCell);
		}
		return disconnectedCells;
	}

}
