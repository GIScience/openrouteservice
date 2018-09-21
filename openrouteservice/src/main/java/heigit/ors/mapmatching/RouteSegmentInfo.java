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
package heigit.ors.mapmatching;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.util.FrechetDistance;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RouteSegmentInfo {
	private List<EdgeIteratorState> edges;
	private Geometry geometry;
	private long time;
	private double distance;

	public RouteSegmentInfo(List<EdgeIteratorState> edges, double distance, long time, Geometry geom) {
		this.edges = edges;
		this.time = time;
		this.distance = distance;
		this.geometry = geom;
	}

	public boolean isEmpty() {
		return this.edges == null || this.edges.size() == 0;
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

	public List<Integer> getEdges() {
		List<Integer> res = new ArrayList<Integer>(edges.size());
	    
		for(EdgeIteratorState edge : edges)
			res.add(EdgeIteratorStateHelper.getOriginalEdge(edge));
		
		return res;
	}

	public Geometry getGeometry() {
		if (geometry == null)
		{
			
		}
		
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
	
	public String getNearbyStreetName(PointList points, boolean ignoreAdjacency)
	{
		if (edges.size() == 0)
			return null;
		
		String result = null;
		Point2D[] p = getPoints(points);
		double minValue = Double.MAX_VALUE;
		String edgeName = null;
		
		for(EdgeIteratorState edge : edges)
		{
			edgeName = edge.getName();
			
			if (Helper.isEmpty(edgeName))
				continue;
			
			PointList pl = edge.fetchWayGeometry(3);
			if (pl.getSize() <= 1)
				continue;
			
			if (ignoreAdjacency)
			{
				if (arePolylinesAdjacent(points, pl))
					return null;
			}
			
			Point2D[] q = getPoints(pl);
			
			 FrechetDistance fd = new FrechetDistance(p, q);

			 try
			 {
				 double value = fd.computeFrechetDistance();// * pl.calcDistance(dc)/1000.0;
				 if (value < minValue && value < 1.5E-6)
				 {
					 minValue = value;
					 result = edgeName;
				 }
			 }
			 catch(Exception ex)
			 {
				 
			 }
		}
		
		return result;
	}
	
	private Point2D[] getPoints(PointList points)
	{
		List<Point2D> res = new ArrayList<Point2D>(points.getSize()); 
		
		double lon0 = 0, lat0 = 0, lon1, lat1;
		for (int i = 0; i < points.getSize(); i++)
		{
			lon1 = points.getLon(i);
			lat1 = points.getLat(i);
			if (i > 0)
			{
				if (lon0 == lon1 || lat0 == lat1)
					continue;
			}
			
			Point2D p = new Point2D.Double(lon1, lat1);
			res.add(p);
			
			lon0 = lon1;
			lat0 = lat1;
		}
		
		return res.toArray(new Point2D[res.size()]);
	}
	
	private boolean arePolylinesAdjacent(PointList pl1, PointList pl2)
	{
		for (int i = 0; i < pl1.getSize(); i++)
		{
			double lon0 = pl1.getLon(i);
			double lat0 = pl1.getLat(i);
			
			for (int j = 0; j < pl2.getSize(); j++)
			{
				double lon1 = pl2.getLon(j);
				double lat1 = pl2.getLat(j);
				
				if (lon0 == lon1 && lat0 == lat1)
					return true;
			}
		}
		
		return false;
	}
}
