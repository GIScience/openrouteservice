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

package heigit.ors.api.responses.routing.JSONRouteResponseObjects;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.routing.RouteStep;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="JSONStep", description = "Step of a route segment")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONStep {
    @ApiModelProperty(value = "The distance for the step in metres.")
    @JsonProperty("distance")
    private Double distance;
    @ApiModelProperty(value = "The duration for the step in seconds.")
    @JsonProperty("duration")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    private Double duration;
    @ApiModelProperty(value = "The [instruction](https://github.com/GIScience/openrouteservice-docs#instruction-types) action for symbolisation purposes.")
    @JsonProperty("type")
    private Integer type;
    @ApiModelProperty(value = "The routing instruction text for the step.")
    @JsonProperty("instruction")
    private String instruction;
    @ApiModelProperty(value = "The name of the next street.")
    @JsonProperty("name")
    private String name;
    @ApiModelProperty(value = "Only for roundabouts. Contains the number of the exit to take.")
    @JsonProperty("exit_number")
    private Integer exitNumber;
    @ApiModelProperty(value = "Contains the bearing of the entrance and all passed exits in a roundabout [:roundabout_exits=true].")
    @JsonProperty("exit_bearings")
    private Integer[] exitBearings;

    @ApiModelProperty(value = "List containing the indices of the steps start- and endpoint corresponding to the *geometry*.")
    @JsonProperty("way_points")
    private Integer[] waypoints;
    @ApiModelProperty(value = "The maneuver to be performed [:maneuvers=true]")
    @JsonProperty("maneuver")
    private JSONStepManeuver maneuver;

    public JSONStep(RouteStep step) {
        this.distance = step.getDistance();
        this.duration = step.getDuration();
        this.type = step.getType();
        this.instruction = step.getInstruction();
        this.name = step.getName();
        if(step.getExitNumber() != -1)
            this.exitNumber = step.getExitNumber();
        if(step.getWayPoints().length > 0) {
            waypoints = new Integer[step.getWayPoints().length];
            for (int i=0; i< step.getWayPoints().length; i++) {
                waypoints[i] = step.getWayPoints()[i];
            }
        }
        if(step.getManeuver() != null) {
            this.maneuver = new JSONStepManeuver(step.getManeuver());
        }
        if(step.getRoundaboutExitBearings() != null && step.getRoundaboutExitBearings().length > 0) {
            this.exitBearings = new Integer[step.getRoundaboutExitBearings().length];
            for(int i=0; i< step.getRoundaboutExitBearings().length; i++) {
                this.exitBearings[i] = step.getRoundaboutExitBearings()[i];
            }
        }
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

    public Integer[] getWaypoints() {
        return waypoints;
    }

    public JSONStepManeuver getManeuver() {
        return maneuver;
    }
}
