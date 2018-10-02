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
