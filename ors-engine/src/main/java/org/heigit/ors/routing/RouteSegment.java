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
package org.heigit.ors.routing;

import com.graphhopper.ResponsePath;
import com.graphhopper.ResponsePath;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.FormatUtility;

import java.util.ArrayList;
import java.util.List;

public class RouteSegment {
	private final double distance;
	private final double duration;
	private final double ascent;
	private final double descent;
	private double detourFactor = 0.0;
	private final List<RouteStep> steps;

	public RouteSegment(ResponsePath path, DistanceUnit units) throws Exception {
		distance = FormatUtility.roundToDecimalsForUnits(DistanceUnitUtil.convert(path.getDistance(), DistanceUnit.METERS, units), units);
		duration = FormatUtility.roundToDecimals(path.getTime()/1000.0, 1);
		ascent = path.getAscend();
		descent = path.getDescend();
		steps = new ArrayList<>();
	}

	public double getDistance()
	{
		return distance;
	}   

	public double getDuration()
	{
		return duration;
	}

	public double getAscent()
	{
		return ascent;
	}

	public double getDescent()
	{
		return descent;
	}

	public double getAscentRounded()
	{
		return FormatUtility.roundToDecimals(ascent, 1);
	}

	public double getDescentRounded()
	{
		return FormatUtility.roundToDecimals(descent, 1);
	}

	public void addStep(RouteStep step)
	{
		steps.add(step);
	}

	public void addSteps(List<RouteStep> stepsToAdd)
	{
		steps.addAll(stepsToAdd);
	}

	public List<RouteStep> getSteps() {
		return steps;
	}

	public double getDetourFactor() {
		return detourFactor;
	}

	public void setDetourFactor(double detourFactor) {
		this.detourFactor = detourFactor;
	}
}
