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

import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.QueryRoutingCHGraph;
import com.graphhopper.routing.util.DefaultSnapFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint3D;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.exceptions.PointNotFoundException;

import java.util.*;

public class MatrixSearchContextBuilder {
	private final boolean resolveNames;
	private final LocationIndex locIndex;
	private final EdgeFilter edgeFilter;
	private Map<Coordinate, LocationEntry> locationCache;
	private GraphHopperStorage graphHopperStorage;
	private Weighting weighting;

	public MatrixSearchContextBuilder(GraphHopperStorage graphHopperStorage, LocationIndex index, EdgeFilter edgeFilter, boolean resolveNames) {
		locIndex = index;
		this.edgeFilter = edgeFilter;
		this.resolveNames = resolveNames;
		this.graphHopperStorage = graphHopperStorage;
	}

	public MatrixSearchContext create(Graph graph, RoutingCHGraph chGraph, Weighting weighting, String profileName, Coordinate[] sources, Coordinate[] destinations, double maxSearchRadius) throws Exception {
		if (locationCache == null)
			locationCache = new HashMap<>();
		else
			locationCache.clear();

		checkBounds(graph.getBounds(), sources, destinations);
		this.weighting = weighting;

		List<Snap> snaps = new ArrayList<>(sources.length + destinations.length);

		resolveLocations(profileName, sources, snaps, maxSearchRadius);
		resolveLocations(profileName, destinations, snaps, maxSearchRadius);

		QueryGraph queryGraph = QueryGraph.create(graph, snaps);
		RoutingCHGraph routingCHGraph = null;
		if (chGraph != null) {
			routingCHGraph = new QueryRoutingCHGraph(chGraph, queryGraph);
		}

		MatrixLocations mlSources = createLocations(sources);
		MatrixLocations mlDestinations = createLocations(destinations);

		return new MatrixSearchContext(queryGraph, routingCHGraph, mlSources, mlDestinations);
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
		for (int i = 0; i < coords.length; i++) {
			Coordinate c = coords[i];
			if (!bounds.contains(c.y, c.x)) {
				ids.add(i);
			}
		}
		int[] idsArray = new int[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			idsArray[i] = ids.get(i);
		}
		return idsArray;
	}

	private void resolveLocations(String profileName, Coordinate[] coords, List<Snap> queryResults, double maxSearchRadius) {
		for (Coordinate p : coords) {
			LocationEntry ld = locationCache.get(p);
			if (ld == null) {
				Snap qr = locIndex.findClosest(p.y, p.x, getSnapFilter(profileName));

				ld = new LocationEntry();
				ld.snap = qr;

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

	protected EdgeFilter getSnapFilter(String profileName) {
		EdgeFilter defaultSnapFilter = new DefaultSnapFilter(weighting, this.graphHopperStorage.getEncodingManager().getBooleanEncodedValue(Subnetwork.key(profileName)));
		//TODO when Matrix supports additional parameters such as avoidables in the future, the corresponding filters need to be added here for snapping
//		if (edgeFilterFactory != null)
//			return edgeFilterFactory.createEdgeFilter(request.getAdditionalHints(), weighting.getFlagEncoder(), ghStorage, defaultSnapFilter);
		return defaultSnapFilter;
	}


	private MatrixLocations createLocations(Coordinate[] coords) throws Exception {
		MatrixLocations mlRes = new MatrixLocations(coords.length);
		for (int i = 0; i < coords.length; i++) {
			Coordinate p = coords[i];
			LocationEntry ld = locationCache.get(p);
			if (ld != null)
				mlRes.setData(i, ld.nodeId == -1 ? -1 : ld.snap.getClosestNode(), ld.location);
			else
				throw new Exception("Oops!");
		}
		return mlRes;
	}

	class LocationEntry {
		private int nodeId;
		private ResolvedLocation location;
		private Snap snap;

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
	}
}
