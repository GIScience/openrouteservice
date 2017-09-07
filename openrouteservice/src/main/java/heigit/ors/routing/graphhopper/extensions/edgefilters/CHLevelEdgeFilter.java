package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.CHEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

public abstract class CHLevelEdgeFilter implements CHEdgeFilter {
	protected final FlagEncoder encoder;
	protected final CHGraph graph;
	protected final int maxNodes;
	protected int highestNode = -1;
	protected int highestNodeLevel = -1;
	protected int baseNode;
	protected int baseNodeLevel = -1;

	public CHLevelEdgeFilter(CHGraph g, FlagEncoder encoder) {
		graph = g;
		maxNodes = g.getNodes(); 
		this.encoder = encoder;
	}

	@Override
	public boolean accept(EdgeIteratorState edgeIterState) {
		return false;
	}
	
	public int getHighestNode() {
		return highestNode;
	}

	public void setBaseNode(int nodeId) {
		baseNode = nodeId;
		if (nodeId < maxNodes)
		  baseNodeLevel = graph.getLevel(nodeId);
	}

	public void updateHighestNode(EdgeIteratorState edgeIterState) {
		int adjNode = edgeIterState.getAdjNode();
		
		if (adjNode < maxNodes)
		{
			if (highestNode == -1 || highestNodeLevel < graph.getLevel(adjNode))
			{
				highestNode =  adjNode;
				highestNodeLevel = graph.getLevel(highestNode);
			}
		}
		else
		{
			if (highestNode == -1)
				highestNode =  adjNode;
		}
	}
}
