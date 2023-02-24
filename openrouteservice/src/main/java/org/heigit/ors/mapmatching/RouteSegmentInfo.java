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
package org.heigit.ors.mapmatching;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.util.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.heigit.ors.util.FrechetDistance;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RouteSegmentInfo {
	private final List<EdgeIteratorState> edges;
	private final Geometry geometry;
	private final long time;
	private final double distance;

	public RouteSegmentInfo(List<EdgeIteratorState> edges, double distance, long time, Geometry geom) {
		this.edges = edges;
		this.time = time;
		this.distance = distance;
		this.geometry = geom;
	}

	public boolean isEmpty() {
		return this.edges == null || this.edges.isEmpty();
	}
	
	public double getDistance() {
		return this.distance;
	}	

	public long getTime() {
		return this.time;
	}
	
	public String getEdgeName(int index)
	{
		return edges.get(index).getName();
	}

	/**
	 * @return Returns all Edges.
	 */
	public List<EdgeIteratorState> getEdgesStates() {
		return edges;
	}

	public List<Integer> getEdges() {
		List<Integer> res = new ArrayList<>(edges.size());

		for(EdgeIteratorState edge : edges)
			res.add(EdgeIteratorStateHelper.getOriginalEdge(edge));

		return res;
	}

	public Geometry getGeometry() {
		return this.geometry;
	}

	public double getLength()
	{
		return this.geometry.getLength();
	}

	public double getLength(DistanceCalc dc) {
		double res = 0;

		if (this.getGeometry() != null) {
			LineString ls = (LineString) this.getGeometry();
			int nPoints = ls.getNumPoints();

			if (nPoints > 1) {
				Coordinate c = ls.getCoordinateN(0);
				double x0 = c.x;
				double y0 = c.y;
				for (int i = 1; i < ls.getNumPoints(); i++) {
					c = ls.getCoordinateN(i);

					res += dc.calcDist(y0, x0, c.y, c.x);
					x0 = c.x;
					y0 = c.y;
				}
			}
		}

		return res;
	}

	public String getNearbyStreetName(PointList points, boolean ignoreAdjacency) {
		if (edges.isEmpty())
			return null;

		String result = null;
		Point2D[] p = getPoints(points);
		double minValue = Double.MAX_VALUE;

		for(EdgeIteratorState edge : edges) {
			String edgeName = edge.getName();
			if (Helper.isEmpty(edgeName))
				continue;

			PointList pl = edge.fetchWayGeometry(FetchMode.ALL);
			if (pl.size() <= 1)
				continue;

			if (ignoreAdjacency && arePolylinesAdjacent(points, pl))
				return null;

			Point2D[] q = getPoints(pl);

			FrechetDistance fd = new FrechetDistance(p, q);

			try {
				double value = fd.computeFrechetDistance();// * pl.calcDistance(dc)/1000.0
				if (value < minValue && value < 1.5E-6) {
					minValue = value;
					result = edgeName;
				}
			} catch(Exception ex) {
				// do nothing
			}
		}

		return result;
	}

	private Point2D[] getPoints(PointList points) {
		List<Point2D> res = new ArrayList<>(points.size());
		double lon0 = 0;
		double lat0 = 0;
		double lon1;
		double lat1;
		for (int i = 0; i < points.size(); i++) {
			lon1 = points.getLon(i);
			lat1 = points.getLat(i);
			if (i > 0 && (lon0 == lon1 || lat0 == lat1))
				continue;

			Point2D p = new Point2D.Double(lon1, lat1);
			res.add(p);
			lon0 = lon1;
			lat0 = lat1;
		}
		return res.toArray(new Point2D[0]);
	}

	private boolean arePolylinesAdjacent(PointList pl1, PointList pl2) {
		for (int i = 0; i < pl1.size(); i++) {
			double lon0 = pl1.getLon(i);
			double lat0 = pl1.getLat(i);

			for (int j = 0; j < pl2.size(); j++) {
				double lon1 = pl2.getLon(j);
				double lat1 = pl2.getLat(j);

				if (lon0 == lon1 && lat0 == lat1)
					return true;
			}
		}
		return false;
	}
}
