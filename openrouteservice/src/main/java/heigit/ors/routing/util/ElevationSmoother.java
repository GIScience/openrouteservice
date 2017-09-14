/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.util;

import java.util.LinkedList;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.PointList;

public class ElevationSmoother 
{
	public static PointList smooth(PointList points)
	{
		int nSize = points.size();
		if (nSize <= 2)
			return points;

		DistanceCalc distCalc = new DistanceCalc3D();
		double MIN_DISTANCE = 10;
		int WINDOW_SIZE = 20;

		PointList newPoints = new PointList(nSize, true);
		LinkedList<Double> values = new LinkedList<Double>();

		double x0, y0, z0, x1 = 0, y1 = 0, z1 = 0;
		double elevSum = 0.0;

		x0 = points.getLon(0);
		y0 = points.getLat(0);
		z0 = points.getEle(0);

		elevSum += z0;
		values.addLast(z0);

		newPoints.add(y0, x0, z0);

		for (int i = 1; i < nSize; ++i)
		{
			x1 = points.getLon(i);
			y1 = points.getLat(i);
			z1 = points.getEle(i);

			double dist = distCalc.calcDist(y0, x0, y1, x1);
			if (dist > MIN_DISTANCE)
			{
				int n = (int)Math.ceil(dist / MIN_DISTANCE);

				for (int j = 1; j < n; j++)
				{
					double ele = z0 + j*(z1 -z0)/((double)(n-1));
					
					if (values.size() == WINDOW_SIZE)
		    		{
		    			elevSum -= ((Double) values.getFirst()).doubleValue();
		    			values.removeFirst();
		    		}
					
					elevSum += ele;
		    		values.addLast(ele);
				}
			}
			else
			{
				if (values.size() == WINDOW_SIZE)
	    		{
	    			elevSum -= ((Double) values.getFirst()).doubleValue();
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
