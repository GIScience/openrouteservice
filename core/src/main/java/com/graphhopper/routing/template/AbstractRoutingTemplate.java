package com.graphhopper.routing.template;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PointList;

import java.util.List;

// ORS-GH MOD START
// additional imports
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.PathProcessor;
// ORS-GH MOD END

/**
 * @author Peter Karich
 */
public class AbstractRoutingTemplate {
    // result from lookup
    protected List<QueryResult> queryResults;

    protected PointList getWaypoints() {
        PointList pointList = new PointList(queryResults.size(), true);
        for (QueryResult qr : queryResults) {
            pointList.add(qr.getSnappedPoint());
        }
        return pointList;
    }

    // ORS-GH MOD START
    // ORS TODO: write a reason for this change
    protected EdgeFilter edgeFilter;

    public EdgeFilter getEdgeFilter() {
        return edgeFilter;
    }

    public void setEdgeFilter(EdgeFilter edgeFilter) {
        this.edgeFilter = edgeFilter;
    }
    // ORS-GH MOD END
}
