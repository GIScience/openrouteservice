package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.routing.RouteResult;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONSummary {
    @JsonProperty(value = "distance")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.2d")
    protected Double distance;
    @JsonProperty(value = "duration")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    protected Double duration;
    @JsonProperty(value = "ascent")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    protected Double ascent;
    @JsonProperty(value = "descent")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    protected Double descent;

    public JSONSummary(Double distance, Double duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public JSONSummary(Double distance, Double duration, Double ascent, Double descent) {
        this(distance, duration);
        this.ascent = ascent;
        this.descent = descent;
    }

    public JSONSummary(RouteResult route, boolean includeElevation) {
        if(includeElevation) {
            this.ascent = route.getSummary().getAscent();
            this.descent = route.getSummary().getDescent();
        }
        this.distance = route.getSummary().getDistance();
        this.duration = route.getSummary().getDuration();
    }

    public Double getDistance() {
        return distance;
    }

    public Double getDuration() {
        return duration;
    }

    public Double getDescent() {
        return descent;
    }

    public Double getAscent() {
        return ascent;
    }
}