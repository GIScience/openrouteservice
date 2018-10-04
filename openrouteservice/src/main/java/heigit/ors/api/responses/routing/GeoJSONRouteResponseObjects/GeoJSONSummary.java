package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONExtra;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSegment;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSummary;
import heigit.ors.routing.RouteResult;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GeoJSONSummary extends JSONSummary {
    @JsonProperty("segments")
    private List<JSONSegment> segments;
    private int[] wayPoints;
    private Map<String, JSONExtra> extras;

    public GeoJSONSummary(RouteResult result, List<JSONSegment> segments, Map extras) {
        super(result.getSummary().getDistance(), result.getSummary().getDuration());
        this.segments = segments;
        this.wayPoints = result.getWayPointsIndices();
        this.extras = extras;
    }

    public List<JSONSegment> getSegments() {
        return segments;
    }

    @JsonProperty("way_points")
    public int[] getWaypoints() {
        return wayPoints;
    }

    @JsonProperty("extras")
    public Map<String, JSONExtra> getExtras() {
        return extras;
    }
}
