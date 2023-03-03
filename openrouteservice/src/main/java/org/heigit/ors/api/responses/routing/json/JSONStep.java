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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.routing.RouteStep;
import org.heigit.ors.util.StringUtility;

import java.util.Arrays;

@ApiModel(value="JSONStep", description = "Step of a route segment")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONStep {
    @ApiModelProperty(value = "The distance for the step in metres.", example = "245")
    @JsonProperty("distance")
    private final Double distance;
    @ApiModelProperty(value = "The duration for the step in seconds.", example = "96.2")
    @JsonProperty("duration")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    private final Double duration;
    @ApiModelProperty(value = "The [instruction](https://GIScience.github.io/openrouteservice/documentation/Instruction-Types.html) action for symbolisation purposes.", example = "1")
    @JsonProperty("type")
    private final Integer type;
    @ApiModelProperty(value = "The routing instruction text for the step.", example = "Turn right onto Berliner Straße")
    @JsonProperty("instruction")
    private final String instruction;
    @ApiModelProperty(value = "The name of the next street.", example = "Berliner Straße")
    @JsonProperty("name")
    private String name;
    @ApiModelProperty(value = "Only for roundabouts. Contains the number of the exit to take.", example = "2")
    @JsonProperty("exit_number")
    private Integer exitNumber;
    @ApiModelProperty(value = "Contains the bearing of the entrance and all passed exits in a roundabout  CUSTOM_KEYS:{'validWhen':{'ref':'roundabout_exits',value:true}}.", example = "[10,45,60]")
    @JsonProperty("exit_bearings")
    private int[] exitBearings;
    @ApiModelProperty(value = "List containing the indices of the steps start- and endpoint corresponding to the *geometry*.", example = "[45,48]")
    @JsonProperty("way_points")
    private int[] waypoints;
    @ApiModelProperty(value = "The maneuver to be performed  CUSTOM_KEYS:{'validWhen':{'ref':'maneuvers',value:true}}")
    @JsonProperty("maneuver")
    private JSONStepManeuver maneuver;

    public JSONStep(RouteStep step) {
        this.distance = step.getDistance();
        this.duration = step.getDuration();
        this.type = step.getType();
        this.instruction = step.getInstruction();

        this.name = StringUtility.isEmpty(step.getName()) ? "-" : step.getName();

        if (step.getExitNumber() != -1)
            this.exitNumber = step.getExitNumber();

        if (step.getWayPoints().length > 0)
            this.waypoints = Arrays.copyOf(step.getWayPoints(), step.getWayPoints().length);

        if (step.getManeuver() != null)
            this.maneuver = new JSONStepManeuver(step.getManeuver());

        if (step.getRoundaboutExitBearings() != null && step.getRoundaboutExitBearings().length > 0)
            this.exitBearings = Arrays.copyOf(step.getRoundaboutExitBearings(), step.getRoundaboutExitBearings().length);
    }

    public Double getDistance() {
        return distance;
    }

    public Double getDuration() {
        return duration;
    }

    public Integer getType() {
        return type;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getName() {
        return name;
    }

    public Integer getExitNumber() {
        return exitNumber;
    }

    public int[] getWaypoints() {
        return waypoints;
    }

    public JSONStepManeuver getManeuver() {
        return maneuver;
    }
}
