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
package heigit.ors.isochrones.builders.concaveballs;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import heigit.ors.isochrones.GraphEdgeMapFinder;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.builders.AbstractIsochroneMapBuilder;
import heigit.ors.routing.RouteSearchContext;
import heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.apache.log4j.Logger;
import org.opensphere.geometry.algorithm.ConcaveHull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ConcaveBallsIsochroneMapBuilder extends AbstractIsochroneMapBuilder 
{
	private final Logger LOGGER = Logger.getLogger(ConcaveBallsIsochroneMapBuilder.class.getName());

	private double searchWidth = 0.0007; 
	private double pointWidth = 0.0005;
	private double visitorThreshold = 0.0013;
	private Envelope searchEnv = new Envelope();
	private GeometryFactory _geomFactory;
	private PointItemVisitor visitor = null;
	private List<Coordinate> prevIsoPoints = null;
    private TreeSet<Coordinate>_treeSet;
	private RouteSearchContext _searchContext;

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

		Coordinate loc = parameters.getLocation();
		IsochroneMap isochroneMap = new IsochroneMap(parameters.getTravellerId(), loc);
		AccessibilityMap edgeMap = GraphEdgeMapFinder.findEdgeMap(_searchContext, parameters);

		if (LOGGER.isDebugEnabled())
		{
			sw.stop();

			LOGGER.debug("Find edges: " + sw.getSeconds());
		}

		if (edgeMap.isEmpty())
			return isochroneMap;

		_treeSet = new TreeSet<Coordinate>();

		List<Coordinate> isoPoints = new ArrayList<Coordinate>((int)(1.2*edgeMap.getMap().size()));

		if (LOGGER.isDebugEnabled())
		{
			sw = new StopWatch();
			sw.start();
		}

		markDeadEndEdges(edgeMap);

		if (LOGGER.isDebugEnabled())
		{
			sw.stop();
			LOGGER.debug("Mark dead ends: " + sw.getSeconds());
		}

		int nRanges = parameters.getRanges().length;
		double metersPerSecond = maxSpeed / 3.6;

		double prevCost = 0;
		for (int i = 0; i < nRanges; i++) {
			double isoValue = parameters.getRanges()[i];

			if (LOGGER.isDebugEnabled())
			{
				sw = new StopWatch();
				sw.start();
			}

			GeometryCollection points = buildIsochrone(edgeMap, isoPoints, loc.x, loc.y, isoValue, prevCost,maxSpeed, 0.85);

			if (LOGGER.isDebugEnabled())
			{
				//	 savePoints(points, "D:\\isochrones3.shp");
				sw.stop();
				LOGGER.debug(i + " Find points: " + sw.getSeconds() + " " + points.getNumGeometries());

				sw = new StopWatch();
				sw.start();
			}

			addIsochrone(isochroneMap, points, isoValue, metersPerSecond * isoValue);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Build concave hull: " + sw.stop().getSeconds());

			prevCost = isoValue;
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Total time: " + swTotal.stop().getSeconds());

		return isochroneMap;
	}
	
	private double getConcaveHullTreshold(double isoValue)
	{
		//	private double CONCAVE_HULL_THRESHOLD = 0.012;

		/*if (isoValue < 10000)
			return 0.005;
		else if (isoValue < 30000)
			return 0.008;
		else if (isoValue < 50000)
			return 0.012;
		else if (isoValue < 100000)
			return 0.02;*/
		
		return 0.012;
	}

	private void addIsochrone(IsochroneMap isochroneMap, GeometryCollection points, double isoValue, double maxRadius)
	{
		if (points.isEmpty())
			return;

		ConcaveHull ch = new ConcaveHull(points, getConcaveHullTreshold(isoValue), false);
		Geometry geom = ch.getConcaveHull();

		if (geom instanceof GeometryCollection)
		{
			GeometryCollection geomColl = (GeometryCollection)geom;
			if (geomColl.isEmpty())
				return;
		}

		Polygon poly = (Polygon)geom;

		copyConvexHullPoints(poly);

		isochroneMap.addIsochrone(new Isochrone(poly, isoValue, maxRadius));
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

	private GeometryCollection buildIsochrone(AccessibilityMap edgeMap, List<Coordinate> points, double lon, double lat,
			double isolineCost, double prevCost,  double maxSpeed, double detailedGeomFactor) {
		IntObjectMap<SPTEntry> map = edgeMap.getMap();

		points.clear();
		_treeSet.clear();

		if (prevIsoPoints != null)
			points.addAll(prevIsoPoints);

		GraphHopperStorage graph = _searchContext.getGraphHopper().getGraphHopperStorage();
		NodeAccess nodeAccess = graph.getNodeAccess();
		int maxNodeId = graph.getNodes();

		SPTEntry edgeEntry = edgeMap.getEdgeEntry();
		SPTEntry goalEdge = edgeEntry;

		DistanceCalc dcFast = new DistancePlaneProjection();
		double bufferSize = 0.0018;
		Quadtree qtree = new Quadtree();
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
		
		int nodeId, edgeId;

		for (IntObjectCursor<SPTEntry> entry : map) {
			goalEdge = entry.value;
			edgeId = goalEdge.originalEdge;

			if (edgeId == -1)
				continue;

			nodeId = goalEdge.adjNode;

			if (nodeId == -1 || nodeId > maxNodeId)
				continue;
			
			EdgeIteratorState iter = graph.getEdgeIteratorState(edgeId, nodeId);

			float maxCost = (float) (goalEdge.weight);
			float minCost = (float) (goalEdge.parent.weight);

			// ignore all edges that have been considered in the previous step
			if (minCost < prevCost)
				continue;

			searchWidth = defaultSearchWidth; 
			visitorThreshold = defaultVisitorThreshold; 
			pointWidth = defaulPointWidth;

			visitor.setThreshold(visitorThreshold);

			// edges that are fully inside of the isochrone
			if (isolineCost >= maxCost) {

				if (goalEdge.edge == -2)
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
}
