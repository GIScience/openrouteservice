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
package org.heigit.ors.matrix;

import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.exceptions.PointNotFoundException;

import java.util.*;

public class MatrixSearchContextBuilder {
	private Map<Coordinate, LocationEntry> locationCache;
	private boolean resolveNames;
	private LocationIndex locIndex;
	private EdgeFilter edgeFilter;

	class LocationEntry {
		private int nodeId;
		private ResolvedLocation location;
		private QueryResult queryResult;

		public int getNodeId() {
			return nodeId;
		}

		public void setNodeId(int nodeId) {
			this.nodeId = nodeId;
		}

		public ResolvedLocation getLocation() {
			return location;
		}

		public void setLocation(ResolvedLocation location) {
			this.location = location;
		}

		public QueryResult getQueryResult() {
			return queryResult;
		}

		public void setQueryResult(QueryResult queryResult) {
			this.queryResult = queryResult;
		}
	}

	public MatrixSearchContextBuilder(LocationIndex index, EdgeFilter edgeFilter, boolean resolveNames) {
		locIndex = index;
		this.edgeFilter = edgeFilter;
		this.resolveNames = resolveNames;
	}

	public MatrixSearchContext create(Graph graph, Coordinate[] sources, Coordinate[] destinations, double maxSearchRadius) throws Exception {
		if (locationCache == null)
			locationCache = new HashMap<>();
		else
			locationCache.clear();

		checkBounds(graph.getBounds(), sources, destinations);

		QueryGraph queryGraph = new QueryGraph(graph);
		List<QueryResult> queryResults = new ArrayList<>(sources.length + destinations.length);
		
		resolveLocations(sources, queryResults, maxSearchRadius);
		resolveLocations(destinations, queryResults, maxSearchRadius);

		queryGraph.lookup(queryResults);
		
		MatrixLocations mlSources = createLocations(sources);
		MatrixLocations mlDestinations = createLocations(destinations);
		
		return new  MatrixSearchContext(queryGraph, mlSources, mlDestinations);
	}

	private void checkBounds(BBox bounds, Coordinate[] sources, Coordinate[] destinations) throws PointNotFoundException {
		String[] messages = new String[2];
		messages[0] = constructPointOutOfBoundsMessage("Source", bounds, sources);
		messages[1] = constructPointOutOfBoundsMessage("Destination", bounds, destinations);

		String exceptionMessage = messages[0];
		if (!exceptionMessage.isEmpty() && !messages[1].isEmpty())
			exceptionMessage += ". ";
		exceptionMessage += messages[1];

		if (!exceptionMessage.isEmpty())
			throw new PointNotFoundException(exceptionMessage, MatrixErrorCodes.POINT_NOT_FOUND);
	}

	private String constructPointOutOfBoundsMessage(String pointsType, BBox bounds, Coordinate[] coords) {
		int[] pointIds = pointIdsOutOfBounds(bounds, coords);
		String message = "";
		if (pointIds.length > 0) {
			String idString = Arrays.toString(pointIds);
			StringBuilder coordsString = new StringBuilder();
			for (int id : pointIds) {
				if (coordsString.length() > 0) {
					coordsString.append("; ");
				}
				coordsString.append(coords[id].y).append(",").append(coords[id].x);
			}

			message = pointsType + " point(s) " + idString + " out of bounds: " + coordsString;
		}
		return message;
	}

	private int[] pointIdsOutOfBounds(BBox bounds, Coordinate[] coords) {
		List<Integer> ids = new ArrayList<>();
		for (int i=0; i<coords.length; i++) {
			Coordinate c = coords[i];
			if (!bounds.contains(c.y, c.x)) {
				ids.add(i);
			}
		}
		int[] idsArray = new int[ids.size()];
		for(int i=0; i<ids.size(); i++) {
			idsArray[i] = ids.get(i);
		}
		return idsArray;
	}
	
	private void resolveLocations(Coordinate[] coords, List<QueryResult> queryResults, double maxSearchRadius) {
		for (Coordinate p : coords) {
			LocationEntry ld = locationCache.get(p);
			if (ld == null) {
				QueryResult qr = locIndex.findClosest(p.y, p.x, edgeFilter);

				ld = new LocationEntry();
				ld.queryResult = qr;

				if (qr.isValid() && qr.getQueryDistance() < maxSearchRadius) {
					GHPoint3D pt = qr.getSnappedPoint();
					ld.nodeId = qr.getClosestNode();
					ld.location = new ResolvedLocation(new Coordinate(pt.getLon(), pt.getLat()), resolveNames ? qr.getClosestEdge().getName() : null, qr.getQueryDistance());

					queryResults.add(qr);
				} else {
					ld.nodeId = -1;
				}
				locationCache.put(p, ld);
			}
		}
	}
 	
	private MatrixLocations createLocations(Coordinate[] coords) throws Exception {
		MatrixLocations mlRes = new MatrixLocations(coords.length);
		for (int i = 0; i < coords.length; i++) {
			Coordinate p = coords[i];
			LocationEntry ld = locationCache.get(p);
			if (ld != null)
				mlRes.setData(i, ld.nodeId == -1 ? -1 : ld.queryResult.getClosestNode(), ld.location);
			else
				throw new Exception("Oops!");
		}
		return mlRes;
	}
}
