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

public class RouteSegmentItem {
	private int _from;
	private int _to;
	private long _value;
	private double _distance;

	public RouteSegmentItem(int from, int to, long value, double distance)
	{
		_from = from;
		_to = to;
		_value  = value;
		_distance = distance;
	}

	public int getFrom() {
		return _from;
	}

	public void setFrom(int from) {
		_from = from;
	}
	
	public int getTo() {
		return _to;
	}
	
	public void setTo(int to) {
		_to = to;
	}

	public long getValue() {
		return _value;
	}

	public void setValue(int value) {
		_value = value;
	}	
	
	public double getDistance()
	{
		return _distance;
	}
	
	public void setDistance(double value)
	{
		_distance = value;
	}
}
