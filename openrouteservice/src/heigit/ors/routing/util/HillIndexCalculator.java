package org.freeopenls.routeservice.documents.instruction;

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
				gradient = Math.min(split.Gradient, 20);
			}
			totalDistance += split.Length*0.000621371;
		}

		int hillIndex = (int)(100*(totalVerticalClimb + excessSteepClimb)/(5280*totalDistance));

		return (hillIndex >= 20) ? 20 : (byte)hillIndex;
	}
}
