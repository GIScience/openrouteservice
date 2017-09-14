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

import com.graphhopper.util.shapes.BBox;

public class RouteSummary 
{
	private double _distance;
	private double _distanceActual;
	private double _duration;
	private double _ascent;
	private double _descent;
	private double _avgSpeed;
	private BBox _bbox;

	public double getDistance() {
		return _distance;
	}

	public void setDistance(double distance) {
		_distance = distance;
	}

	public double getDuration() {
		return _duration;
	}

	public void setDuration(double duration) {
		this._duration = duration;
	}
	
	public BBox getBBox()
	{
		return _bbox;
	}
	
	public void setBBox(BBox bbox)
	{
		_bbox = bbox;
	}

	public double getAscent() {
		return _ascent;
	}

	public void setAscent(double ascent) {
		_ascent = ascent;
	}

	public double getDescent() {
		return _descent;
	}

	public void setDescent(double descent) {
		_descent = descent;
	}

	public double getDistanceActual() {
		return _distanceActual;
	}

	public void setDistanceActual(double distanceActual) {
		_distanceActual = distanceActual;
	}

	public double getAverageSpeed() {
		return _avgSpeed;
	}

	public void setAverageSpeed(double avgSpeed) {
		_avgSpeed = avgSpeed;
	}
}
