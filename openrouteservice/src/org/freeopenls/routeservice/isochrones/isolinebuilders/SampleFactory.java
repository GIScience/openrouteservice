package org.freeopenls.routeservice.isochrones.isolinebuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geomgraph.EdgeNodingValidator;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class SampleFactory {
	public class ArrayListVisitor  implements ItemVisitor
	{
		private int index = 0;
		private int nItems = 0;
		private EdgeInfo[] items;
		private Envelope bounds;
		
		public ArrayListVisitor() {
			nItems = 100;
			items = new EdgeInfo[nItems];
		}
		
		public int getLength() {
			return index;
		}
		
		public void setBounds(Envelope bounds)
		{
			this.bounds = bounds;
		}

		public void visitItem(Object item)
		{
			EdgeInfo edge = (EdgeInfo)item;
		    //if (edge.intersect(this.bounds)) // this additional check produces some artifacts in isochrones
			{
				items[index] = edge;
				index++;
				
				if (index >= nItems)
				{
					EdgeInfo[] arr = Arrays.copyOf(items, nItems + 500);
					nItems += 500;
					items = arr;
				}
			}
		}

		public EdgeInfo[] getItems() { return items; }

		public void clear()
		{
			index = 0;
		}
	}

	
	private static final DistanceCalc dce = new DistanceCalcEarth();

	private double searchRadiusM;
	private double searchRadiusLat;

	private Quadtree qtree;
	private ArrayListVisitor itemsVisitor;

	public SampleFactory(Quadtree qtree) {
		this.qtree = qtree;
		this.setSearchRadiusM(200);
		itemsVisitor = new ArrayListVisitor();
	}

	public void setSearchRadiusM(double radiusMeters) {
		this.searchRadiusM = radiusMeters;
		this.searchRadiusLat = 360 * radiusMeters / (2 * Math.PI * 6378100.0);
	}

	public long getSample(double lon, double lat) {
		// find scaling factor for equirectangular projection
		double xscale = Math.cos(lat * Math.PI / 180.0);

		// query always returns a (possibly empty) list, but never null
		Envelope env = new Envelope(lon, lon, lat, lat);
		env.expandBy(searchRadiusLat / xscale/40.0, searchRadiusLat/40.0);
		itemsVisitor.setBounds(env);		
		qtree.query(env, itemsVisitor);

		// look for edges and make a sample
		@SuppressWarnings("unchecked")
		long res = findClosestValue(itemsVisitor.getItems(), itemsVisitor.getLength(), lon, lat, xscale);
		
		itemsVisitor.clear();
		
		if (res == Long.MAX_VALUE)
		{
			env.init(lon, lon, lat, lat);
			env.expandBy(searchRadiusLat / xscale, searchRadiusLat);
			itemsVisitor.setBounds(env);
			qtree.query(env, itemsVisitor);
			// look for edges and make a sample
			@SuppressWarnings("unchecked")
			long res2 = findClosestValue(itemsVisitor.getItems(), itemsVisitor.getLength(), lon, lat, xscale);
			res = res2;
			
			itemsVisitor.clear();
		}
		
		return res;
	}
	
	private long findClosestValue(EdgeInfo[] edges, int edgesCount, double px, double py, double xscale) {
		double cx, cy, cdist2 = Double.POSITIVE_INFINITY;
		double bx = 0, by = 0, bdist2 = Double.POSITIVE_INFINITY;
		EdgeInfo cEdge, bEdge = null;
		int cseg, bseg = -1;
		/*double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
        boolean hasBounds = false;*/
		
		for (int i = 0; i < edgesCount; i++) {
			EdgeInfo edge = edges[i];
			//hasBounds = edge.hasBounds();
			PointList pl = edge.getGeometry();
			int numCoords = pl.getSize();

			if (numCoords > 1) {
				cEdge = edge;

				double x0 = pl.getLon(0);
				double y0 = pl.getLat(0);
				
				//minX = maxX = x0;
				//minY = maxY = y0;
				
				cseg = 0;
				for (int seg = 1; seg < numCoords - 1; seg++) {
					cseg = seg - 1;

					double x1 = pl.getLon(seg);
					double y1 = pl.getLat(seg);

					// use bounding rectangle to find a lower bound on (squared)
					// distance ?
					// this would mean more squaring or roots.
					double frac = segmentFraction(x0, y0, x1, y1, px, py, xscale);
					// project to get closest point
					cx = x0 + frac * (x1 - x0);
					cy = y0 + frac * (y1 - y0);

					// find distance to edge (do not take root)
					double dx = cx - px; // * xscale;
					double dy = cy - py;
					cdist2 = dx * dx + dy * dy;
					// replace best segments
					if (cdist2 < bdist2) {
						bdist2 = cdist2;
						bx = cx;
						by = cy;
						bEdge = cEdge;
						bseg = cseg;
					}
					
					/*if (!hasBounds)
					{
						if (minX > x1)
							minX = x1;
						if (minY > y1)
							minY = y1;
						if (maxX < x1)
							maxX = x1;
						if (maxY < y1)
							maxY = y1;
					}*/

					x0 = x1;
					y0 = y1;
				}
				
				//if (!hasBounds)
				//	edge.setBounds(new Envelope(minX, maxX, minY, maxY));
			}
		}

		if (bEdge != null) {
			double v1 = bEdge.getV1();
			double v2 = bEdge.getV2();

			double dist = distanceAlong(bEdge.getGeometry(), bseg, bx, by);
			double d2 = dce.calcDist(by, bx, py, px);
			if (d2 <= searchRadiusM)
			{
				double totalCost = v2 - v1;
				double edgeDist = bEdge.getDistance();
				double costPerMeter = totalCost / edgeDist;

				double res = v1 + totalCost * dist / edgeDist + d2 * costPerMeter;
				return (long)res;
			}
		}

		return Long.MAX_VALUE;
	}

	private double distanceAlong(PointList pl, int seg, double x, double y) {
		double dist = 0.0;
		double x0 = pl.getLon(0);
		double y0 = pl.getLat(0);
		for (int s = 1; s < seg; s++) {
			double x1 = pl.getLon(s);
			double y1 = pl.getLat(s);
			dist += dce.calcDist(y0, x0, y1, x1);
			x0 = x1;
			y0 = y1;
		}
		dist += dce.calcDist(y0, x0, y, x); // dist along partial segment

		return dist;
	}

	/**
	 * Adapted from com.vividsolutions.jts.geom.LineSegment Combines
	 * segmentFraction and projectionFactor methods.
	 */
	public static double segmentFraction(double x0, double y0, double x1, double y1, double xp, double yp, double xscale) {
		// Use comp.graphics.algorithms Frequently Asked Questions method
		double dx = (x1 - x0) * xscale;
		double dy = y1 - y0;
		double len2 = dx * dx + dy * dy;
		// this fixes a (reported) divide by zero bug in JTS when line segment
		// has 0 length
		if (len2 == 0)
			return 0;
		double r = ((xp - x0) * xscale * dx + (yp - y0) * dy) / len2;
		if (r < 0.0)
			return 0.0;
		else if (r > 1.0)
			return 1.0;
		return r;
	}
}
