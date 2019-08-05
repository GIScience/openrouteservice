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

    public boolean isContinue() {
		return Math.abs(_bearingAfter - _bearingBefore) < 6;
	}
}
