package heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import heigit.ors.routing.util.ElevationSmoother;

public class ElevationSmoothPathProcessor extends PathProcessor {
	public ElevationSmoothPathProcessor()
	{

	}

	@Override
	public void start(FlagEncoder encoder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSegmentIndex(int index, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processEdge(int pathIndex, EdgeIteratorState edge, boolean lastEdge, PointList geom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public PointList processPoints(PointList points) {
		return ElevationSmoother.smooth(points);
	}
}
