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

package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONExtra;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSegment;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSummary;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteWarning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"distance", "duration"})
public class GeoJSONSummary extends JSONSummary {
    @JsonProperty("segments")
    private List<JSONSegment> segments;
    private List<Integer> wayPoints;
    private Map<String, JSONExtra> extras;
    private List<RouteWarning> warnings;

    public GeoJSONSummary(RouteResult result, List<JSONSegment> segments, Map extras, boolean includeElevation) {
        super(result, includeElevation);
        this.segments = segments;
        this.wayPoints = result.getWayPointsIndices();
        this.extras = extras;
        this.warnings = result.getWarnings();
    }

    public List<JSONSegment> getSegments() {
        return segments;
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
    public List<Map> getWarnings() {
        List<Map> warningsMap = new ArrayList<>();
        for (RouteWarning warning: warnings) {
            Map<String, Object> warningMap = new HashMap<>();
            warningMap.put("code", warning.getWarningCode());
            warningMap.put("message", warning.getWarningMessage());
            warningsMap.add(warningMap);
        }
        return warningsMap;
    }
}
