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

package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.routing.ExtraSummaryItem;
import heigit.ors.routing.RouteSegmentItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "JSONExtra", description = "An object representing one of the extra info items requested")
public class JSONExtra {
    private List<List<Long>> values;
    private List<JSONExtraSummary> summary;

    public JSONExtra(List<RouteSegmentItem> segments, List<ExtraSummaryItem> summaryItems) {
        values = new ArrayList<>();

        for(RouteSegmentItem item : segments) {
            List<Long> segment = new ArrayList<>();
            segment.add(Long.valueOf(item.getFrom()));
            segment.add(Long.valueOf(item.getTo()));
            segment.add(item.getValue());
            values.add(segment);
        }

        summary = new ArrayList<>();
        for(ExtraSummaryItem item : summaryItems) {
            summary.add(new JSONExtraSummary(item.getValue(), item.getDistance(), item.getAmount()));
        }
    }

    @ApiModelProperty(value = "A list of values representing a section of the route. The individual values are: \n" +
            "Value 1: Indice of the staring point of the geometry for this section,\n" +
            "Value 2: Indice of the end point of the geoemetry for this sections,\n" +
            "Value 3: [Value](https://github.com/GIScience/openrouteservice-docs#routing-response) assigned to this section.",
            example = "[[0,3,26],[3,10,12]]")
    @JsonProperty("values")
    private List<List<Long>> getValues() {
        return values;
    }

    @ApiModelProperty(value = "List representing the summary of the extra info items.")
    @JsonProperty("summary")
    private List<JSONExtraSummary> getSummary() {
        return summary;
    }
}
