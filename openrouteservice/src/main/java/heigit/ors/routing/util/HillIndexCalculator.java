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

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.routing.util.RouteSplit;
import com.graphhopper.routing.util.SteepnessUtil;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.PointList;

public class HillIndexCalculator {
   private DistanceCalc distCalc;
   private List<RouteSplit> splits;
   
   public HillIndexCalculator()
   {
	   distCalc = new DistanceCalc3D();
	   splits = new ArrayList<RouteSplit>();
   }
   
    // the formula for hillIndex is taken from http://www.roberts-1.com/bikehudson/r/m/hilliness/#grade
	public byte getHillIndex(PointList points, boolean reverse)
	{
		SteepnessUtil.computeRouteSplits(points, reverse, distCalc, splits);
		
		double totalVerticalClimb = 0.0;
		double excessSteepClimb = 0.0;
		double totalDistance = 0.0;
		
		for(RouteSplit split : splits)
		{
			double gradient = split.Gradient;
			if (gradient > 0)
			{
				double vc = split.VerticalClimb*3.28084;
				totalVerticalClimb += vc;
				gradient = Math.min(split.Gradient, 30);
			}
			totalDistance += split.Length*0.000621371;
		}

		int hillIndex = (int)(100*(totalVerticalClimb + excessSteepClimb)/(5280*totalDistance));

		return (hillIndex >= 35) ? 35 : (byte)hillIndex;
	}
}
