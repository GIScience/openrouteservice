package heigit.ors.routing.util;

import com.graphhopper.routing.PathProcessingContext;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

// Modification by Maxim Rylov: Added a new class.
public abstract class PathProcessor {
   public abstract void init(PathProcessingContext cntx);	
   public abstract void setSegmentIndex(int index, int count);
   public abstract void processEdge(int pathIndex, EdgeIteratorState edge, boolean lastEdge, PointList geom);
   public abstract void finish();
   public abstract PointList processPoints(PointList points);
}
