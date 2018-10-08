package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.RouteStepManeuver;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONStepManeuver {
    @JsonProperty("location")
    private Double[] location;
    @JsonProperty("bearing_before")
    private Integer bearingBefore;
    @JsonProperty("bearing_after")
    private Integer bearingAfter;

    public JSONStepManeuver(RouteStepManeuver maneuver) {
        Coordinate coordinate = maneuver.getLocation();
        if(coordinate != null) {
            if (!Double.isNaN(coordinate.z)) {
                location = new Double[3];
                location[2] = coordinate.z;
            } else {
                location = new Double[2];
            }
            location[0] = coordinate.x;
            location[1] = coordinate.y;
        }
        //bearingBefore = (maneuver.getBearingBefore() == null) ? 0 : maneuver.getBearingBefore();
        bearingAfter = maneuver.getBearingAfter();
        bearingBefore = maneuver.getBearingBefore();
    }

    public Double[] getLocation() {
        return location;
    }

    public Integer getBearingBefore() {
        return bearingBefore;
    }

    public Integer getBearingAfter() {
        return bearingAfter;
    }
}
