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

import java.util.ArrayList;
import java.util.List;

public class JSONExtra {
    private List<List<Long>> values;
    private List<JSONExtraSummary> summary;

    public JSONExtra(List<RouteSegmentItem> segments, List<ExtraSummaryItem> summaryItems) {
        values = new ArrayList<>();

        for(RouteSegmentItem item : segments) {
            List<Long> segment = new ArrayList<>();
            segment.add(new Long(item.getFrom()));
            segment.add(new Long(item.getTo()));
            segment.add(item.getValue());
            values.add(segment);
        }

        summary = new ArrayList<>();
        for(ExtraSummaryItem item : summaryItems) {
            summary.add(new JSONExtraSummary(item.getValue(), item.getDistance(), item.getAmount()));
        }

    }

    @JsonProperty("values")
    private List<List<Long>> getValues() {
        return values;
    }

    @JsonProperty("summary")
    private List<JSONExtraSummary> getSummary() {
        return summary;
    }
}
