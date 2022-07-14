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

import com.graphhopper.Trip;
import com.vividsolutions.jts.geom.Coordinate;

import java.util.Date;

public class RoutePtStop {
	public final String stopId;
	public final String stopName;
	public final Coordinate location;

	public final Date arrivalTime;
	public final Date plannedArrivalTime;
	public final Date predictedArrivalTime;
	public final boolean arrivalCancelled;

	public final Date departureTime;
	public final Date plannedDepartureTime;
	public final Date predictedDepartureTime;
	public final boolean departureCancelled;
	public RoutePtStop(Trip.Stop stop) {
		stopId = stop.stop_id;
		stopName = stop.stop_name;
		location = new Coordinate(stop.geometry.getX(), stop.geometry.getY());
		arrivalTime = stop.arrivalTime;
		plannedArrivalTime = stop.plannedArrivalTime;
		predictedArrivalTime = stop.predictedArrivalTime;
		arrivalCancelled = stop.arrivalCancelled;
		departureTime = stop.departureTime;
		plannedDepartureTime = stop.plannedDepartureTime;
		predictedDepartureTime = stop.predictedDepartureTime;
		departureCancelled = stop.departureCancelled;
	}
}
