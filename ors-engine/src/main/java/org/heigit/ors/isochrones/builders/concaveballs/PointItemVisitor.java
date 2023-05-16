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
package org.heigit.ors.isochrones.builders.concaveballs;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.index.ItemVisitor;

public class PointItemVisitor implements ItemVisitor 
{
	private double threshold;
	private boolean bFound;
	private double lat;
	private double lon;

	public PointItemVisitor(double lon, double lat, double threshold) {
		this.lat = lat;
		this.lon = lon;
		this.threshold = threshold;
	}

	public void setThreshold(double value)
	{
		threshold = value;
	}
	
	public void setPoint(double lon, double lat) {
		this.lat = lat;
		this.lon = lon;
		bFound = false;
	}

	public void visitItem(Object item) {
		if (!bFound) {
			Coordinate p = (Coordinate) item;

			double dx = p.x - lon;
			if (dx > threshold)
				return;

			double dy = p.y - lat;
			if (Math.abs(dy) > threshold)
				return;

			double dist = Math.sqrt(dx*dx+dy*dy);
			if (dist < threshold)
				bFound = true;
		}
	}

	public boolean isNeighbourFound() {
		return bFound;
	}
}

