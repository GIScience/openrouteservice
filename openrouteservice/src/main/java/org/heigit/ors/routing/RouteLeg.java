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

import com.graphhopper.GHResponse;
import com.graphhopper.Trip;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.FormatUtility;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.heigit.ors.routing.RouteResult.KEY_TIMEZONE_ARRIVAL;
import static org.heigit.ors.routing.RouteResult.KEY_TIMEZONE_DEPARTURE;

public class RouteLeg {
	private final String type;
	private final String departureLocation;
	private final String tripHeadsign;
	private final double distance;
	private final double duration;
	private final ZonedDateTime departureTime;
	private final ZonedDateTime arrivalTime;
	private final String feedId;
	private final String tripId;
	private final String routeId;
	private final boolean isInSameVehicleAsPrevious;
	private Coordinate[] geometry;
	private final List<RouteStep> instructions;
	private final List<RoutePtStop> stops;
	private final boolean includeElevation;

	public RouteLeg(Trip.Leg leg, DistanceUnit units, List<RouteStep> instructions, GHResponse response, boolean includeElevation) throws Exception {
		distance = FormatUtility.roundToDecimalsForUnits(DistanceUnitUtil.convert(leg.getDistance(), DistanceUnit.METERS, units), units);
		type = leg.type;
		departureLocation = leg.departureLocation;
		String timezoneDeparture = response.getHints().getString(KEY_TIMEZONE_DEPARTURE, "UTC");
		String timezoneArrival = response.getHints().getString(KEY_TIMEZONE_ARRIVAL, "UTC");
		departureTime = ZonedDateTime.ofInstant(leg.getDepartureTime().toInstant(), ZoneId.of(timezoneDeparture));
		arrivalTime = ZonedDateTime.ofInstant(leg.getArrivalTime().toInstant(), ZoneId.of(timezoneArrival));
		geometry = new Coordinate[leg.geometry.getCoordinates().length];
		for (int i = 0; i < leg.geometry.getCoordinates().length; i++) {
			// this is an ugly hack since GH uses org.locationtech.jts.geom classes and we expect com.vividsolutions.jts.geom classes.
			// we seriously need to consolidate at some point.
			geometry[i] = new Coordinate(leg.geometry.getCoordinates()[i].x, leg.geometry.getCoordinates()[i].y, leg.geometry.getCoordinates()[i].z);
		}
		this.instructions = instructions;
		this.includeElevation = includeElevation;
		if (leg instanceof Trip.PtLeg) {
			duration = FormatUtility.roundToDecimals(((Trip.PtLeg) leg).travelTime / 1000.0, 1);
			tripHeadsign = ((Trip.PtLeg) leg).trip_headsign;
			feedId = ((Trip.PtLeg) leg).feed_id;
			tripId = ((Trip.PtLeg) leg).trip_id;
			routeId = ((Trip.PtLeg) leg).route_id;
			isInSameVehicleAsPrevious = ((Trip.PtLeg) leg).isInSameVehicleAsPrevious;
			stops = new ArrayList<>();
			for (Trip.Stop stop : ((Trip.PtLeg) leg).stops) {
				stops.add(new RoutePtStop(stop));
			}
		} else { // leg must be instance of Trip.WalkLeg
			duration = FormatUtility.roundToDecimals(getDurationSum(instructions), 1);
			tripHeadsign = null;
			feedId = null;
			tripId = null;
			routeId = null;
			isInSameVehicleAsPrevious = false;
			stops = null;
		}
	}

	private double getDurationSum(List<RouteStep> instructions) {
		double d = 0;
		for (RouteStep step : instructions) {
			d += step.getDuration();
		}
		return d;
	}

	public String getType() {
		return type;
	}

	public String getDepartureLocation() {
		return departureLocation;
	}

	public String getTripHeadsign() {
		return tripHeadsign;
	}

	public double getDistance() {
		return distance;
	}

	public double getDuration() {
		return duration;
	}

	public ZonedDateTime getDepartureTime() {
		return departureTime;
	}

	public ZonedDateTime getArrivalTime() {
		return arrivalTime;
	}

	public String getFeedId() {
		return feedId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getRouteId() {
		return routeId;
	}

	public boolean isInSameVehicleAsPrevious() {
		return isInSameVehicleAsPrevious;
	}

	public Coordinate[] getGeometry() {
		return geometry;
	}

	public List<RouteStep> getInstructions() {
		return instructions;
	}

	public List<RoutePtStop> getStops() {
		return stops;
	}

	public boolean getIncludeElevation() {
		return includeElevation;
	}
}
