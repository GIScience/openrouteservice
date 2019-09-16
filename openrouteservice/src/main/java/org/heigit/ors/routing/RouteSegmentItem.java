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

public class RouteSegmentItem {
	private int from;
	private int to;
	private long value;
	private double distance;

	public RouteSegmentItem(int from, int to, long value, double distance) {
		this.from = from;
		this.to = to;
		this.value = value;
		this.distance = distance;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}
	
	public int getTo() {
		return to;
	}
	
	public void setTo(int to) {
		this.to = to;
	}

	public long getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}	
	
	public double getDistance()
	{
		return distance;
	}
	
	public void setDistance(double value)
	{
		distance = value;
	}
}
