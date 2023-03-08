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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.routing.RoutePtStop;

import java.util.Date;
import java.util.List;

@ApiModel(value="JSONPtStop", description = "Stop of a public transport leg")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONPtStop {
    @ApiModelProperty(value = "The ID of the stop.", example = "de:08221:1138:0:O")
    @JsonProperty("stop_id")
    private final String stopId;
    @ApiModelProperty(value = "The name of the stop.", example = "Heidelberg, Alois-Link-Platz")
    @JsonProperty("name")
    private String name;
    @ApiModelProperty(value = "The location of the stop.", example = "[8.6912542, 49.399979]")
    @JsonProperty("location")
    private List<Double> location;
    @ApiModelProperty(value = "Arrival time of the stop.", example = "2022-07-04T13:22:00Z")
    @JsonProperty("arrival_time")
    private Date arrivalTime;
    @ApiModelProperty(value = "Planned arrival time of the stop.", example = "2022-07-04T13:22:00Z")
    @JsonProperty("planned_arrival_time")
    private Date plannedArrivalTime;
    @ApiModelProperty(value = "Predicted arrival time of the stop.", example = "2022-07-04T13:22:00Z")
    @JsonProperty("predicted_arrival_time")
    private Date predictedArrivalTime;
    @ApiModelProperty(value = "Whether arrival at the stop was cancelled.", example = "false")
    @JsonProperty("arrival_cancelled")
    private Boolean arrivalCancelled;
    @ApiModelProperty(value = "Departure time of the stop.", example = "2022-07-04T13:22:00Z")
    @JsonProperty("departure_time")
    private Date departureTime;
    @ApiModelProperty(value = "Planned departure time of the stop.", example = "2022-07-04T13:22:00Z")
    @JsonProperty("planned_departure_time")
    private Date plannedDepartureTime;
    @ApiModelProperty(value = "Predicted departure time of the stop.", example = "2022-07-04T13:22:00Z")
    @JsonProperty("predicted_departure_time")
    private Date predictedDepartureTime;
    @ApiModelProperty(value = "Whether departure at the stop was cancelled.", example = "false")
    @JsonProperty("departure_cancelled")
    private Boolean departureCancelled;

    public JSONPtStop(RoutePtStop stop) {
        stopId = stop.getStopId();
        name = stop.getStopName();
        location = stop.getLocationAsCoordinateList();
        arrivalTime = stop.getArrivalTime();
        plannedArrivalTime = stop.getPlannedArrivalTime();
        predictedArrivalTime = stop.getPredictedArrivalTime();
        arrivalCancelled = stop.isArrivalCancelled();
        departureTime = stop.getDepartureTime();
        plannedDepartureTime = stop.getPlannedDepartureTime();
        predictedDepartureTime = stop.getPredictedDepartureTime();
        departureCancelled = stop.isDepartureCancelled();
    }
}
