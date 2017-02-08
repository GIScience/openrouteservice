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

// Authors: M. Rylov 

package org.freeopenls.routeservice.isochrones;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;

import java.util.ArrayList;
import java.util.List;

import org.freeopenls.routeservice.graphhopper.extensions.DijkstraCostCondition;
import org.freeopenls.routeservice.graphhopper.extensions.EdgeMapInfo;
import org.freeopenls.routeservice.isochrones.isolinebuilders.*;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.routing.util.WeightingMap;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class IsochroneMapBuilder {

	private GraphHopper graphHopper;
	private SampleFactory sampleFactory;
	private EdgeFilter edgeFilter; 

	public IsochroneMapBuilder(GraphHopper gh, EdgeFilter edgeFilter) {
		graphHopper = gh;
		this.edgeFilter = edgeFilter;
	}

	private EdgeMapInfo getEdgeMap(double lat, double lon, double maxCost, String encoderName) throws Exception {
		FlagEncoder encoder = graphHopper.getEncodingManager().getEncoder(encoderName);
		GraphHopperStorage graph = graphHopper.getGraphHopperStorage();
		WeightingMap wm = new WeightingMap("fastest");
		Weighting weighting = graphHopper.createWeighting(wm, -1, encoder, graph);
		// maxCost is measured in seconds.
		//double factor = getWeightFactor();
		double cost = maxCost;// * factor;
		
		QueryResult res = graphHopper.getLocationIndex().findClosest(lat, lon, edgeFilter);
		int fromId = res.getClosestNode();

		if (fromId == -1)
			throw new Exception("The closest node is null.");
		
		// IMPORTANT: It only works with TraversalMode.NODE_BASED.
		DijkstraCostCondition dijkstraAlg = new DijkstraCostCondition(graph, encoder, weighting, cost, TraversalMode.NODE_BASED);
		dijkstraAlg.setEdgeFilter(edgeFilter);
		dijkstraAlg.computePath(fromId, Integer.MIN_VALUE);

		TIntObjectMap<EdgeEntry> edgeMap = dijkstraAlg.getMap();

		return new EdgeMapInfo(edgeMap, dijkstraAlg.getCurrentEdge());
	}

	public IsochroneMap buildMap(double lat, double lon, double maxCost, String encoderName, String method, double interval, double gridSizeMeters) throws Exception {
		// 1. Find all graph edges for a given cost.
		EdgeMapInfo edgeMap = getEdgeMap(lat, lon, maxCost + 30, encoderName);

		if (!edgeMap.isEmpty()) {
			GraphHopperStorage graph = graphHopper.getGraphHopperStorage();
			NodeAccess nodeAccess = graph.getNodeAccess();

			TIntObjectMap<EdgeEntry> map = edgeMap.getMap();

			if (map.size() == 0)
				return null;
			
			List<Coordinate> initialPoints = new ArrayList<Coordinate>(map.size());
			Quadtree qtree = new Quadtree();

			// 2. Extract edges from the shortest-path-tree determined by
			// edgeEntry.
			EdgeEntry edgeEntry = edgeMap.getEdgeEntry();
			EdgeEntry goalEdge = edgeEntry;
			
			int maxNodeId = graph.getNodes();
			
			for ( TIntObjectIterator<EdgeEntry> it = map.iterator(); it.hasNext(); ) {
			    it.advance();
				int nodeId = it.key();

				if (nodeId == -1 || nodeId > maxNodeId)
					continue;

				goalEdge = it.value();

				int edgeId = goalEdge.originalEdge;

				if (edgeId == -1)// || blockedEdges.contains(edgeId))
					continue;

				double lat1 = nodeAccess.getLat(nodeId);
				double lon1 = nodeAccess.getLon(nodeId);

				EdgeIteratorState iter = graph.getEdgeIteratorState(edgeId, nodeId);
				
				if (iter == null) // || !edgeFilter.accept(iter))
					continue;
				
				PointList pl = iter.fetchWayGeometry(3);

				int size = pl.getSize();
				if (size > 0) {
					double minX = Double.MAX_VALUE;
					double maxX = Double.MIN_VALUE;
					double minY = Double.MAX_VALUE;
					double maxY = Double.MIN_VALUE;

					for (int j = 0; j < pl.getSize(); j++) {
						double x = pl.getLon(j);
						double y = pl.getLat(j);

						if (x < minX)
							minX = x;
						if (y < minY)
							minY = y;
						if (x > maxX)
							maxX = x;
						if (y > maxY)
							maxY = y;
					}

					Envelope envGeom = new Envelope(minX, maxX, minY, maxY);

					float v1 = (float) (goalEdge.weight);
					float v2 = (goalEdge.parent != null) ? (float) (goalEdge.parent.weight) : v1;

					qtree.insert(envGeom, new EdgeInfo(pl, v2, v1, (float) iter.getDistance()));

					// 3. Store node coordinates as an initial point for grid.
					initialPoints.add(new Coordinate(lon1, lat1));
				}
			}

			sampleFactory = new SampleFactory(qtree);
			sampleFactory.setSearchRadiusM(gridSizeMeters);

			ZFunc timeFunc = new ZFunc() {
				@Override
				public long z(Coordinate c) {
					long value = sampleFactory.getSample(c.x, c.y);
					return value;
				}
			};

			AbstractIsolineBuilder isolineBuilder = null;
			
			if (Helper.isEmpty(method) || "Default".equals(method) || "RecursiveGrid".equals(method))
			{
				Coordinate center = new Coordinate(lon, lat);
				double dY = Math.toDegrees(gridSizeMeters / 6378100.0);
				double dX = dY / Math.cos(Math.toRadians(center.x));

				isolineBuilder = new RecursiveGridIsolineBuilder(dX, dY, center, timeFunc,
					initialPoints);
			}
			else if ("TIN".equals(method))
			{
				isolineBuilder = new TINIsolineBuilder(initialPoints, timeFunc);
			}
			else
			{
			    throw new Exception("Unknown method.");	
			}
			
			IsochroneMap isochroneMap = new IsochroneMap();

			if (interval <= 0) {
				GeometryCollection geoms = (GeometryCollection) isolineBuilder.computeIsoline((long) maxCost);
				addIsochrone(method, isochroneMap, geoms, maxCost);
			} else {
				int nIntervals = (int) Math.ceil(maxCost / interval);
				for (int i = 0; i < nIntervals - 1; i++) {
					double cost = (i + 1) * interval;
					GeometryCollection geoms = (GeometryCollection) isolineBuilder.computeIsoline((long) cost);
					addIsochrone(method, isochroneMap, geoms, cost);
				}
				
				GeometryCollection geoms = (GeometryCollection) isolineBuilder.computeIsoline((long) maxCost);
				addIsochrone(method, isochroneMap, geoms, maxCost);
			}

			return isochroneMap;
		}

		return null;
	}
	
	private void addIsochrone(String method, IsochroneMap isochroneMap, GeometryCollection geoms, double cost)
	{
		if (!geoms.isEmpty()) {
			for (int i = 0; i < geoms.getNumGeometries(); i++) {
				Polygon poly = (Polygon) geoms.getGeometryN(i);

				//poly = (Polygon)JTS.smooth(poly, 0.05);
				//poly = (Polygon) DouglasPeuckerSimplifier.simplify(poly, 0.0001);
				
				if (!poly.isEmpty()) {
					if ("Default".equalsIgnoreCase(method) || "RecursiveGrid".equalsIgnoreCase(method) || "TIN".equals(method) || method == null) {
						//poly = geomFactory.createPolygon(poly.getExteriorRing().getCoordinateSequence());
						isochroneMap.addIsochrone(new Isochrone(poly, cost));
					} else if ("default2".equalsIgnoreCase(method)) {
						isochroneMap.addIsochrone(new Isochrone(poly, cost));
					}
				}
			}
		}
	}
}
