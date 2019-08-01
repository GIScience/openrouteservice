package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

// ORS-GH MOD - new class
public interface PathProcessor {
    PathProcessor DEFAULT = new DefaultPathProcessor();

    void processPathEdge(EdgeIteratorState edge, PointList geom);

    PointList processPoints(PointList points);
}
