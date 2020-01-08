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
package org.heigit.ors.routing.util;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.PointList;

import java.util.LinkedList;

public class ElevationSmoother {
	private ElevationSmoother() {}

	public static PointList smooth(PointList points) {
		int nSize = points.size();
		if (nSize <= 2)
			return points;

		DistanceCalc distCalc = new DistanceCalc3D();
		final double MIN_DISTANCE = 10;
		final int WINDOW_SIZE = 20;

		PointList newPoints = new PointList(nSize, true);
		LinkedList<Double> values = new LinkedList<>();

		double x0;
		double y0;
		double z0;
		double x1;
		double y1;
		double z1;
		double elevSum = 0.0;

		x0 = points.getLon(0);
		y0 = points.getLat(0);
		z0 = points.getEle(0);

		elevSum += z0;
		values.addLast(z0);

		newPoints.add(y0, x0, z0);

		for (int i = 1; i < nSize; ++i) {
			x1 = points.getLon(i);
			y1 = points.getLat(i);
			z1 = points.getEle(i);

			double dist = distCalc.calcDist(y0, x0, y1, x1);
			if (dist > MIN_DISTANCE) {
				int n = (int)Math.ceil(dist / MIN_DISTANCE);

				for (int j = 1; j < n; j++) {
					double ele = z0 + j*(z1 -z0)/((double)(n-1));
					
					if (values.size() == WINDOW_SIZE) {
		    			elevSum -= values.getFirst();
		    			values.removeFirst();
		    		}
					
					elevSum += ele;
		    		values.addLast(ele);
				}
			} else {
				if (values.size() == WINDOW_SIZE) {
	    			elevSum -= values.getFirst();
	    			values.removeFirst();
	    		}
				
				elevSum += z1;
	    		values.addLast(z1);
			}

			double ele = elevSum / values.size();

			newPoints.add(y1, x1, ele);

			x0 = x1;
			y0 = y1;
			z0 = z1;
		}

		return newPoints;
	}
}
