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
package org.heigit.ors.routing.util.extrainfobuilders;

import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.PointList;
import org.heigit.ors.routing.RouteExtraInfo;
import org.heigit.ors.routing.RouteSegmentItem;
import org.heigit.ors.routing.util.SteepnessUtil;

public class SteepnessExtraInfoBuilder extends RouteExtraInfoBuilder {
	private boolean firstSegment = true;
	private double x0;
	private double y0;
	private double z0;
	private double cumElevation = 0.0;
	private double maxAltitude = Double.MIN_VALUE;
	private double minAltitude = Double.MAX_VALUE;
	private double splitLength = 0.0;
	private int prevGradientCat = 0;
	private int pointsCount = 0;
	private RouteSegmentItem prevSegmentItem;
	private final DistanceCalc3D distCalc;
	private boolean lastEdge;
	
    public SteepnessExtraInfoBuilder(RouteExtraInfo extraInfo) {
		super(extraInfo);
		distCalc = new DistanceCalc3D();
	}

	public void addSegment(double value, long valueIndex, PointList geom, double dist, boolean lastEdge) {
		this.lastEdge = lastEdge;
    }

	public void addSegment(double value, long valueIndex, PointList geom, double dist) {
		throw new UnsupportedOperationException("SimpleRouteExtraInfoBuilder does not support method addSegment without lastEdge flag.");
	}
	
	public void addPoints(PointList geom) {
		int nPoints = geom.size() - 1;
		if (nPoints == 0)
			return;		
		
		int j0 = 0;
		
		if (firstSegment) {
			j0 = 1;

			x0 = geom.getLon(0);
			y0 = geom.getLat(0);
			z0 = geom.getEle(0);
			
			maxAltitude = z0;
			minAltitude = z0;
			pointsCount++;
			
			firstSegment = false;
		}

		double elevDiff;
		for (int j = j0; j < nPoints; ++j) {
			double x1 = geom.getLon(j);
			double y1 = geom.getLat(j);
			double z1 = geom.getEle(j);
			
			elevDiff = z1 - z0;
			cumElevation += elevDiff;
			double segLength = distCalc.calcDist(y0, x0, z0, y1, x1, z1);

			double prevMinAltitude = minAltitude;
			double prevMaxAltitude = maxAltitude;
			if (z1 > maxAltitude)
				maxAltitude = z1;
			if (z1 < minAltitude)
				minAltitude = z1;

			if ((prevMaxAltitude - z1 > SteepnessUtil.ELEVATION_THRESHOLD || z1 - prevMinAltitude > SteepnessUtil.ELEVATION_THRESHOLD) && splitLength > 30) {
				boolean bApply = true;
				int elevSign = (cumElevation - elevDiff) > 0 ? 1 : -1;
				double gradient = elevSign*100*(prevMaxAltitude - prevMinAltitude) / splitLength;
				
				if (prevGradientCat != 0 ) {
					double zn= Double.MIN_NORMAL;
					
					if (j + 1 < nPoints)
					  zn = geom.getEle(j + 1);

					if (zn != Double.MIN_VALUE) {
						double elevGap = segLength/30;
						if ((
								elevSign > 0 && prevGradientCat > 0 || prevGradientCat < 0
							)
							&& Math.abs(zn - z1) < elevGap)
							bApply = false;
					}
				}
				
				if (bApply) {
					int gradientCat = SteepnessUtil.getCategory(gradient);
					int startIndex = prevSegmentItem != null ? prevSegmentItem.getTo() : 0;

					if (prevGradientCat == gradientCat && prevSegmentItem != null) {
						prevSegmentItem.setTo(prevSegmentItem.getTo() + pointsCount);
						prevSegmentItem.setDistance(prevSegmentItem.getDistance() + splitLength);
					} else {

						RouteSegmentItem item = new RouteSegmentItem(startIndex, startIndex + pointsCount, gradientCat, splitLength);
						extraInfo.add(item);
						prevSegmentItem = item;
					}
					
					pointsCount = 0;
					prevGradientCat = gradientCat;
					minAltitude = Math.min(z0, z1);
					maxAltitude = Math.max(z0, z1);
					splitLength = 0.0;
					
					cumElevation = elevDiff;
				}
			}
			
			splitLength += segLength;
			
			x0 = x1;
			y0 = y1;
			z0 = z1;
			
			pointsCount++;
		}
		
		if (lastEdge && splitLength > 0) {
			elevDiff = maxAltitude - minAltitude;
			if (extraInfo.isEmpty() && splitLength < 50 && elevDiff < SteepnessUtil.ELEVATION_THRESHOLD)
				elevDiff = 0;
			
			double gradient = (cumElevation > 0 ? 1: -1)*100* elevDiff / splitLength;
			int gradientCat = SteepnessUtil.getCategory(gradient);
			
			if (prevSegmentItem != null && (prevGradientCat == gradientCat || splitLength < 30)) {
				prevSegmentItem.setTo(prevSegmentItem.getTo() + pointsCount);
			} else {
				int startIndex = prevSegmentItem != null ? prevSegmentItem.getTo() : 0;
				
				RouteSegmentItem item = new RouteSegmentItem(startIndex, startIndex + pointsCount, gradientCat, splitLength);
				extraInfo.add(item);
				
				prevSegmentItem = item;
				prevGradientCat = gradientCat;
				pointsCount = 0;
			}
		}
	}
	
	public void finish() {
    	// do nothing
	}
}
