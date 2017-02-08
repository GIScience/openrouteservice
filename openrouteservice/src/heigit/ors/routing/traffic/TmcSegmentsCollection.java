package org.freeopenls.routeservice.traffic;

import java.util.ArrayList;
import java.util.List;

import org.freeopenls.routeservice.isochrones.JTS;
import org.freeopenls.tools.GeomUtility;

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
