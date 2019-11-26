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

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.HikeFlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import heigit.ors.common.TravelRangeType;
import org.heigit.ors.fastisochrones.FastIsochroneAlgorithm;
import heigit.ors.isochrones.GraphEdgeMapFinder;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.builders.AbstractIsochroneMapBuilder;
import heigit.ors.isochrones.builders.concaveballs.PointItemVisitor;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.EccentricityStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;
import heigit.ors.routing.RouteSearchContext;
import heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FootFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.ORSAbstractFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.weighting.DistanceWeighting;
import heigit.ors.util.GeomUtility;
import org.apache.log4j.Logger;
import org.opensphere.geometry.algorithm.ConcaveHull;

import java.util.*;

public class FastIsochroneMapBuilder extends AbstractIsochroneMapBuilder
{
	private final Logger LOGGER = Logger.getLogger(FastIsochroneMapBuilder.class.getName());

	private Envelope searchEnv = new Envelope();
	private GeometryFactory _geomFactory;
	private PointItemVisitor visitor = null;
	private List<Coordinate> prevIsoPoints = null;
    private TreeSet<Coordinate>_treeSet;
	private RouteSearchContext _searchContext;
	private QueryGraph queryGraph;

	private double searchWidth = 0.0007;
	private double pointWidth = 0.0005;
	private double visitorThreshold = 0.0013;

	private boolean BUFFERED_OUTPUT = true;

	public void initialize(RouteSearchContext searchContext) {
		_geomFactory = new GeometryFactory();
		_searchContext = searchContext;		
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

		Weighting weighting = null;

		if (parameters.getRangeType() == TravelRangeType.Time)
		{
			weighting = ((ORSGraphHopper) _searchContext.getGraphHopper())
					.getIsochroneCoreFactoryDecorator()
					.getDecoratedAlgorithmFactory(new FastestWeighting(_searchContext.getEncoder()))
					.getWeighting();
		}
		else
		{
			weighting = ((ORSGraphHopper) _searchContext.getGraphHopper())
					.getIsochroneCoreFactoryDecorator()
					.getDecoratedAlgorithmFactory(new ShortestWeighting(_searchContext.getEncoder()))
					.getWeighting();
		}

		Coordinate loc = parameters.getLocation();
		List<QueryResult> res = Arrays.asList(_searchContext.getGraphHopper().getLocationIndex().findClosest(loc.y, loc.x, _searchContext.getEdgeFilter()));
		//Needed to get the cell of the start point (preprocessed information, so no info on virtual nodes)
		int nonvirtualClosestNode = res.get(0).getClosestNode();

		QueryGraph queryGraph = new QueryGraph(_searchContext.getGraphHopper().getGraphHopperStorage().getIsochroneGraph(weighting));
		queryGraph.lookup(res);
		this.queryGraph = queryGraph;

        //This calculates the nodes that are within the limit
		//Currently only support for Node based
		if(!(_searchContext.getGraphHopper() instanceof ORSGraphHopper))
			throw new IllegalStateException("Unable to run fast isochrones without ORSGraphhopper");

		CellStorage cellStorage = ((ORSGraphHopper) _searchContext.getGraphHopper()).getPartitioningFactoryDecorator().getCellStorage();
		IsochroneNodeStorage isochroneNodeStorage = ((ORSGraphHopper) _searchContext.getGraphHopper()).getPartitioningFactoryDecorator().getIsochroneNodeStorage();
		EccentricityStorage eccentricityStorage = ((ORSGraphHopper) _searchContext.getGraphHopper()).getEccentricity().getEccentricityStorage(weighting);

		int nRanges = parameters.getRanges().length;
		double prevCost = 0;
		IsochroneMap isochroneMap = null;

		for (int i = 0; i < nRanges; i++) {

			FastIsochroneAlgorithm fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
					queryGraph,
					new PreparationWeighting(weighting),
					TraversalMode.NODE_BASED,
					cellStorage,
					isochroneNodeStorage,
					eccentricityStorage);
			fastIsochroneAlgorithm.setOriginalFrom(nonvirtualClosestNode);
			fastIsochroneAlgorithm.calcIsochroneNodes(res.get(0).getClosestNode(), parameters.getRanges()[i]);

			List<Double> contourCoordinates = new ArrayList<>();

			//printing for debug
//			printBordernodes(cellStorage.getNodesOfCell(195), isochroneNodeStorage, this._searchContext.getGraphHopper().getGraphHopperStorage().getNodeAccess());
			System.out.println("{" +
					"  \"type\": \"FeatureCollection\"," +
					"  \"features\": [");
			for (int cellId : fastIsochroneAlgorithm.getFullyReachableCells()){
				contourCoordinates.addAll(cellStorage.getCellContourOrder(cellId));
				printCell(cellStorage.getCellContourOrder(cellId), cellId);
			}
			System.out.println("]}");

			GHPoint3D snappedPosition = res.get(0).getSnappedPoint();

			AccessibilityMap edgeMap = new AccessibilityMap(fastIsochroneAlgorithm.getBestWeightMap(), snappedPosition);

			GHPoint3D point = edgeMap.getSnappedPosition();

			loc = (point == null) ? parameters.getLocation() : new Coordinate(point.lon, point.lat);

			if(isochroneMap == null) isochroneMap = new IsochroneMap(parameters.getTravellerId(), loc);

			if (LOGGER.isDebugEnabled())
			{
				sw.stop();

				LOGGER.debug("Find edges: " + sw.getSeconds());
			}

			if (edgeMap.isEmpty())
				return isochroneMap;

			_treeSet = new TreeSet<Coordinate>();

			List<Coordinate> isoPoints = new ArrayList<Coordinate>((int)(1.2*edgeMap.getMap().size() + 1.2*contourCoordinates.size()));

			double metersPerSecond = maxSpeed / 3.6;
			// only needed for reachfactor property
			double meanMetersPerSecond = meanSpeed / 3.6;

			double isoValue = parameters.getRanges()[i];
			double isochronesDifference = parameters.getRanges()[i];
			if (i > 0)
				isochronesDifference = isochronesDifference -parameters.getRanges()[i-1];

			float smoothingFactor = parameters.getSmoothingFactor();
			TravelRangeType isochroneType = parameters.getRangeType();

			if (LOGGER.isDebugEnabled())
			{
				sw = new StopWatch();
				sw.start();
			}

			double maxRadius = 0;
			double meanRadius = 0;
			switch (isochroneType) {
				case Distance:
					maxRadius = isoValue;
					meanRadius = isoValue;
					break;
				case Time:
					maxRadius = metersPerSecond * isoValue;
					meanRadius = meanMetersPerSecond * isoValue;
					isochronesDifference = metersPerSecond * isochronesDifference;
					break;
			}

			GeometryCollection points = buildIsochrone(edgeMap, contourCoordinates, isoPoints, loc.x, loc.y, isoValue, prevCost, isochronesDifference, 0.85);

			if (LOGGER.isDebugEnabled())
			{
				//	 savePoints(points, "D:\\isochrones3.shp");
				sw.stop();
				LOGGER.debug(i + " Find points: " + sw.getSeconds() + " " + points.getNumGeometries());

				sw = new StopWatch();
				sw.start();
			}

			addIsochrone(isochroneMap, points, isoValue, maxRadius, meanRadius, smoothingFactor);


			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Build concave hull: " + sw.stop().getSeconds());

			prevCost = isoValue;
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

	private void addIsochrone(IsochroneMap isochroneMap, GeometryCollection points, double isoValue, double maxRadius, double meanRadius, float smoothingFactor)
	{
		if (points.isEmpty())
			return;

		ConcaveHull ch = new ConcaveHull(points, convertSmoothingFactorToDistance(smoothingFactor, maxRadius), false);
		Geometry geom = ch.getConcaveHull();

		if (geom instanceof GeometryCollection)
		{
			GeometryCollection geomColl = (GeometryCollection)geom;
			if (geomColl.isEmpty())
				return;
		}

		Polygon poly = (Polygon)geom;

		copyConvexHullPoints(poly);

		isochroneMap.addIsochrone(new Isochrone(poly, isoValue, meanRadius));
	}

	private void markDeadEndEdges(AccessibilityMap edgeMap)
	{
		IntObjectMap<SPTEntry> map = edgeMap.getMap();
		IntObjectMap<Integer> result = new GHIntObjectHashMap<Integer>(map.size()/20);

		for (IntObjectCursor<SPTEntry> entry : map) {
			SPTEntry  edge = entry.value;
			if (edge.originalEdge == -1)
				continue;

			result.put(edge.parent.originalEdge, 1);
		}

		for (IntObjectCursor<SPTEntry> entry : map) {
			SPTEntry  edge = entry.value;
			if (edge.originalEdge == -1)
				continue;

			if (!result.containsKey(edge.originalEdge))
				edge.edge =-2;
		}
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
		points.clear();
		_treeSet.clear();

		if (prevIsoPoints != null)
			points.addAll(prevIsoPoints);

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
		double defaulPointWidth =  0.005;
		double defaultVisitorThreshold = 0.0035;

		// make results a bit more precise for regions with low data density
		if (map.size() < 10000)
		{
			defaultSearchWidth = 0.0008;
			defaulPointWidth = 0.005;
			defaultVisitorThreshold = 0.0025;
		}

		boolean useHighDetail = map.size() < 1000 || isochronesDifference < 1000;

		if (useHighDetail) {
			bufferSize = 0.00018;
			defaultVisitorThreshold = 0.000005;
		}

		int nodeId, edgeId;

		searchWidth = defaultSearchWidth;
		visitorThreshold = defaultVisitorThreshold;
		pointWidth = defaulPointWidth;

		visitor.setThreshold(visitorThreshold);

		for (IntObjectCursor<SPTEntry> entry : map) {
			goalEdge = entry.value;
			edgeId = goalEdge.originalEdge;

			if (edgeId == -1)
				continue;

			nodeId = goalEdge.adjNode;

			if (nodeId == -1 || nodeId > maxNodeId)
				continue;

			EdgeIteratorState iter = queryGraph.getEdgeIteratorState(edgeId, nodeId);
			if(((CHEdgeIteratorState) iter).isShortcut())
				continue;

			float maxCost = (float) (goalEdge.weight);
			float minCost = (float) (goalEdge.parent.weight);

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
					if (((maxCost >= detailedZone && maxCost <= isolineCost) || edgeDist > 300))
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


		int j = 0;
		while (j < contourCoordinates.size()){
			double latitude = contourCoordinates.get(j);
			j++;
			double longitude = contourCoordinates.get(j);
			j++;
			addPoint(points, qtree, longitude, latitude, true);
		}



		Geometry[] geometries = new Geometry[points.size()];

		for (int i = 0;i < points.size();++i)
		{
			Coordinate c = points.get(i);
			geometries[i] = _geomFactory.createPoint(c);
		}

		return new GeometryCollection(geometries, _geomFactory);
	}

	private void copyConvexHullPoints(Polygon poly)
	{
		LineString ring = (LineString)poly.getExteriorRing();
		if (prevIsoPoints == null)
			prevIsoPoints = new ArrayList<Coordinate>(ring.getNumPoints());
		else
			prevIsoPoints.clear();
		for (int i = 0; i< ring.getNumPoints(); ++i)
		{
			Point p = ring.getPointN(i);
			prevIsoPoints.add(new Coordinate(p.getX(), p.getY()));
		}
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

	private void printBordernodes(Set<Integer> nodes, IsochroneNodeStorage isochroneNodeStorage, NodeAccess nodeAccess){
		System.out.print("{\"type\": \"MultiPoint\",\"coordinates\": [");

		for (int node : nodes) {
			if (isochroneNodeStorage.getBorderness(node)) {

				System.out.print("[" + String.valueOf(nodeAccess.getLon(node)).substring(0, 8) + "," + String.valueOf(nodeAccess.getLat(node)).substring(0, 8) + "],");
			}
		}

		System.out.println("]}");

	}
}
