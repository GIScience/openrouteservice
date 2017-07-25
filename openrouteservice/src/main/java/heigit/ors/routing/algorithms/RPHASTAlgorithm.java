package heigit.ors.routing.algorithms;

import java.util.PriorityQueue;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.CHLevelEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

public class RPHASTAlgorithm extends AbstractOneToManyRoutingAlgorithm {
	private IntObjectMap<SPTEntry> bestWeightMapFrom;
	protected SPTEntry currFrom;
	protected SPTEntry currTo;
	protected PriorityQueue<SPTEntry> prioQueue;
	protected CHLevelEdgeFilter upwardEdgeFilter;
	protected CHLevelEdgeFilter downwardEdgeFilter;
	protected SubGraph targetGraph;
	
	protected boolean finishedFrom;
	protected boolean finishedTo;
	protected int visitedCountFrom;
	protected int visitedCountTo;
	
	public RPHASTAlgorithm(Graph graph, Weighting weighting, TraversalMode traversalMode) {
		super(graph, weighting, traversalMode);
		
		int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);

		initCollections(size);
		
		CHGraph chGraph = null;
		if (graph instanceof CHGraph)
			chGraph = (CHGraph)graph;
		else if (graph instanceof QueryGraph)
		{
			QueryGraph qGraph = (QueryGraph)graph;
			chGraph = (CHGraph)qGraph.getMainGraph();
		}
					
		setMaxVisitedNodes(Integer.MAX_VALUE);
		FlagEncoder encoder = weighting.getFlagEncoder();
		
		upwardEdgeFilter = new CHLevelEdgeFilter(chGraph, encoder);
		downwardEdgeFilter = new CHLevelEdgeFilter(chGraph, encoder);
		downwardEdgeFilter.setBackwardSearch(true);
		
		inEdgeExplorer = graph.createEdgeExplorer();
		outEdgeExplorer =  graph.createEdgeExplorer();
	}

	protected void initCollections(int size) {
		prioQueue = new PriorityQueue<SPTEntry>(size);
		bestWeightMapFrom = new GHIntObjectHashMap<SPTEntry>(size);
	}
	
	@Override
	public void reset()
	{
		finishedFrom = false;
		finishedTo = false;
		prioQueue.clear();
		bestWeightMapFrom.clear();
	}

	@Override
	public void prepare(int[] sources, int[] targets) {
		targetGraph = new SubGraph(graph);
		
		PriorityQueue<Integer> prioQueue = new PriorityQueue<>(200);

		addNodes(targetGraph, prioQueue, targets);

     	while (!prioQueue.isEmpty()) {
			int adjNode = prioQueue.poll();
			EdgeIterator iter = outEdgeExplorer.setBaseNode(adjNode);
			 
			while (iter.next()) {
				if (!downwardEdgeFilter.accept(iter))
					continue;

				if (targetGraph.addEdge(adjNode, iter, true))
					prioQueue.add(iter.getAdjNode());
			}
		}
     	
     	addNodes(null, prioQueue, sources);
  
     	targetGraph.print();
	}
	
	private void addNodes(SubGraph graph, PriorityQueue<Integer> prioQueue, int[] nodes)
	{
		for (int i = 0; i < nodes.length; i++) 
		{
			int nodeId = nodes[i];
			if (nodeId >= 0)
			{
				if (graph != null)
					graph.addEdge(nodeId, null, true);
				prioQueue.add(nodeId);
			}
		}
	}

	protected void runUpwardSearch() {
		while (!isMaxVisitedNodesExceeded() && !finishedFrom) {
				finishedFrom = !upwardSearch();
		}
	}

	protected void runDownwardSearch() {
		while (!finishedTo) {
			finishedTo = !downwardSearch();
		}
	}

	@Override
	public int getVisitedNodes() {
		return visitedCountFrom + visitedCountTo;
	}

	private boolean upwardSearch() {
		if (prioQueue.isEmpty()) {

			return false;
		}
		currFrom = prioQueue.poll();
		fillEdgesUpward(currFrom, prioQueue, bestWeightMapFrom, outEdgeExplorer, false);
		visitedCountFrom++;
		return true;
	}

	private boolean downwardSearch() {
		if (prioQueue.isEmpty()) {

			return false;
		}
		currTo = prioQueue.poll();
		fillEdgesDownward(currTo, prioQueue, bestWeightMapFrom, inEdgeExplorer, false);
		visitedCountTo++;

		return true;
	}
	
	@Override
	public SPTEntry[] calcPaths(int from, int[] to) {
		initUpwardSearch(from, 0);
		
		runUpwardSearch(); 
		
		initDownwardSearch(upwardEdgeFilter.getHighestNode());
  
		EdgeExplorer tmpExplorer = inEdgeExplorer;
		inEdgeExplorer = targetGraph.createExplorer();
		
		runDownwardSearch();
		
		inEdgeExplorer = tmpExplorer;

		SPTEntry[] targets = new SPTEntry[to.length];

		for (int i = 0; i < to.length; i++)
			targets[i] = bestWeightMapFrom.get(to[i]);
		
		return targets;
	}
	
	public void initUpwardSearch(int from, double weight) {
		currFrom = createSPTEntry(from, weight);
		currFrom.visited = true;
		prioQueue.add(currFrom);

		if (!traversalMode.isEdgeBased()) {
			bestWeightMapFrom.put(from, currFrom);
		} else if (currTo != null && currTo.adjNode == from) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			finishedFrom = true;
			finishedTo = true;
		}
	}
	
	private void initDownwardSearch(int to) {
		// Set the highest node from the upwards pass as the start node for the
		// downwards pass. Keep parent refs for future use in distance
		// extraction.
		currTo = bestWeightMapFrom.get(to);
		currTo.visited = true;
		prioQueue.clear();
		prioQueue.add(currTo);
		if (!traversalMode.isEdgeBased()) {
			bestWeightMapFrom.put(to, currTo);
		} else if (currFrom != null && currFrom.adjNode == to) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			finishedFrom = true;
			finishedTo = true;
		}
	}

	private void fillEdgesUpward(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> shortestWeightMap,
			EdgeExplorer explorer, boolean reverse) {
		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null) // we reach one of the target nodes
			return;
		
		while (iter.next()) {
			if (!upwardEdgeFilter.accept(iter)) 
				continue;

			double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.edge) + currEdge.weight;

			if (Double.isInfinite(tmpWeight)) 
				continue;

			// Keep track of the currently found highest CH level node
			upwardEdgeFilter.updateHighestNode(iter);

			SPTEntry ee = shortestWeightMap.get(iter.getAdjNode());

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(iter.getAdjNode(), ee);
				prioQueue.add(ee);
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge;
				prioQueue.add(ee);
			} 
		}
	}

	private void fillEdgesDownward(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue,
			IntObjectMap<SPTEntry> shortestWeightMap, EdgeExplorer explorer, boolean reverse) {

		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
		
		if (iter == null) // we reach one of the target nodes
			return;

		while (iter.next())
		{
			// no need in filter, since all edges in targetGraph are valid and acceptable
			//if (!additionalEdgeFilter.accept(iter)) 
			//	continue;
			
			double tmpWeight = weighting.calcWeight(iter, false, currEdge.edge) + currEdge.weight;
			if (Double.isInfinite(tmpWeight))
				continue;
			
			SPTEntry ee = shortestWeightMap.get(iter.getAdjNode());

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(iter.getAdjNode(), ee);
				prioQueue.add(ee);
				ee.visited = true; 
			} else if (ee.weight >= tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge; 
				prioQueue.add(ee);
			} else if (ee.visited == false) {
				// // This is the case if the node has been assigned a weight in
				// // the upwards pass (fillEdges). We need to use it in the
				// // downwards pass to access lower level nodes, though the
				// weight
				// // does not have to be reset necessarily
				// prioQueue.remove(ee);
				//
				prioQueue.add(ee);
				ee.visited = true;
			}
		}
	}
}
