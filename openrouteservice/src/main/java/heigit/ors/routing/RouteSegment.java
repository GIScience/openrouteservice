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
package heigit.ors.routing;

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.shapes.BBox;

import heigit.ors.common.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;

public class RouteSegment {
	private double _distance;
	private double _duration;
	private double _ascent;
	private double _descent;
	private double _detourFactor = 0.0;
	private BBox _bbox;
	private List<RouteStep> _steps;

	public RouteSegment(PathWrapper path, DistanceUnit units) throws Exception
	{
		_distance = FormatUtility.roundToDecimals(DistanceUnitUtil.convert(path.getDistance(), DistanceUnit.Meters, units), FormatUtility.getUnitDecimals(units));
		_duration =   FormatUtility.roundToDecimals(path.getTime()/1000.0, 1);
		_ascent = FormatUtility.roundToDecimals(path.getAscend(), 1);
		_descent = FormatUtility.roundToDecimals(path.getDescend() ,1);

		if (_bbox == null)
		{
			double lat = path.getPoints().getLat(0);
			double lon = path.getPoints().getLon(0);
			_bbox = new BBox(lon, lon, lat, lat);
		}

		path.calcRouteBBox(_bbox);

		_steps = new ArrayList<RouteStep>();
	}

	public double getDistance()
	{
		return _distance;
	}   

	public double getDuration()
	{
		return _duration;
	}

	public double getAscent()
	{
		return _ascent;
	}

	public double getDescent()
	{
		return _descent;
	}

	public BBox getBBox()
	{
		return _bbox;
	}

	public void addStep(RouteStep step)
	{
		_steps.add(step);
	}

	public List<RouteStep> getSteps() {
		return _steps;
	}

	public double getDetourFactor() {
		return _detourFactor;
	}

	public void setDetourFactor(double detourFactor) {
		_detourFactor = detourFactor;
	}
}
