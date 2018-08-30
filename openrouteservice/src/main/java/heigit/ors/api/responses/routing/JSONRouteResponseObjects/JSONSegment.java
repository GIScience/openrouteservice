package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;

import java.util.ArrayList;
import java.util.List;

public class JSONSegment {
    private Double distance;
    private Double duration;
    private List<JSONStep> steps;

    public JSONSegment(RouteSegment routeSegment) {
        this.distance = routeSegment.getDistance();
        this.duration = routeSegment.getDuration();
        steps = new ArrayList<>();
        for(RouteStep routeStep : routeSegment.getSteps()) {
            steps.add(new JSONStep(routeStep));
        }
    }

    public Double getDistance() {
        return distance;
    }

    public Double getDuration() {
        return duration;
    }

    public List<JSONStep> getSteps() {
        return steps;
    }
}
