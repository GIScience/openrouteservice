package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.routing.RouteStep;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONStep {
    @ApiModelProperty("The length of this step")
    private Double distance;
    @ApiModelProperty("The amount of time this step should take")
    private Double duration;
    @ApiModelProperty("The type of step")
    private Integer type;
    @ApiModelProperty("The navigational instruction to get tot the next step of the route")
    private String instruction;
    @ApiModelProperty("The name (if available) of the street that this step travels along")
    private String name;
    @ApiModelProperty("The exit number of the roundabout that must be taken")
    private Integer exitNumber;
    @ApiModelProperty("The start and end coordinates that make up this step")
    private Integer[] waypoints;

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

    @JsonProperty("way_points")
    public Integer[] getWaypoints() {
        return waypoints;
    }
}
