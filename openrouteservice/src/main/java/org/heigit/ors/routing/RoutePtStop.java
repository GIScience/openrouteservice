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
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RoutePtStop {
	private final String stopId;
	private final String stopName;
	private final Coordinate location;

	private final Date arrivalTime;
	private final Date plannedArrivalTime;
	private final Date predictedArrivalTime;
	private final boolean arrivalCancelled;

	private final Date departureTime;
	private final Date plannedDepartureTime;
	private final Date predictedDepartureTime;
	private final boolean departureCancelled;
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

	public String getStopId() {
		return stopId;
	}

	public String getStopName() {
		return stopName;
	}

	public List<Double> getLocationAsCoordinateList() {
		return Double.isNaN(location.z) ? Arrays.asList(location.x, location.y) : Arrays.asList(location.x, location.y, location.z);
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public Date getPlannedArrivalTime() {
		return plannedArrivalTime;
	}

	public Date getPredictedArrivalTime() {
		return predictedArrivalTime;
	}

	public boolean isArrivalCancelled() {
		return arrivalCancelled;
	}

	public Date getDepartureTime() {
		return departureTime;
	}

	public Date getPlannedDepartureTime() {
		return plannedDepartureTime;
	}

	public Date getPredictedDepartureTime() {
		return predictedDepartureTime;
	}

	public boolean isDepartureCancelled() {
		return departureCancelled;
	}
}
