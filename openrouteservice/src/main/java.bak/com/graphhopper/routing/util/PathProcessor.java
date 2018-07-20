package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

// ORS-GH MOD - Modification by Maxim Rylov: Added a new class.
public abstract class PathProcessor {
   public abstract void init(FlagEncoder enc);
   public abstract void setSegmentIndex(int index, int count);
   public abstract void processEdge(EdgeIteratorState edge, boolean isLastEdge, PointList geom);
   public abstract void finish();
   public abstract PointList processPoints(PointList points);
}
