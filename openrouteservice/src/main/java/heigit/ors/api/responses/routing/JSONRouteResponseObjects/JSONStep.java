package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import heigit.ors.routing.RouteStep;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONStep {
    private Double distance;
    private Double duration;
    private Integer type;
    private String instruction;
    private String name;
    private Integer exitNumber;
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

    public Integer[] getWaypoints() {
        return waypoints;
    }
}
