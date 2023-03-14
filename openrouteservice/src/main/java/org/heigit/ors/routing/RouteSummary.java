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

import com.graphhopper.util.shapes.BBox;

public class RouteSummary {
	private double distance;
	private double duration;
	private double ascent;
	private double descent;
	private double avgSpeed;
	private BBox bbox;

	private int transfers;
	private int fare;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public int getTransfers() {
		return transfers;
	}

	public void setTransfers(int transfers) {
		this.transfers = transfers;
	}
	public int  getFare() {
		return fare;
	}

	public void setFare(int fare) {
		this.fare = fare;
	}
	public BBox getBBox()
	{
		return bbox;
	}

	public void setBBox(BBox bbox)
	{
		this.bbox = bbox;
	}

	public double getAscent() {
		return ascent;
	}

	public void setAscent(double ascent) {
		this.ascent = ascent;
	}

	public double getDescent() {
		return descent;
	}

	public void setDescent(double descent) {
		this.descent = descent;
	}

	public double getAverageSpeed() {
		return avgSpeed;
	}

	public void setAverageSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
}
