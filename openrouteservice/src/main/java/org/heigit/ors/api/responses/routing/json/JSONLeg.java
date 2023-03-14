/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.responses.routing.json;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.routing.RouteLeg;
import org.heigit.ors.routing.RoutePtStop;
import org.heigit.ors.routing.RouteStep;
import org.heigit.ors.util.PolylineEncoder;
import org.locationtech.jts.geom.Coordinate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value="JSONLeg", description = "Leg of a route")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONLeg {
    @ApiModelProperty(value = "The type of the leg, possible values are currently 'walk' and 'pt'.", example = "pt")
    @JsonProperty("type")
    private final String type;
    @ApiModelProperty(value = "The departure location of the leg.", example = "Dossenheim, Süd Bstg G1")
    @JsonProperty("departure_location")
    private final String departureLocation;
    @ApiModelProperty(value = "The headsign of the public transport vehicle of the leg.", example = "Bismarckplatz - Speyererhof - EMBL - Boxberg - Mombertplatz")
    @JsonProperty("trip_headsign")
    private final String tripHeadsign;
    @ApiModelProperty(value = "The public transport route name of the leg.", example = "RNV Bus 39A")
    @JsonProperty("route_long_name")
    private final String routeLongName;
    @ApiModelProperty(value = "The public transport route name (short version) of the leg.", example = "39A")
    @JsonProperty("route_short_name")
    private final String routeShortName;
    @ApiModelProperty(value = "The route description of the leg (if provided in the GTFS data set).", example = "Bus")
    @JsonProperty("route_desc")
    private final String routeDesc;
    @ApiModelProperty(value = "The route type of the leg (if provided in the GTFS data set).", example = "1")
    @JsonProperty("route_type")
    private final int routeType;
    @ApiModelProperty(value = "The distance for the leg in metres.", example = "245")
    @JsonProperty("distance")
    private final Double distance;
    @ApiModelProperty(value = "The duration for the leg in seconds.", example = "96.2")
    @JsonProperty("duration")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    private final Double duration;
    @ApiModelProperty(value = "Departure date and time" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'departure','value':true}}", example = "2020-01-31T12:45:00+01:00")
    @JsonProperty(value = "departure")
    protected ZonedDateTime departure;
    @ApiModelProperty(value = "Arrival date and time" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'arrival','value':true}}", example = "2020-01-31T13:15:00+01:00")
    @JsonProperty(value = "arrival")
    protected ZonedDateTime arrival;
    @ApiModelProperty(value = "The feed ID this public transport leg based its information from.", example = "gtfs_0")
    @JsonProperty("feed_id")
    private String feedId;
    @ApiModelProperty(value = "The trip ID of this public transport leg.", example = "trip_id: vrn-19-39A-1-2-21-H-8-Special-50-42")
    @JsonProperty("trip_id")
    private String tripId;
    @ApiModelProperty(value = "The route ID of this public transport leg.", example = "vrn-19-39A-1")
    @JsonProperty("route_id")
    private String routeId;
    @ApiModelProperty(value = "Whether the legs continues in the same vehicle as the previous one.", example = "false")
    @JsonProperty("is_in_same_vehicle_as_previous")
    private Boolean isInSameVehicleAsPrevious;
    @ApiModelProperty(value = "The geometry of the leg. This is an encoded polyline.", example = "yuqlH{i~s@gaUe@VgEQFcBRbB_C")
    @JsonProperty("geometry")
    @JsonUnwrapped
    private final String geomResponse;
    @ApiModelProperty("List containing the specific steps the segment consists of.")
    @JsonProperty("instructions")
    private final List<JSONStep> instructions;
    @ApiModelProperty("List containing the stops the along the leg.")
    @JsonProperty("stops")
    private final List<JSONPtStop> stops;

    public JSONLeg(RouteLeg leg) {
        type = leg.getType();
        departureLocation = leg.getDepartureLocation();
        tripHeadsign = leg.getTripHeadsign();
        routeLongName = leg.getRouteLongName();
        routeShortName = leg.getRouteShortName();
        routeDesc = leg.getRouteDesc();
        routeType = leg.getRouteType();
        distance = leg.getDistance();
        duration = leg.getDuration();
        departure = leg.getDepartureTime();
        arrival = leg.getArrivalTime();
        feedId = leg.getFeedId();
        tripId = leg.getTripId();
        routeId = leg.getRouteId();
        isInSameVehicleAsPrevious = type.equals("pt") ? leg.isInSameVehicleAsPrevious() : null;
        if (leg.getInstructions() != null) {
            instructions = new ArrayList<>();
            for (RouteStep routeStep : leg.getInstructions()) {
                instructions.add(new JSONStep(routeStep));
            }
        } else {
            instructions = null;
        }
        if (leg.getStops() != null) {
            stops = new ArrayList<>();
            for(RoutePtStop stop : leg.getStops()) {
                stops.add(new JSONPtStop(stop));
            }
        } else {
            stops = null;
        }
        geomResponse = constructEncodedGeometry(leg.getGeometry(), leg.getIncludeElevation());
    }

    private String constructEncodedGeometry(final Coordinate[] coordinates, boolean includeElevation) {
        if(coordinates != null)
            return PolylineEncoder.encode(coordinates, includeElevation, new StringBuilder());
        else
            return "";
    }
}
