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
