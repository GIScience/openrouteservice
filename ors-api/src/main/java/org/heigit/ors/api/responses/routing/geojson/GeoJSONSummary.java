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

package org.heigit.ors.api.responses.routing.geojson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.responses.routing.json.JSONExtra;
import org.heigit.ors.api.responses.routing.json.JSONLeg;
import org.heigit.ors.api.responses.routing.json.JSONSegment;
import org.heigit.ors.api.responses.routing.json.JSONSummary;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.routing.RouteWarning;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"distance", "duration"})
public class GeoJSONSummary extends JSONSummary {
    @JsonProperty("segments")
    private final List<JSONSegment> segments;
    private final List<Integer> wayPoints;
    private final Map<String, JSONExtra> extras;

    @JsonProperty("legs")
    private final List<JSONLeg> legs;
    private final List<RouteWarning> warnings;
    @ApiModelProperty(value = "Departure date and time" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'departure','value':true}}", example = "2020-01-31T12:45:00+01:00")
    @JsonProperty(value = "departure")
    protected ZonedDateTime departure;
    @ApiModelProperty(value = "Arrival date and time" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'arrival','value':true}}", example = "2020-01-31T13:15:00+01:00")
    @JsonProperty(value = "arrival")
    protected ZonedDateTime arrival;

    public GeoJSONSummary(RouteResult result, List<JSONSegment> segments, Map<String, JSONExtra> extras, boolean includeElevation, boolean isPtRequest, List<JSONLeg> legs) {
        super(result, includeElevation, isPtRequest);
        this.segments = segments;
        this.wayPoints = result.getWayPointsIndices();
        this.extras = extras;
        this.warnings = result.getWarnings();
        this.legs = legs;

        if(result.hasDepartureAndArrival()) {
            departure = result.getDeparture();
            arrival = result.getArrival();
        }
    }

    public List<JSONSegment> getSegments() {
        return segments;
    }

    public List<JSONLeg> getLegs() {
        return legs;
    }

    @JsonProperty("way_points")
    public List<Integer> getWaypoints() {
        return wayPoints;
    }

    @JsonProperty("extras")
    public Map<String, JSONExtra> getExtras() {
        return extras;
    }

    @JsonProperty("summary")
    public JSONSummary getSummary() {
        return new JSONSummary(this.distance, this.duration);
    }

    @JsonProperty("warnings")
    public List<Map<String, Object>> getWarnings() {
        List<Map<String, Object>> warningsMap = new ArrayList<>();
        for (RouteWarning warning: warnings) {
            Map<String, Object> warningMap = new HashMap<>();
            warningMap.put("code", warning.getWarningCode());
            warningMap.put("message", warning.getWarningMessage());
            warningsMap.add(warningMap);
        }
        return warningsMap;
    }
}
