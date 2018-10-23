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
package heigit.ors.routing.traffic;

import java.util.List;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.shapes.BBox;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class TmcSegmentsCollection
{
	private List<TmcSegment> segments;
	private Quadtree quadTree;
	private GeometryFactory geomFactory;
	private DistanceCalc distanceCalc;

	public TmcSegmentsCollection(List<TmcSegment> segments)
	{
		this.segments = segments;
		this.geomFactory = new GeometryFactory();
		this.distanceCalc = new DistanceCalcEarth();
	}
	
	private void buildQuadTree()
	{
		quadTree = new Quadtree();
		for (TmcSegment seg : segments) {
				quadTree.insert(seg.getGeometry().getEnvelopeInternal(), seg);
		}
	}
	
	public int size()
	{
		return segments.size();
	}
	
	public TmcSegment get(int index)
	{
		return segments.get(index);
	}
	
	public List<TmcSegment> getSegments()
	{
		return segments;
	}
	
	public TmcSegment getClosestSegment(Coordinate c, double thresholdDistance)
	{
		if (quadTree == null)
			buildQuadTree();
		
		Point p = geomFactory.createPoint(c);
		BBox bbox = distanceCalc.createBBox(c.y, c.x, thresholdDistance);
		Envelope env = new Envelope(bbox.minLon, bbox.maxLon, bbox.minLat, bbox.maxLat);
		
		@SuppressWarnings("unchecked")
		List<TmcSegment> segments = (List<TmcSegment>)(quadTree.query(env));
		double minDistance = Double.MAX_VALUE;
		TmcSegment res = null;
		for( TmcSegment seg : segments)
		{
 			Coordinate c1 = DistanceOp.nearestPoints(seg.getGeometry(), p)[0];
			double dist =  distanceCalc.calcDist(c.y, c.x, c1.y, c1.x);
			
			if (dist < minDistance)
			{
				minDistance = dist;
				
				if (dist <= thresholdDistance)
					res = seg;
			}
		}
		
		return res;
	}
}
