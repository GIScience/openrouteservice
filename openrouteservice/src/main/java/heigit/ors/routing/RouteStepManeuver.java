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

import com.vividsolutions.jts.geom.Coordinate;

public class RouteStepManeuver {
	private Coordinate _location;
	private int _bearingBefore = 0;
	private int _bearingAfter = 0;
	
	public RouteStepManeuver()
	{}

	public Coordinate getLocation() {
		return _location;
	}

	public void setLocation(Coordinate location) {
		_location = location;
	}

	public int getBearingBefore() {
		return _bearingBefore;
	}

	public void setBearingBefore(int value) {
		_bearingBefore = value;
	}
	
	public int getBearingAfter() {
		return _bearingAfter;
	}

	public void setBearingAfter(int value) {
		_bearingAfter = value;
	}
}
