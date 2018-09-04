package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSegment;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSummary;
import heigit.ors.routing.RouteResult;

import java.util.List;

public class GeoJSONSummary extends JSONSummary {
    @JsonProperty("segments")
    private List<JSONSegment> segments;
    @JsonProperty("way_points")
    private int[] wayPoints;

    public GeoJSONSummary(RouteResult result, List<JSONSegment> segments) {
        super(result.getSummary().getDistance(), result.getSummary().getDuration());
        this.segments = segments;
        this.wayPoints = result.getWayPointsIndices();
    }

    public List<JSONSegment> getSegments() {
        return segments;
    }

    public int[] getWaypoints() {
        return wayPoints;
    }
}
