package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONSegment {
    @ApiModelProperty("The total length of the route")
    @JsonProperty("distance")
    private Double distance;
    @ApiModelProperty("How long the route should take to complete")
    @JsonProperty("duration")
    private Double duration;
    @ApiModelProperty("The steps that ")
    @JsonProperty("steps")
    private List<JSONStep> steps;
    @JsonProperty("detourfactor")
    private Double detourFactor;
    @JsonProperty("percentage")
    private Double percentage;
    @JsonProperty("avgspeed")
    private Double averageSpeed;
    @JsonProperty("ascent")
    private Double ascent;
    @JsonProperty("descent")
    private Double descent;

    public JSONSegment(RouteSegment routeSegment, boolean includeElevation) {
        this.distance = routeSegment.getDistance();
        this.duration = routeSegment.getDuration();
        this.detourFactor = routeSegment.getDetourFactor();
        if(includeElevation) {
            this.ascent = routeSegment.getAscent();
            this.descent = routeSegment.getDescent();
        }
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

    public Double getDetourFactor() {
        return detourFactor;
    }

    public Double getAscent() {
        return ascent;
    }

    public Double getDescent() {
        return descent;
    }

    public List<JSONStep> getSteps() {
        return steps;
    }
}
