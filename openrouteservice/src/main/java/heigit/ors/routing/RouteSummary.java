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

import com.graphhopper.util.shapes.BBox;

public class RouteSummary 
{
	private double _distance;
	//private double _distanceActual;
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

	//MARQ24 removed not implemented
	//public double getDistanceActual() {
	//	return _distanceActual;
	//}
	//public void setDistanceActual(double distanceActual) {
	//	_distanceActual = distanceActual;
	//}

	public double getAverageSpeed() {
		return _avgSpeed;
	}

	public void setAverageSpeed(double avgSpeed) {
		_avgSpeed = avgSpeed;
	}
}
