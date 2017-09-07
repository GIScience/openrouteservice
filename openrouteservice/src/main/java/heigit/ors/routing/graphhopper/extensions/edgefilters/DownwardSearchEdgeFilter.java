package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

public class DownwardSearchEdgeFilter extends CHLevelEdgeFilter {

	public DownwardSearchEdgeFilter(CHGraph g, FlagEncoder encoder) {
		super(g, encoder);
	}

	@Override
	public boolean accept(EdgeIteratorState edgeIterState) {
		int adj = edgeIterState.getAdjNode(); 
		// always accept virtual edges, see #288
		if (baseNode >= maxNodes || adj >= maxNodes)
			return true;
 
		if (baseNodeLevel <= graph.getLevel(adj))
			return edgeIterState.isBackward(encoder);
		else
			return false;
	}
}
